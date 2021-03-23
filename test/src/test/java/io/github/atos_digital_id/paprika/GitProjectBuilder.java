package io.github.atos_digital_id.paprika;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;

import lombok.Getter;
import lombok.NonNull;

public class GitProjectBuilder implements AutoCloseable {

  public static final String PAPRIKA_GROUP_ID;

  public static final String PAPRIKA_ARTIFACT_ID;

  public static final String PAPRIKA_VERSION;

  static {

    // Get Paprika GAV

    try(
        InputStream inputStream =
            GitProjectBuilder.class.getResourceAsStream( "/META-INF/build-info.properties" ) ) {

      Properties properties = new Properties();
      properties.load( inputStream );

      PAPRIKA_GROUP_ID = properties.getProperty( "project.groupId" );
      PAPRIKA_ARTIFACT_ID = properties.getProperty( "project.artifactId" );
      PAPRIKA_VERSION = properties.getProperty( "project.version" );

    } catch( IOException e ) {
      throw new RuntimeException( "Can not load build info: " + e.getMessage(), e );
    }

    // Init Velocity

    Velocity.setProperty( "resource.loaders", "classpath" );
    Velocity
        .setProperty( "resource.loader.classpath.class", ClasspathResourceLoader.class.getName() );
    Velocity.init();

  }

  @Getter
  private final Path workingDir;

  private final Git git;

  public GitProjectBuilder() throws IOException, GitAPIException {

    workingDir = Files.createTempDirectory( "paprika-test" );

    git = Git.init().setDirectory( workingDir.toFile() ).call();

    install( "." );

  }

  @Override
  public void close() throws IOException {

    git.close();

    Files.walkFileTree( workingDir, new SimpleFileVisitor<Path>() {

      @Override
      public FileVisitResult visitFile( Path file, BasicFileAttributes attrs ) throws IOException {
        Files.delete( file );
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory( Path dir, IOException exc ) throws IOException {
        Files.delete( dir );
        return FileVisitResult.CONTINUE;
      }

    } );

  }

  /*
   * Files
   */

  private Path resolve( String ... elts ) {

    Path res = workingDir;
    for( String elt : elts )
      res = res.resolve( elt );

    return res.toAbsolutePath().normalize();

  }

  private void write( Path path, String template, Map<String, Object> context ) throws IOException {

    Files.createDirectories( path.getParent() );

    VelocityContext ctxt = new VelocityContext( context );
    Template tmpl = Velocity.getTemplate( template );
    if( Files.exists( path ) )
      Files.delete( path );
    try( Writer writer = Files.newBufferedWriter( path ) ) {
      tmpl.merge( ctxt, writer );
    }

  }

  public void readme( @NonNull String path, int inc ) throws IOException {

    Map<String, Object> context = new HashMap<>();
    context.put( "inc", inc );

    write( resolve( path, "README.md" ), "templates/readme.template", context );

  }

  public void install( @NonNull String path ) throws IOException {

    Map<String, Object> extensionsContext = new HashMap<>();
    extensionsContext.put( "paprikaGroupId", PAPRIKA_GROUP_ID );
    extensionsContext.put( "paprikaArtifactId", PAPRIKA_ARTIFACT_ID );
    extensionsContext.put( "paprikaVersion", PAPRIKA_VERSION );

    write(
        resolve( path, ".mvn", "extensions.xml" ),
        "templates/paprika_extension.template",
        extensionsContext );

  }

  public void config( @NonNull String path, @NonNull Map<String, String> properties )
      throws IOException {

    Map<String, Object> propertiesContext = new HashMap<>();
    propertiesContext.put( "props", properties );

    write(
        resolve( path, ".mvn", "paprika.properties" ),
        "templates/paprika_properties.template",
        propertiesContext );

  }

  public void pom(
      @NonNull String path,
      @NonNull String name,
      int inc,
      String parent,
      @NonNull String packaging,
      @NonNull List<String> modules,
      @NonNull List<String> dependencies ) throws IOException {

    Map<String, Object> context = new HashMap<>();
    context.put( "name", name );
    context.put( "inc", inc );
    context.put( "parent", parent );
    context.put( "packaging", packaging );
    context.put( "modules", modules );
    context.put( "dependencies", dependencies );

    write( resolve( path, "pom.xml" ), "templates/pom.template", context );

  }

  public void java( @NonNull String path, int inc, @NonNull String pack ) throws IOException {

    Map<String, Object> context = new HashMap<>();
    context.put( "package", pack );
    context.put( "inc", inc );

    write(
        resolve( path, "src", "main", "java", "com", pack, "FooBar.java" ),
        "templates/java.template",
        context );

  }

  /*
   * Git
   */

  private static final ZonedDateTime DEFAULT_DATE =
      ZonedDateTime.parse( "2007-12-03T10:15:30+01:00[Europe/Paris]" );

  private PersonIdent jimmy( ZonedDateTime date ) {
    return new PersonIdent(
        "Jim Raynor",
        "jim.raynor@sonsofkorhal.com",
        Date.from( date.toInstant() ),
        TimeZone.getTimeZone( date.getZone() ) );
  }

  public void commit( @NonNull String message, @NonNull ZonedDateTime date )
      throws GitAPIException {
    git.add().addFilepattern( "." ).call();
    git.commit().setAuthor( jimmy( date ) ).setCommitter( jimmy( date ) ).setMessage( message )
        .call();
  }

  public void commit( @NonNull String message ) throws GitAPIException {
    commit( message, DEFAULT_DATE );
  }

  public void tag( @NonNull String tag, @NonNull String message, @NonNull ZonedDateTime date )
      throws GitAPIException {
    git.tag().setAnnotated( true ).setName( tag ).setMessage( message ).setTagger( jimmy( date ) )
        .call();
  }

  public void tag( @NonNull String tag ) throws GitAPIException {
    tag( tag, tag, DEFAULT_DATE );
  }

  public void lightweightTag( @NonNull String tag ) throws GitAPIException {
    git.tag().setAnnotated( false ).setName( tag ).call();
  }

  public void branch( @NonNull String branch ) throws GitAPIException {
    git.checkout().setCreateBranch( true ).setName( branch ).call();
  }

}
