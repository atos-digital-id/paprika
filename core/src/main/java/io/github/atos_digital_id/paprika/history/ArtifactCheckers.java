package io.github.atos_digital_id.paprika.history;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.io.ModelReader;
import org.eclipse.jgit.diff.ContentSource;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.errors.BinaryBlobException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.FileMode;
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
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.LfsFactory;

import io.github.atos_digital_id.paprika.GitHandler;
import io.github.atos_digital_id.paprika.config.Config;
import io.github.atos_digital_id.paprika.config.ConfigHandler;
import io.github.atos_digital_id.paprika.project.ArtifactDef;
import io.github.atos_digital_id.paprika.utils.Briefs.BriefModel;
import io.github.atos_digital_id.paprika.utils.Patterns;
import io.github.atos_digital_id.paprika.utils.Patterns.PathFilter;
import io.github.atos_digital_id.paprika.utils.Patterns.PathFilterResult;
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
  private static class PathPart {

    private final int start;

    private final byte[] bytes;

    @Override
    public String toString() {
      return start + ":" + new String( bytes );
    }

  }

  /**
   * Check the modifications of a module.
   **/
  public class Checker {

    private final ArtifactDef def;

    private final List<PathPart> workingDir;

    private final int workingDirLen;

    private final int workingDirDepth;

    private final PathFilter filter;

    private final String loggedPath;

    public Checker( ArtifactDef def ) {

      this.def = def;

      String p = gitHandler.relativize( def.getWorkingDir() );

      List<String> pElts = Patterns.split( p, '/' );
      List<PathPart> parts = new ArrayList<>( pElts.size() );
      int start = 0;
      for( String e : pElts ) {
        if( !e.isEmpty() ) {
          parts.add( new PathPart( start, e.getBytes() ) );
          start += e.length() + 1;
        }
      }
      this.workingDir = parts;
      this.workingDirLen = start;
      this.workingDirDepth = parts.size();

      Config config = configHandler.get( def );

      this.filter = config.getObservedPathPredicate();

      this.loggedPath = p + "/{" + config.getObservedPath() + "}";

    }

    private final FastFilter treeFilter = new FastFilter();

    private class FastFilter extends TreeFilter {

      private boolean fs;

      private String currentPath = null;

      public FastFilter fs( boolean fs ) {
        this.fs = fs;
        return this;
      }

      public String getCurrentPath() {
        return this.currentPath;
      }

      @Override
      public boolean include( TreeWalk walk ) throws IOException {

        int depth = walk.getDepth();

        // Search working directory

        if( depth < workingDirDepth ) {

          byte[] rawPath = walk.getRawPath();

          PathPart part = workingDir.get( depth );
          int partStart = part.getStart();
          byte[] partBytes = part.getBytes();

          for( int i = 0; i < partBytes.length; i++ ) {
            if( rawPath.length <= partStart + i )
              return false;
            int comp = ( rawPath[partStart + i] & 0xff ) - ( partBytes[i] & 0xff );
            if( comp < 0 )
              return false;
            if( comp > 0 )
              throw StopWalkException.INSTANCE;
          }
          if( rawPath.length > partStart + partBytes.length ) {
            if( rawPath[partStart + partBytes.length] > '/' )
              throw StopWalkException.INSTANCE;
            return false;
          }

          // skip if the working dirs are identical
          if( ( depth == workingDirDepth - 1 ) && !fs && walk.idEqual( 0, 1 ) )
            throw StopWalkException.INSTANCE;

          return true;

        }

        this.currentPath = walk.getPathString().substring( workingDirLen );

        // Filter observable paths
        if( walk.isSubtree() && filter.partial( this.currentPath ) == PathFilterResult.TREE_MATCH )
          return true;

        // Exclude ignored files

        if( fs ) {
          FileTreeIterator oldTree = walk.getTree( 0, FileTreeIterator.class );
          if( oldTree != null && oldTree.isEntryIgnored() )
            return false;
        }

        // Filter identical versionned directories
        if( walk.isSubtree() )
          return fs || !walk.idEqual( 0, 1 );

        // Regular file: check for modification

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

      @Override
      public boolean shouldBeRecursive() {
        return false;
      }

      @Override
      public TreeFilter clone() {
        return this;
      }

    };

    private boolean isModifiedIn(
        ObjectReader reader,
        ContentSource source,
        ContentSource parentSource,
        AbstractTreeIterator tree,
        AbstractTreeIterator parent,
        boolean fs ) throws IOException {

      try( TreeWalk walk = new TreeWalk( gitHandler.repository(), reader ) ) {

        walk.addTree( tree );
        walk.addTree( parent );
        walk.setRecursive( true );
        walk.setFilter( treeFilter.fs( fs ) );

        while( walk.next() ) {

          String path = treeFilter.getCurrentPath();

          logger.stack( "Diff at {}: ", path );
          try {

            if( !filter.complete( path ) ) {
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

    private CanonicalTreeParser getTree( ObjectReader reader, RevCommit commit )
        throws IOException {
      CanonicalTreeParser tree = new CanonicalTreeParser();
      RevTree revTree = commit.getTree();
      tree.reset( reader, revTree );
      return tree;
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

      CanonicalTreeParser headTree = getTree( reader, head );

      FileTreeIterator workingTree = new FileTreeIterator( repo );

      logger
          .log( "Compare working dir and commit {} on {}", Pretty.id( head.getId() ), loggedPath );

      return isModifiedIn( reader, null, source, workingTree, headTree, true );

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

      CanonicalTreeParser commitTree = getTree( reader, commit );

      CanonicalTreeParser parentTree = getTree( reader, parent );

      logger.log(
          "Compare commit {} and {} on {}",
          Pretty.id( commit.getId() ),
          Pretty.id( parent.getId() ),
          loggedPath );

      ContentSource source = ContentSource.create( reader );
      return isModifiedIn( reader, source, source, commitTree, parentTree, false );

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
