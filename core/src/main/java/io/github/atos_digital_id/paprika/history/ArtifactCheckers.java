package io.github.atos_digital_id.paprika.history;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.io.ModelReader;
import org.eclipse.jgit.diff.ContentSource;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.errors.BinaryBlobException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.MutableObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;
import org.eclipse.jgit.util.LfsFactory;

import io.github.atos_digital_id.paprika.GitHandler;
import io.github.atos_digital_id.paprika.config.ConfigHandler;
import io.github.atos_digital_id.paprika.project.ArtifactDef;
import io.github.atos_digital_id.paprika.utils.Briefs.BriefModel;
import io.github.atos_digital_id.paprika.utils.Patterns;
import io.github.atos_digital_id.paprika.utils.Pretty;
import io.github.atos_digital_id.paprika.utils.cache.ArtifactIdCache;
import io.github.atos_digital_id.paprika.utils.cache.HashMapArtifactIdCache;
import io.github.atos_digital_id.paprika.utils.log.PaprikaLogger;
import lombok.Data;
import lombok.NonNull;

/**
 * Module checkers manager.
 **/
@Named
@Singleton
public class ArtifactCheckers {

  public static final String POM_PATH = "pom.xml";

  @Inject
  private PaprikaLogger logger;

  @Inject
  private ModelReader modelReader;

  @Inject
  private ConfigHandler configHandler;

  @Inject
  private GitHandler gitHandler;

  private final ArtifactIdCache<Checker> cache = new HashMapArtifactIdCache<>();

  /**
   * Returns the checker of a module.
   *
   * @param def the module to check.
   * @return the checker associated to the module.
   **/
  public Checker create( @NonNull ArtifactDef def ) {
    return cache.get( def, () -> new Checker( def ) );
  }

  @Data
  private static class IdentifiedTree {

    private final AnyObjectId id;

    private final CanonicalTreeParser tree;

    public IdentifiedTree reset() {
      this.tree.reset();
      return this;
    }

  }

  /**
   * Check the modifications of a module.
   **/
  public class Checker {

    private final ArtifactDef def;

    private final List<String> pathElts;

    private final Predicate<String> filter;

    private final String loggedPath;

    private RevCommit previousCommit;

    private IdentifiedTree previousTree;

    public Checker( ArtifactDef def ) {

      this.def = def;

      String p = gitHandler.relativize( def.getWorkingDir() );
      this.pathElts = new LinkedList<>( Patterns.split( p, '/' ) );
      Iterator<String> ite = this.pathElts.iterator();
      while( ite.hasNext() )
        if( ite.next().isEmpty() )
          ite.remove();

      this.filter = configHandler.get( def ).getObservedPath();

      this.loggedPath = p + "{" + configHandler.get( def ).getObservedPathValue() + "}";

    }

    private boolean findSubTree(
        CanonicalTreeParser tree,
        String pathElt,
        ObjectReader reader,
        MutableObjectId bufferId ) throws IOException {

      while( !tree.eof() ) {

        String path = tree.getEntryPathString();
        if( pathElt.equals( path ) ) {
          bufferId.fromRaw( tree.idBuffer(), tree.idOffset() );
          tree.reset( reader, bufferId );
          return true;
        }

        tree.next( 1 );

      }

      return false;

    }

    private IdentifiedTree getTree( ObjectReader reader, RevCommit commit ) throws IOException {

      CanonicalTreeParser tree = new CanonicalTreeParser();
      RevTree revTree = commit.getTree();
      tree.reset( reader, revTree );

      if( pathElts.isEmpty() )
        return new IdentifiedTree( revTree, tree );

      MutableObjectId bufferId = new MutableObjectId();

      for( String elt : pathElts )
        if( !findSubTree( tree, elt, reader, bufferId ) )
          return null;

      return new IdentifiedTree( bufferId, tree );

    }

    private boolean isModified( TreeWalk walk ) {

      FileMode oldMode = walk.getFileMode( 0 );
      if( oldMode == FileMode.MISSING )
        return true;

      FileMode newMode = walk.getFileMode( 1 );
      if( newMode == FileMode.MISSING )
        return true;

      if( oldMode != newMode )
        return true;

      return !walk.idEqual( 0, 1 );

    }

