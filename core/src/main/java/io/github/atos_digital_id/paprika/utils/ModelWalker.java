package io.github.atos_digital_id.paprika.utils;

import java.util.List;
import java.util.Properties;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Extension;
import org.apache.maven.model.Model;
import org.apache.maven.model.ModelBase;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginConfiguration;
import org.apache.maven.model.PluginContainer;
import org.apache.maven.model.Profile;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.Reporting;

import io.github.atos_digital_id.paprika.project.ArtifactId;
import lombok.NonNull;

/**
 * Maven model walker utility for GAV extract.
 **/
@Named
@Singleton
public class ModelWalker {

  // Classic API

  /**
   * Model visitor.
   **/
  public interface ModelVisitor {

    /**
     * Visit properties. Default implementation: do nothing.
     *
     * @param properties the visited properties.
     **/
    public default void visitProperties( Properties properties ) {}

    /**
     * Visit the root model. Default implementation: do nothing.
     *
     * @param model the visited model.
     **/
    public default void visitModel( Model model ) {}

    /**
     * Visit a dependency. Default implementation: do nothing.
     *
     * @param dependency the visited dependency.
     **/
    public default void visitDependency( Dependency dependency ) {}

    /**
     * Visit a parent. Default implementation: do nothing.
     *
     * @param parent the visited parent.
     **/
    public default void visitParent( Parent parent ) {}

    /**
     * Visit a plugin. Default implementation: do nothing.
     *
     * @param plugin the visited plugin.
     **/
    public default void visitPlugin( Plugin plugin ) {}

    /**
     * Visit a report plugin. Default implementation: do nothing.
     *
     * @param reportPlugin the visited report plugin.
     **/
    public default void visitReportPlugin( ReportPlugin reportPlugin ) {}

    /**
     * Visit a extension. Default implementation: do nothing.
     *
     * @param extension the visited extension.
     **/
    public default void visitExtension( Extension extension ) {}

  }

  private void visitProperties( Properties properties, ModelVisitor visitor ) {
    if( properties != null )
      visitor.visitProperties( properties );
  }

  /**
   * Launch the visit of a model.
   *
   * @param model the model to visit.
   * @param visitor the visitor to use.
   **/
  public void visitModel( Model model, @NonNull ModelVisitor visitor ) {
    if( model != null ) {
      visitModelBase( model, visitor );
      visitParent( model.getParent(), visitor );
      visitor.visitModel( model );
      visitBuild( model.getBuild(), visitor );
      visitProfiles( model.getProfiles(), visitor );
    }
  }

  private void visitModelBase( ModelBase modelBase, ModelVisitor visitor ) {
    if( modelBase != null ) {
      visitProperties( modelBase.getProperties(), visitor );
      visitDependencyManagement( modelBase.getDependencyManagement(), visitor );
      visitDependencies( modelBase.getDependencies(), visitor );
      visitReporting( modelBase.getReporting(), visitor );
    }
  }

  private void visitPluginContainer( PluginContainer pluginContainer, ModelVisitor visitor ) {
    if( pluginContainer != null )
      visitPlugins( pluginContainer.getPlugins(), visitor );
  }

  private void visitPluginConfiguration(
      PluginConfiguration pluginConfiguration,
      ModelVisitor visitor ) {
    if( pluginConfiguration != null ) {
      visitPluginContainer( pluginConfiguration, visitor );
      visitPluginContainer( pluginConfiguration.getPluginManagement(), visitor );
    }
  }

  private void visitBuild( Build build, ModelVisitor visitor ) {
    if( build != null ) {
      visitPluginConfiguration( build, visitor );
      visitExtensions( build.getExtensions(), visitor );
    }
  }

  private void visitDependency( Dependency dependency, ModelVisitor visitor ) {
    if( dependency != null )
      visitor.visitDependency( dependency );
  }

  private void visitDependencies( List<Dependency> dependencies, ModelVisitor visitor ) {
    if( dependencies != null )
      for( Dependency dependency : dependencies )
        visitDependency( dependency, visitor );
  }

  private void visitParent( Parent parent, ModelVisitor visitor ) {
    if( parent != null )
      visitor.visitParent( parent );
  }

  private void visitPlugin( Plugin plugin, ModelVisitor visitor ) {
    if( plugin != null ) {
      visitor.visitPlugin( plugin );
      visitDependencies( plugin.getDependencies(), visitor );
    }
  }

