package io.github.atos_digital_id.paprika.history;

import static org.eclipse.jgit.diff.DiffEntry.ChangeType.ADD;
import static org.eclipse.jgit.diff.DiffEntry.ChangeType.DELETE;
import static org.eclipse.jgit.diff.DiffEntry.Side.NEW;
import static org.eclipse.jgit.diff.DiffEntry.Side.OLD;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.io.ModelReader;
import org.eclipse.jgit.diff.ContentSource;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.errors.BinaryBlobException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
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
import org.eclipse.jgit.util.LfsFactory;
import org.eclipse.jgit.util.io.NullOutputStream;

import io.github.atos_digital_id.paprika.GitHandler;
import io.github.atos_digital_id.paprika.config.ConfigHandler;
import io.github.atos_digital_id.paprika.project.ArtifactDef;
import io.github.atos_digital_id.paprika.utils.Briefs.BriefModel;
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

  /**
   * Check the modifications of a module.
   **/
  public class Checker {

    private final String prefix;

    private final Predicate<String> filter;

    private final String loggedPath;

    public Checker( ArtifactDef def ) {
      String p = gitHandler.relativize( def.getWorkingDir() );
      if( !p.isEmpty() && !p.endsWith( "/" ) )
        p = p + "/";
      this.prefix = p;
      this.filter = configHandler.get( def ).getObservedPath();
      this.loggedPath = this.prefix + "{" + configHandler.get( def ).getObservedPathValue() + "}";
    }

    private boolean isModifiedIn(
        ObjectReader reader,
        ContentSource source,
        ContentSource parentSource,
        AbstractTreeIterator tree,
        AbstractTreeIterator parent ) throws IOException {

      try( DiffFormatter fmt = new DiffFormatter( NullOutputStream.INSTANCE ) ) {

        fmt.setRepository( gitHandler.repository() );

        List<DiffEntry> diffs = fmt.scan( parent, tree );
        if( diffs.isEmpty() )
          return false;

        for( DiffEntry diff : diffs ) {

          String path = diff.getNewPath();
          if( !path.startsWith( prefix ) )
            continue;

          if( !filter.test( path.substring( prefix.length() ) ) )
            continue;

          if( !POM_PATH.equals( diff.getNewPath() ) )
            return true;

          ChangeType changeType = diff.getChangeType();
          if( changeType == ADD || changeType == DELETE )
            return true;

          ParsedModel newModel = load( reader, source, diff, NEW );
          ParsedModel oldModel = load( reader, source, diff, OLD );

          if( !newModel.equals( oldModel ) )
            return true;

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

      CanonicalTreeParser headTreeParser = new CanonicalTreeParser();
      RevTree headTree = head.getTree();
      headTreeParser.reset( reader, head.getTree() );

      FileTreeIterator workingTree = new FileTreeIterator( repo );

      logger.log( "Compare trees {} and working dir on {}", Pretty.id( headTree ), loggedPath );

      return isModifiedIn(
          reader,
          ContentSource.create( workingTree ),
          source,
          workingTree,
          headTreeParser );

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

      ObjectReader reader = revWalk.getObjectReader();
      ContentSource source = ContentSource.create( reader );

      CanonicalTreeParser parser = new CanonicalTreeParser();
      RevTree tree = commit.getTree();
      parser.reset( reader, tree );

      CanonicalTreeParser parentParser = new CanonicalTreeParser();
      RevTree parentTree = parents[0].getTree();
      parentParser.reset( reader, parentTree );

      logger.log(
          "Compare trees {} and {} on {}",
          Pretty.id( tree ),
          Pretty.id( parentTree ),
          loggedPath );

      return isModifiedIn( reader, source, source, parser, parentParser );

    }

  }

  /*
   * POM
   */

  @Data
  private static class ParsedModel {

    public static final ParsedModel EMPTY = new ParsedModel( null, null );

    private final byte[] bytes;

    private final BriefModel model;

    @Override
    public int hashCode() {
      return Objects.hash( this.bytes, this.model );
    }

    @Override
    public boolean equals( Object obj ) {

      if( obj == this )
        return true;
      if( obj == null )
        return false;
      if( !( obj instanceof ParsedModel ) )
        return false;

      ParsedModel casted = (ParsedModel) obj;

      if( this.model != null )
        if( casted.model != null )
          return this.model.equals( casted.model );
        else
          return false;
      else if( casted.model != null )
        return false;
      else
        return Arrays.equals( this.bytes, casted.bytes );

    }

  }

  private final Map<ObjectId, ParsedModel> pomCache = new LinkedHashMap<>() {

    public static final long serialVersionUID = 1;

    @Override
    protected boolean removeEldestEntry( Map.Entry<ObjectId, ParsedModel> entry ) {
      return this.size() > 5;
    }

  };

  private ObjectId resolve( ObjectReader reader, AbbreviatedObjectId id ) throws IOException {

    if( id.isComplete() )
      return id.toObjectId();

    Collection<ObjectId> ids = reader.resolve( id );
    if( ids.size() == 1 )
      return ids.iterator().next();
    else if( ids.isEmpty() )
      throw new IOException( "No objects with the id " + id );
    else
      throw new IOException( "Too many objects with the id " + id + ": " + ids );

  }

  private ParsedModel load(
      ObjectReader reader,
      ContentSource source,
      DiffEntry diff,
      DiffEntry.Side side ) throws IOException {

    AbbreviatedObjectId abbr = diff.getId( side );
    ObjectId id = resolve( reader, abbr );

    ParsedModel model = pomCache.get( id );
    if( model != null )
      return model;

    ObjectLoader objectLoader = LfsFactory.getInstance().applySmudgeFilter(
        gitHandler.repository(),
        source.open( diff.getPath( side ), id ),
        diff.getDiffAttribute() );

    byte[] bytes;
    try {
      bytes = RawText.load( objectLoader, (int) objectLoader.getSize() ).getRawContent();
    } catch( BinaryBlobException ex ) {
      throw new IOException( "Can not load " + diff.getPath( side ) + ": " + ex.getMessage(), ex );
    }

    if( bytes == null || bytes.length == 0 )
      return ParsedModel.EMPTY;

    BriefModel briefModel = null;
    try( InputStream in = new ByteArrayInputStream( bytes ) ) {
      briefModel = BriefModel.ofModel( modelReader.read( in, null ) );
    } catch( IOException ex ) {
      // silent fail
    }

    model = new ParsedModel( bytes, briefModel );
    pomCache.put( id, model );

    return model;

  }

}