    private boolean isModifiedIn(
        ObjectReader reader,
        ContentSource source,
        ContentSource parentSource,
        AbstractTreeIterator tree,
        AbstractTreeIterator parent ) throws IOException {

      try( TreeWalk walk = new TreeWalk( gitHandler.repository(), reader ) ) {

        walk.addTree( parent );
        walk.addTree( tree );
        walk.setRecursive( false );

        while( walk.next() ) {

          String path = walk.getPathString();

          if( walk.isSubtree() ) {
            if( walk.getDepth() != 0 || !this.def.getModules().contains( path ) )
              walk.enterSubtree();
            continue;
          }

          logger.stack( "Diff at {}: ", path );
          try {

            if( !isModified( walk ) )
              continue;

            if( !filter.test( path ) ) {
              logger.log( "Not observed." );
              continue;
            }

            if( !POM_PATH.equals( path ) ) {
              logger.log( "Diff found." );
              return true;
            }

            BriefModel newModel;
            if( source == null ) {
              newModel = BriefModel.ofModel( def.getModel() );
            } else {
              newModel = load( reader, source, walk, 0 );
              if( newModel == null ) {
                logger.log( "Pom file can not be parsed." );
                return true;
              }
            }

            BriefModel oldModel = load( reader, parentSource, walk, 1 );
            if( oldModel == null ) {
              logger.log( "Pom file from parent commit can not be parsed." );
              return true;
            }

            if( oldModel == null || !newModel.equals( oldModel ) ) {
              logger.log( "Pom files are different." );
              return true;
            }

            logger.log( "Diff ignored." );

          } finally {
            logger.unstack();
          }

        }

        return false;

      }

    }

    /**
     * Test if the module is dirty.
     *
     * @param revWalk current revWalk.
     * @param head HEAD rev commit.
     * @return true if the module is dirty.
     * @throws IOException if any filesystem IO exception occurs.
     **/
    public boolean isDirty( @NonNull RevWalk revWalk, @NonNull RevCommit head ) throws IOException {

      Repository repo = gitHandler.repository();

      ObjectReader reader = revWalk.getObjectReader();
      ContentSource source = ContentSource.create( reader );

      IdentifiedTree headTree = getTree( reader, head );
      if( headTree == null )
        return true;

      this.previousCommit = head;
      this.previousTree = headTree;

      FileTreeIterator workingTree = new FileTreeIterator(
          this.def.getWorkingDir().toFile(),
          repo.getFS(),
          repo.getConfig().get( WorkingTreeOptions.KEY ) );

      logger
          .log( "Compare working dir and commit {} on {}", Pretty.id( head.getId() ), loggedPath );

      return isModifiedIn( reader, null, source, workingTree, headTree.getTree() );

    }

    /**
     * Test if the module is different between the working directory and the
     * tree at the commit.
     *
     * @param revWalk current revWalk.
     * @param commit tested rev commit.
     * @return true if the module has modifications.
     * @throws IOException if any filesystem IO exception occurs.
     **/
    public boolean isModifiedAt( @NonNull RevWalk revWalk, @NonNull RevCommit commit )
        throws IOException {

      RevCommit[] parents = commit.getParents();
      if( parents.length == 0 )
        return true;
      RevCommit parent = parents[0];

      ObjectReader reader = revWalk.getObjectReader();

      IdentifiedTree commitTree;
      if( this.previousCommit != null && this.previousCommit.equals( commit ) ) {
        commitTree = this.previousTree.reset();
      } else {
        commitTree = getTree( reader, commit );
        if( commitTree == null )
          return true;
      }

      IdentifiedTree parentTree = getTree( reader, parent );
      if( parentTree == null )
        return true;
      this.previousCommit = commit;
      this.previousTree = parentTree;

      if( commitTree.getId().equals( parentTree.getId() ) )
        return false;

      logger.log(
          "Compare commit {} and {} on {}",
          Pretty.id( commit.getId() ),
          Pretty.id( parent.getId() ),
          loggedPath );

      ContentSource source = ContentSource.create( reader );
      return isModifiedIn( reader, source, source, commitTree.getTree(), parentTree.getTree() );

    }

  }

  /*
   * POM
   */

  private final Map<ObjectId, BriefModel> pomCache = new LinkedHashMap<>() {

    public static final long serialVersionUID = 1;

    @Override
    protected boolean removeEldestEntry( Map.Entry<ObjectId, BriefModel> entry ) {
      return this.size() > 5;
    }

  };

  private BriefModel load( ObjectReader reader, ContentSource source, TreeWalk walk, int n )
      throws IOException {

    ObjectId id = walk.getObjectId( n );
    if( id.equals( ObjectId.zeroId() ) )
      return null;

    BriefModel model = pomCache.get( id );
    if( model != null )
      return model;

    ObjectLoader objectLoader = LfsFactory.getInstance()
        .applySmudgeFilter( gitHandler.repository(), source.open( null, id ), null );

    byte[] bytes;
    try {
      bytes = RawText.load( objectLoader, (int) objectLoader.getSize() ).getRawContent();
    } catch( BinaryBlobException ex ) {
      throw new IOException( "Can not load " + walk.getPathString() + ": " + ex.getMessage(), ex );
    }

    if( bytes == null || bytes.length == 0 )
      return null;

    try( InputStream in = new ByteArrayInputStream( bytes ) ) {
      model = BriefModel.ofModel( modelReader.read( in, null ) );
    } catch( IOException ex ) {
      // silent fail
    }

    pomCache.put( id, model );

    return model;

  }

}