  private void visitPlugins( List<Plugin> plugins, ModelVisitor visitor ) {
    if( plugins != null )
      for( Plugin plugin : plugins )
        visitPlugin( plugin, visitor );
  }

  private void visitDependencyManagement(
      DependencyManagement dependencyManagement,
      ModelVisitor visitor ) {
    if( dependencyManagement != null )
      visitDependencies( dependencyManagement.getDependencies(), visitor );
  }

  private void visitReporting( Reporting reporting, ModelVisitor visitor ) {
    if( reporting != null )
      visitReportPlugins( reporting.getPlugins(), visitor );
  }

  private void visitProfile( Profile profile, ModelVisitor visitor ) {
    if( profile != null ) {
      visitModelBase( profile, visitor );
      visitPluginConfiguration( profile.getBuild(), visitor );
    }
  }

  private void visitProfiles( List<Profile> profiles, ModelVisitor visitor ) {
    if( profiles != null )
      for( Profile profile : profiles )
        visitProfile( profile, visitor );
  }

  private void visitReportPlugin( ReportPlugin reportPlugin, ModelVisitor visitor ) {
    if( reportPlugin != null )
      visitor.visitReportPlugin( reportPlugin );
  }

  private void visitReportPlugins( List<ReportPlugin> reportPlugins, ModelVisitor visitor ) {
    if( reportPlugins != null )
      for( ReportPlugin reportPlugin : reportPlugins )
        visitReportPlugin( reportPlugin, visitor );
  }

  private void visitExtension( Extension extension, ModelVisitor visitor ) {
    if( extension != null )
      visitor.visitExtension( extension );
  }

  private void visitExtensions( List<Extension> extensions, ModelVisitor visitor ) {
    if( extensions != null )
      for( Extension extension : extensions )
        visitExtension( extension, visitor );
  }

  // GAV

  /**
   * GAV usage.
   **/
  public enum GAVUsage {

    MODEL,

    DEPENDENCY,

    PARENT,

    PLUGIN,

    REPORT_PLUGIN,

    EXTENSION;

  }

  /**
   * GAV (group id, artifact id, version).
   **/
  public interface GAV {

    /**
     * Returns the usage.
     *
     * @return the usage.
     **/
    public GAVUsage getUsage();

    /**
     * Returns the group id.
     *
     * @return the group id.
     **/
    public String getGroupId();

    /**
     * Sets the group id.
     *
     * @param groupId the group id to set.
     **/
    public void setGroupId( String groupId );

    /**
     * Returns the artifact id.
     *
     * @return the artifact id.
     **/
    public String getArtifactId();

    /**
     * Sets the artifact id.
     *
     * @param artifactId the artifact id to set.
     **/
    public void setArtifactId( String artifactId );

    /**
     * Returns the version.
     *
     * @return the version.
     **/
    public String getVersion();

    /**
     * Sets the version.
     *
     * @param version the version to set.
     **/
    public void setVersion( String version );

    /**
     * Convert to an id.
     *
     * @return the corresponding id.
     **/
    public default ArtifactId id() {
      return new ArtifactId( getGroupId(), getArtifactId() );
    }

  }

  /**
   * Simple GAV callback.
   **/
  @FunctionalInterface
  public interface GAVVisitor {

    /**
     * Called each time a GAV is visited.
     *
     * @param gav the visited GAV.
     **/
    public void visitGAV( GAV gav );

  }

  private static class ModelVisitorAdaptor implements ModelVisitor {

    private final GAVVisitor visitor;

    public ModelVisitorAdaptor( GAVVisitor visitor ) {
      this.visitor = visitor;
    }

    @Override
    public void visitModel( Model model ) {

      visitor.visitGAV( new GAV() {

        @Override
        public GAVUsage getUsage() {
          return GAVUsage.MODEL;
        }

        @Override
        public String getGroupId() {
          return model.getGroupId();
        }

        @Override
        public void setGroupId( String groupId ) {
          model.setGroupId( groupId );
        }

        @Override
        public String getArtifactId() {
          return model.getArtifactId();
        }

        @Override
        public void setArtifactId( String artifactId ) {
          model.setArtifactId( artifactId );
        }

        @Override
        public String getVersion() {
          return model.getVersion();
        }

        @Override
        public void setVersion( String version ) {
          model.setVersion( version );
        }

        @Override
        public ArtifactId id() {
          return ArtifactId.from( model );
        }

      } );

    }

    @Override
    public void visitDependency( Dependency dependency ) {

      visitor.visitGAV( new GAV() {

        @Override
        public GAVUsage getUsage() {
          return GAVUsage.DEPENDENCY;
        }

        @Override
        public String getGroupId() {
          return dependency.getGroupId();
        }

        @Override
        public void setGroupId( String groupId ) {
          dependency.setGroupId( groupId );
        }

        @Override
        public String getArtifactId() {
          return dependency.getArtifactId();
        }

        @Override
        public void setArtifactId( String artifactId ) {
          dependency.setArtifactId( artifactId );
        }

        @Override
        public String getVersion() {
          return dependency.getVersion();
        }

        @Override
        public void setVersion( String version ) {
          dependency.setVersion( version );
        }

      } );

    }

    @Override
    public void visitParent( Parent parent ) {

      visitor.visitGAV( new GAV() {

        @Override
        public GAVUsage getUsage() {
          return GAVUsage.PARENT;
        }

        @Override
        public String getGroupId() {
          return parent.getGroupId();
        }

        @Override
        public void setGroupId( String groupId ) {
          parent.setGroupId( groupId );
        }

        @Override
        public String getArtifactId() {
          return parent.getArtifactId();
        }

        @Override
        public void setArtifactId( String artifactId ) {
          parent.setArtifactId( artifactId );
        }

        @Override
        public String getVersion() {
          return parent.getVersion();
        }

        @Override
        public void setVersion( String version ) {
          parent.setVersion( version );
        }

      } );

    }

    @Override
    public void visitPlugin( Plugin plugin ) {

      visitor.visitGAV( new GAV() {

        @Override
        public GAVUsage getUsage() {
          return GAVUsage.PLUGIN;
        }

        @Override
        public String getGroupId() {
          return plugin.getGroupId();
        }

        @Override
        public void setGroupId( String groupId ) {
          plugin.setGroupId( groupId );
        }

        @Override
        public String getArtifactId() {
          return plugin.getArtifactId();
        }

        @Override
        public void setArtifactId( String artifactId ) {
          plugin.setArtifactId( artifactId );
        }

        @Override
        public String getVersion() {
          return plugin.getVersion();
        }

        @Override
        public void setVersion( String version ) {
          plugin.setVersion( version );
        }

      } );

    }

    @Override
    public void visitReportPlugin( ReportPlugin reportPlugin ) {

      visitor.visitGAV( new GAV() {

        @Override
        public GAVUsage getUsage() {
          return GAVUsage.REPORT_PLUGIN;
        }

        @Override
        public String getGroupId() {
          return reportPlugin.getGroupId();
        }

        @Override
        public void setGroupId( String groupId ) {
          reportPlugin.setGroupId( groupId );
        }

        @Override
        public String getArtifactId() {
          return reportPlugin.getArtifactId();
        }

        @Override
        public void setArtifactId( String artifactId ) {
          reportPlugin.setArtifactId( artifactId );
        }

        @Override
        public String getVersion() {
          return reportPlugin.getVersion();
        }

        @Override
        public void setVersion( String version ) {
          reportPlugin.setVersion( version );
        }

      } );

    }

    @Override
    public void visitExtension( Extension extension ) {

      visitor.visitGAV( new GAV() {

        @Override
        public GAVUsage getUsage() {
          return GAVUsage.EXTENSION;
        }

        @Override
        public String getGroupId() {
          return extension.getGroupId();
        }

        @Override
        public void setGroupId( String groupId ) {
          extension.setGroupId( groupId );
        }

        @Override
        public String getArtifactId() {
          return extension.getArtifactId();
        }

        @Override
        public void setArtifactId( String artifactId ) {
          extension.setArtifactId( artifactId );
        }

        @Override
        public String getVersion() {
          return extension.getVersion();
        }

        @Override
        public void setVersion( String version ) {
          extension.setVersion( version );
        }

      } );

    }

  }

  /**
   * Visit all the GAV of a Mavel model.
   *
   * @param model the model to visit.
   * @param visitor the visitor to use.
   **/
  public void visitModel( Model model, @NonNull GAVVisitor visitor ) {
    visitModel( model, new ModelVisitorAdaptor( visitor ) );
  }

}
