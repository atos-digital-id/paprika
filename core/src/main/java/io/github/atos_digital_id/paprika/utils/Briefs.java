package io.github.atos_digital_id.paprika.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationFile;
import org.apache.maven.model.ActivationOS;
import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Build;
import org.apache.maven.model.BuildBase;
import org.apache.maven.model.CiManagement;
import org.apache.maven.model.ConfigurationContainer;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.Developer;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Extension;
import org.apache.maven.model.FileSet;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.InputSource;
import org.apache.maven.model.IssueManagement;
import org.apache.maven.model.License;
import org.apache.maven.model.MailingList;
import org.apache.maven.model.Model;
import org.apache.maven.model.ModelBase;
import org.apache.maven.model.Notifier;
import org.apache.maven.model.Organization;
import org.apache.maven.model.Parent;
import org.apache.maven.model.PatternSet;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginConfiguration;
import org.apache.maven.model.PluginContainer;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Relocation;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.model.Reporting;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryBase;
import org.apache.maven.model.RepositoryPolicy;
import org.apache.maven.model.Resource;
import org.apache.maven.model.Scm;
import org.apache.maven.model.Site;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Brief model (Maven pom model) management. How to generate Briefs classes:
 * <ul>
 * <li>look at brief.xslt, run it and paste the result here.
 * <li>Add package and import lines
 * <li>Change BriefModel visibility
 * <li>Change all "fake" boolean String to boolean and change getters.
 * <li>Remove all unused code.
 * <li>Remove all "internal utility" fields.
 * <li>Remove all deprecated fields and classes.
 * <li>Remove all unwanted fields:
 * <ul>
 * <li>ModelBase.modules
 * </ul>
 * </ul>
 */
public class Briefs {

  @Data
  @EqualsAndHashCode( callSuper = true )
  public static class BriefModel extends BriefModelBase {

    private final String modelVersion;
    private final BriefParent parent;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String packaging;
    private final String name;
    private final String description;
    private final String url;
    private final boolean childProjectUrlInheritAppendPath;
    private final String inceptionYear;
    private final BriefOrganization organization;
    private final Set<BriefLicense> licenses;
    private final Set<BriefDeveloper> developers;
    private final Set<BriefContributor> contributors;
    private final Set<BriefMailingList> mailingLists;
    // private final BriefPrerequisites prerequisites;
    private final BriefScm scm;
    private final BriefIssueManagement issueManagement;
    private final BriefCiManagement ciManagement;
    private final BriefBuild build;
    private final Set<BriefProfile> profiles;

    private BriefModel( Model model ) {
      super( model );
      this.modelVersion = model.getModelVersion();
      this.parent = BriefParent.ofParent( model.getParent() );
      this.groupId = model.getGroupId();
      this.artifactId = model.getArtifactId();
      this.version = model.getVersion();
      this.packaging = model.getPackaging();
      this.name = model.getName();
      this.description = model.getDescription();
      this.url = model.getUrl();
      this.childProjectUrlInheritAppendPath = model.isChildProjectUrlInheritAppendPath();
      this.inceptionYear = model.getInceptionYear();
      this.organization = BriefOrganization.ofOrganization( model.getOrganization() );
      this.licenses = BriefLicense.ofLicense( model.getLicenses() );
      this.developers = BriefDeveloper.ofDeveloper( model.getDevelopers() );
      this.contributors = BriefContributor.ofContributor( model.getContributors() );
      this.mailingLists = BriefMailingList.ofMailingList( model.getMailingLists() );
      // this.prerequisites = BriefPrerequisites.ofPrerequisites(
      // model.getPrerequisites() );
      this.scm = BriefScm.ofScm( model.getScm() );
      this.issueManagement = BriefIssueManagement.ofIssueManagement( model.getIssueManagement() );
      this.ciManagement = BriefCiManagement.ofCiManagement( model.getCiManagement() );
      this.build = BriefBuild.ofBuild( model.getBuild() );
      this.profiles = BriefProfile.ofProfile( model.getProfiles() );
    }

    public static BriefModel ofModel( Model model ) {
      return model == null ? null : new BriefModel( model );
    }

  }

  @Data
  private static class BriefModelBase {

    // private final Set<String> modules;
    private final BriefDistributionManagement distributionManagement;
    private final Properties properties;
    private final BriefDependencyManagement dependencyManagement;
    private final Set<BriefDependency> dependencies;
    private final Set<BriefRepository> repositories;
    private final Set<BriefRepository> pluginRepositories;
    // private final Object reports;
    private final BriefReporting reporting;

    private BriefModelBase( ModelBase model ) {
      // this.modules = model.getModules() == null ? null : new HashSet<>(
      // model.getModules() );
      this.distributionManagement =
          BriefDistributionManagement.ofDistributionManagement( model.getDistributionManagement() );
      this.properties = model.getProperties();
      this.dependencyManagement =
          BriefDependencyManagement.ofDependencyManagement( model.getDependencyManagement() );
      this.dependencies = BriefDependency.ofDependency( model.getDependencies() );
      this.repositories = BriefRepository.ofRepository( model.getRepositories() );
      this.pluginRepositories = BriefRepository.ofRepository( model.getPluginRepositories() );
      // this.reports = model.getReports();
      this.reporting = BriefReporting.ofReporting( model.getReporting() );
    }

  }

  @Data
  private static class BriefPluginContainer {

    private final Set<BriefPlugin> plugins;

    private BriefPluginContainer( PluginContainer model ) {
      this.plugins = BriefPlugin.ofPlugin( model.getPlugins() );
    }

  }

  @Data
  @EqualsAndHashCode( callSuper = true )
  private static class BriefPluginConfiguration extends BriefPluginContainer {

    private final BriefPluginManagement pluginManagement;

    private BriefPluginConfiguration( PluginConfiguration model ) {
      super( model );
      this.pluginManagement =
          BriefPluginManagement.ofPluginManagement( model.getPluginManagement() );
    }

  }

  @Data
  @EqualsAndHashCode( callSuper = true )
  private static class BriefBuildBase extends BriefPluginConfiguration {

    private final String defaultGoal;
    private final Set<BriefResource> resources;
    private final Set<BriefResource> testResources;
    private final String directory;
    private final String finalName;
    private final Set<String> filters;

    private BriefBuildBase( BuildBase model ) {
      super( model );
      this.defaultGoal = model.getDefaultGoal();
      this.resources = BriefResource.ofResource( model.getResources() );
      this.testResources = BriefResource.ofResource( model.getTestResources() );
      this.directory = model.getDirectory();
      this.finalName = model.getFinalName();
      this.filters = model.getFilters() == null ? null : new HashSet<>( model.getFilters() );
    }

    public static BriefBuildBase ofBuildBase( BuildBase model ) {
      return model == null ? null : new BriefBuildBase( model );
    }

  }

  @Data
  @EqualsAndHashCode( callSuper = true )
  private static class BriefBuild extends BriefBuildBase {

    private final String sourceDirectory;
    private final String scriptSourceDirectory;
    private final String testSourceDirectory;
    private final String outputDirectory;
    private final String testOutputDirectory;
    private final Set<BriefExtension> extensions;

    private BriefBuild( Build model ) {
      super( model );
      this.sourceDirectory = model.getSourceDirectory();
      this.scriptSourceDirectory = model.getScriptSourceDirectory();
      this.testSourceDirectory = model.getTestSourceDirectory();
      this.outputDirectory = model.getOutputDirectory();
      this.testOutputDirectory = model.getTestOutputDirectory();
      this.extensions = BriefExtension.ofExtension( model.getExtensions() );
    }

    public static BriefBuild ofBuild( Build model ) {
      return model == null ? null : new BriefBuild( model );
    }

  }

  @Data
  private static class BriefCiManagement {

    private final String system;
    private final String url;
    private final Set<BriefNotifier> notifiers;

    private BriefCiManagement( CiManagement model ) {
      this.system = model.getSystem();
      this.url = model.getUrl();
      this.notifiers = BriefNotifier.ofNotifier( model.getNotifiers() );
    }

    public static BriefCiManagement ofCiManagement( CiManagement model ) {
      return model == null ? null : new BriefCiManagement( model );
    }

  }

  @Data
  private static class BriefNotifier {

    private final String type;
    private final Boolean sendOnError;
    private final Boolean sendOnFailure;
    private final Boolean sendOnSuccess;
    private final Boolean sendOnWarning;

    // private final String address;

    private final Properties configuration;

    private BriefNotifier( Notifier model ) {
      this.type = model.getType();
      this.sendOnError = model.isSendOnError();
      this.sendOnFailure = model.isSendOnFailure();
      this.sendOnSuccess = model.isSendOnSuccess();
      this.sendOnWarning = model.isSendOnWarning();
      // this.address = model.getAddress();
      this.configuration = model.getConfiguration();
    }

    public static BriefNotifier ofNotifier( Notifier model ) {
      return model == null ? null : new BriefNotifier( model );
    }

    public static Set<BriefNotifier> ofNotifier( Collection<Notifier> coll ) {
      if( coll == null )
        return null;
      Set<BriefNotifier> set = new HashSet<>();
      for( Notifier model : coll )
        set.add( ofNotifier( model ) );
      return set;
    }

  }

  @Data
  private static class BriefContributor {

    private final String name;
    private final String email;
    private final String url;
    private final String organization;
    private final String organizationUrl;
    private final Set<String> roles;
    private final String timezone;
    private final Properties properties;

    private BriefContributor( Contributor model ) {
      this.name = model.getName();
      this.email = model.getEmail();
      this.url = model.getUrl();
      this.organization = model.getOrganization();
      this.organizationUrl = model.getOrganizationUrl();
      this.roles = model.getRoles() == null ? null : new HashSet<>( model.getRoles() );
      this.timezone = model.getTimezone();
      this.properties = model.getProperties();
    }

    public static BriefContributor ofContributor( Contributor model ) {
      return model == null ? null : new BriefContributor( model );
    }

    public static Set<BriefContributor> ofContributor( Collection<Contributor> coll ) {
      if( coll == null )
        return null;
      Set<BriefContributor> set = new HashSet<>();
      for( Contributor model : coll )
        set.add( ofContributor( model ) );
      return set;
    }

  }

  @Data
  private static class BriefDependency {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String type;
    private final String classifier;
    private final String scope;
    private final String systemPath;
    private final Set<BriefExclusion> exclusions;
    private final boolean optional;

    private BriefDependency( Dependency model ) {
      this.groupId = model.getGroupId();
      this.artifactId = model.getArtifactId();
      this.version = model.getVersion();
      this.type = model.getType();
      this.classifier = model.getClassifier();
      this.scope = model.getScope();
      this.systemPath = model.getSystemPath();
      this.exclusions = BriefExclusion.ofExclusion( model.getExclusions() );
      this.optional = model.isOptional();
    }

    public static BriefDependency ofDependency( Dependency model ) {
      return model == null ? null : new BriefDependency( model );
    }

    public static Set<BriefDependency> ofDependency( Collection<Dependency> coll ) {
      if( coll == null )
        return null;
      Set<BriefDependency> set = new HashSet<>();
      for( Dependency model : coll )
        set.add( ofDependency( model ) );
      return set;
    }

  }

  @Data
  @EqualsAndHashCode( callSuper = true )
  private static class BriefDeveloper extends BriefContributor {

    private final String id;

    private BriefDeveloper( Developer model ) {
      super( model );
      this.id = model.getId();
    }

    public static BriefDeveloper ofDeveloper( Developer model ) {
      return model == null ? null : new BriefDeveloper( model );
    }

    public static Set<BriefDeveloper> ofDeveloper( Collection<Developer> coll ) {
      if( coll == null )
        return null;
      Set<BriefDeveloper> set = new HashSet<>();
      for( Developer model : coll )
        set.add( ofDeveloper( model ) );
      return set;
    }

  }

  @Data
  private static class BriefExclusion {

    private final String groupId;
    private final String artifactId;

    private BriefExclusion( Exclusion model ) {
      this.groupId = model.getGroupId();
      this.artifactId = model.getArtifactId();
    }

    public static BriefExclusion ofExclusion( Exclusion model ) {
      return model == null ? null : new BriefExclusion( model );
    }

    public static Set<BriefExclusion> ofExclusion( Collection<Exclusion> coll ) {
      if( coll == null )
        return null;
      Set<BriefExclusion> set = new HashSet<>();
      for( Exclusion model : coll )
        set.add( ofExclusion( model ) );
      return set;
    }

  }

  @Data
  private static class BriefIssueManagement {

    private final String system;
    private final String url;

    private BriefIssueManagement( IssueManagement model ) {
      this.system = model.getSystem();
      this.url = model.getUrl();
    }

    public static BriefIssueManagement ofIssueManagement( IssueManagement model ) {
      return model == null ? null : new BriefIssueManagement( model );
    }

  }

  @Data
  private static class BriefDistributionManagement {

    private final BriefDeploymentRepository repository;
    private final BriefDeploymentRepository snapshotRepository;
    private final BriefSite site;
    private final String downloadUrl;
    private final BriefRelocation relocation;
    private final String status;

    private BriefDistributionManagement( DistributionManagement model ) {
      this.repository = BriefDeploymentRepository.ofDeploymentRepository( model.getRepository() );
      this.snapshotRepository =
          BriefDeploymentRepository.ofDeploymentRepository( model.getSnapshotRepository() );
      this.site = BriefSite.ofSite( model.getSite() );
      this.downloadUrl = model.getDownloadUrl();
      this.relocation = BriefRelocation.ofRelocation( model.getRelocation() );
      this.status = model.getStatus();
    }

    public static BriefDistributionManagement ofDistributionManagement(
        DistributionManagement model ) {
      return model == null ? null : new BriefDistributionManagement( model );
    }

  }

  @Data
  private static class BriefLicense {

    private final String name;
    private final String url;
    private final String distribution;
    private final String comments;

    private BriefLicense( License model ) {
      this.name = model.getName();
      this.url = model.getUrl();
      this.distribution = model.getDistribution();
      this.comments = model.getComments();
    }

    public static BriefLicense ofLicense( License model ) {
      return model == null ? null : new BriefLicense( model );
    }

    public static Set<BriefLicense> ofLicense( Collection<License> coll ) {
      if( coll == null )
        return null;
      Set<BriefLicense> set = new HashSet<>();
      for( License model : coll )
        set.add( ofLicense( model ) );
      return set;
    }

  }

  @Data
  private static class BriefMailingList {

    private final String name;
    private final String subscribe;
    private final String unsubscribe;
    private final String post;
    private final String archive;
    private final Set<String> otherArchives;

    private BriefMailingList( MailingList model ) {
      this.name = model.getName();
      this.subscribe = model.getSubscribe();
      this.unsubscribe = model.getUnsubscribe();
      this.post = model.getPost();
      this.archive = model.getArchive();
      this.otherArchives =
          model.getOtherArchives() == null ? null : new HashSet<>( model.getOtherArchives() );
    }

    public static BriefMailingList ofMailingList( MailingList model ) {
      return model == null ? null : new BriefMailingList( model );
    }

    public static Set<BriefMailingList> ofMailingList( Collection<MailingList> coll ) {
      if( coll == null )
        return null;
      Set<BriefMailingList> set = new HashSet<>();
      for( MailingList model : coll )
        set.add( ofMailingList( model ) );
      return set;
    }

  }

  @Data
  private static class BriefOrganization {

    private final String name;
    private final String url;

    private BriefOrganization( Organization model ) {
      this.name = model.getName();
      this.url = model.getUrl();
    }

    public static BriefOrganization ofOrganization( Organization model ) {
      return model == null ? null : new BriefOrganization( model );
    }

  }

  @Data
  private static class BriefPatternSet {

    private final Set<String> includes;
    private final Set<String> excludes;

    private BriefPatternSet( PatternSet model ) {
      this.includes = model.getIncludes() == null ? null : new HashSet<>( model.getIncludes() );
      this.excludes = model.getExcludes() == null ? null : new HashSet<>( model.getExcludes() );
    }

  }

  @Data
  private static class BriefParent {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String relativePath;

    private BriefParent( Parent model ) {
      this.groupId = model.getGroupId();
      this.artifactId = model.getArtifactId();
      this.version = model.getVersion();
      this.relativePath = model.getRelativePath();
    }

    public static BriefParent ofParent( Parent model ) {
      return model == null ? null : new BriefParent( model );
    }

  }

  @Data
  private static class BriefScm {

    private final String connection;
    private final String developerConnection;
    private final String tag;
    private final String url;
    private final boolean childScmConnectionInheritAppendPath;
    private final boolean childScmDeveloperConnectionInheritAppendPath;
    private final boolean childScmUrlInheritAppendPath;

    private BriefScm( Scm model ) {
      this.connection = model.getConnection();
      this.developerConnection = model.getDeveloperConnection();
      this.tag = model.getTag();
      this.url = model.getUrl();
      this.childScmConnectionInheritAppendPath = model.isChildScmConnectionInheritAppendPath();
      this.childScmDeveloperConnectionInheritAppendPath =
          model.isChildScmDeveloperConnectionInheritAppendPath();
      this.childScmUrlInheritAppendPath = model.isChildScmUrlInheritAppendPath();
    }

    public static BriefScm ofScm( Scm model ) {
      return model == null ? null : new BriefScm( model );
    }

  }

  @Data
  @EqualsAndHashCode( callSuper = true )
  private static class BriefFileSet extends BriefPatternSet {

    private final String directory;

    private BriefFileSet( FileSet model ) {
      super( model );
      this.directory = model.getDirectory();
    }

  }

  @Data
  @EqualsAndHashCode( callSuper = true )
  private static class BriefResource extends BriefFileSet {

    private final String targetPath;
    private final boolean filtering;
    private final String mergeId;

    private BriefResource( Resource model ) {
      super( model );
      this.targetPath = model.getTargetPath();
      this.filtering = model.isFiltering();
      this.mergeId = model.getMergeId();
    }

    public static BriefResource ofResource( Resource model ) {
      return model == null ? null : new BriefResource( model );
    }

    public static Set<BriefResource> ofResource( Collection<Resource> coll ) {
      if( coll == null )
        return null;
      Set<BriefResource> set = new HashSet<>();
      for( Resource model : coll )
        set.add( ofResource( model ) );
      return set;
    }

  }

  @Data
  private static class BriefRepositoryBase {

    private final String id;
    private final String name;
    private final String url;
    private final String layout;

    private BriefRepositoryBase( RepositoryBase model ) {
      this.id = model.getId();
      this.name = model.getName();
      this.url = model.getUrl();
      this.layout = model.getLayout();
    }

  }

  @Data
  @EqualsAndHashCode( callSuper = true )
  private static class BriefRepository extends BriefRepositoryBase {

    private final BriefRepositoryPolicy releases;
    private final BriefRepositoryPolicy snapshots;

    private BriefRepository( Repository model ) {
      super( model );
      this.releases = BriefRepositoryPolicy.ofRepositoryPolicy( model.getReleases() );
      this.snapshots = BriefRepositoryPolicy.ofRepositoryPolicy( model.getSnapshots() );
    }

    public static BriefRepository ofRepository( Repository model ) {
      return model == null ? null : new BriefRepository( model );
    }

    public static Set<BriefRepository> ofRepository( Collection<Repository> coll ) {
      if( coll == null )
        return null;
      Set<BriefRepository> set = new HashSet<>();
      for( Repository model : coll )
        set.add( ofRepository( model ) );
      return set;
    }

  }

  @Data
  @EqualsAndHashCode( callSuper = true )
  private static class BriefDeploymentRepository extends BriefRepository {

    private final Boolean uniqueVersion;

    private BriefDeploymentRepository( DeploymentRepository model ) {
      super( model );
      this.uniqueVersion = model.isUniqueVersion();
    }

    public static BriefDeploymentRepository ofDeploymentRepository( DeploymentRepository model ) {
      return model == null ? null : new BriefDeploymentRepository( model );
    }

  }

  @Data
  private static class BriefRepositoryPolicy {

    private final boolean enabled;
    private final String updatePolicy;
    private final String checksumPolicy;

    private BriefRepositoryPolicy( RepositoryPolicy model ) {
      this.enabled = model.isEnabled();
      this.updatePolicy = model.getUpdatePolicy();
      this.checksumPolicy = model.getChecksumPolicy();
    }

    public static BriefRepositoryPolicy ofRepositoryPolicy( RepositoryPolicy model ) {
      return model == null ? null : new BriefRepositoryPolicy( model );
    }

  }

  @Data
  private static class BriefSite {

    private final String id;
    private final String name;
    private final String url;
    private final boolean childSiteUrlInheritAppendPath;

    private BriefSite( Site model ) {
      this.id = model.getId();
      this.name = model.getName();
      this.url = model.getUrl();
      this.childSiteUrlInheritAppendPath = model.isChildSiteUrlInheritAppendPath();
    }

    public static BriefSite ofSite( Site model ) {
      return model == null ? null : new BriefSite( model );
    }

  }

  @Data
  private static class BriefConfigurationContainer {

    private final boolean inherited;
    private final Object configuration;

    private BriefConfigurationContainer( ConfigurationContainer model ) {
      this.inherited = model.isInherited();
      this.configuration = model.getConfiguration();
    }

  }

  @Data
  @EqualsAndHashCode( callSuper = true )
  private static class BriefPlugin extends BriefConfigurationContainer {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final boolean extensions;
    private final Set<BriefPluginExecution> executions;
    private final Set<BriefDependency> dependencies;

    // private final Object goals;

    private BriefPlugin( Plugin model ) {
      super( model );
      this.groupId = model.getGroupId();
      this.artifactId = model.getArtifactId();
      this.version = model.getVersion();
      this.extensions = model.isExtensions();
      this.executions = BriefPluginExecution.ofPluginExecution( model.getExecutions() );
      this.dependencies = BriefDependency.ofDependency( model.getDependencies() );
      // this.goals = model.getGoals();
    }

    public static BriefPlugin ofPlugin( Plugin model ) {
      return model == null ? null : new BriefPlugin( model );
    }

    public static Set<BriefPlugin> ofPlugin( Collection<Plugin> coll ) {
      if( coll == null )
        return null;
      Set<BriefPlugin> set = new HashSet<>();
      for( Plugin model : coll )
        set.add( ofPlugin( model ) );
      return set;
    }

  }

  @Data
  @EqualsAndHashCode( callSuper = true )
  private static class BriefPluginExecution extends BriefConfigurationContainer {

    private final String id;
    private final String phase;

    // private final Integer priority;

    private final Set<String> goals;

    private BriefPluginExecution( PluginExecution model ) {
      super( model );
      this.id = model.getId();
      this.phase = model.getPhase();
      // this.priority = model.getPriority();
      this.goals = model.getGoals() == null ? null : new HashSet<>( model.getGoals() );
    }

    public static BriefPluginExecution ofPluginExecution( PluginExecution model ) {
      return model == null ? null : new BriefPluginExecution( model );
    }

    public static Set<BriefPluginExecution> ofPluginExecution( Collection<PluginExecution> coll ) {
      if( coll == null )
        return null;
      Set<BriefPluginExecution> set = new HashSet<>();
      for( PluginExecution model : coll )
        set.add( ofPluginExecution( model ) );
      return set;
    }

  }

  @Data
  private static class BriefDependencyManagement {

    private final Set<BriefDependency> dependencies;

    private BriefDependencyManagement( DependencyManagement model ) {
      this.dependencies = BriefDependency.ofDependency( model.getDependencies() );
    }

    public static BriefDependencyManagement ofDependencyManagement( DependencyManagement model ) {
      return model == null ? null : new BriefDependencyManagement( model );
    }

  }

  @Data
  @EqualsAndHashCode( callSuper = true )
  private static class BriefPluginManagement extends BriefPluginContainer {

    private BriefPluginManagement( PluginManagement model ) {
      super( model );
    }

    public static BriefPluginManagement ofPluginManagement( PluginManagement model ) {
      return model == null ? null : new BriefPluginManagement( model );
    }

  }

  @Data
  private static class BriefReporting {

    private final boolean excludeDefaults;
    private final String outputDirectory;
    private final Set<BriefReportPlugin> plugins;

    private BriefReporting( Reporting model ) {
      this.excludeDefaults = model.isExcludeDefaults();
      this.outputDirectory = model.getOutputDirectory();
      this.plugins = BriefReportPlugin.ofReportPlugin( model.getPlugins() );
    }

    public static BriefReporting ofReporting( Reporting model ) {
      return model == null ? null : new BriefReporting( model );
    }

  }

  @Data
  @EqualsAndHashCode( callSuper = true )
  private static class BriefProfile extends BriefModelBase {

    private final String id;
    private final BriefActivation activation;
    private final BriefBuildBase build;

    private BriefProfile( Profile model ) {
      super( model );
      this.id = model.getId();
      this.activation = BriefActivation.ofActivation( model.getActivation() );
      this.build = BriefBuildBase.ofBuildBase( model.getBuild() );
    }

    public static BriefProfile ofProfile( Profile model ) {
      return model == null ? null : new BriefProfile( model );
    }

    public static Set<BriefProfile> ofProfile( Collection<Profile> coll ) {
      if( coll == null )
        return null;
      Set<BriefProfile> set = new HashSet<>();
      for( Profile model : coll )
        set.add( ofProfile( model ) );
      return set;
    }

  }

  @Data
  private static class BriefActivation {

    private final Boolean activeByDefault;
    private final String jdk;
    private final BriefActivationOS os;
    private final BriefActivationProperty property;
    private final BriefActivationFile file;

    private BriefActivation( Activation model ) {
      this.activeByDefault = model.isActiveByDefault();
      this.jdk = model.getJdk();
      this.os = BriefActivationOS.ofActivationOS( model.getOs() );
      this.property = BriefActivationProperty.ofActivationProperty( model.getProperty() );
      this.file = BriefActivationFile.ofActivationFile( model.getFile() );
    }

    public static BriefActivation ofActivation( Activation model ) {
      return model == null ? null : new BriefActivation( model );
    }

  }

  @Data
  private static class BriefActivationProperty {

    private final String name;
    private final String value;

    private BriefActivationProperty( ActivationProperty model ) {
      this.name = model.getName();
      this.value = model.getValue();
    }

    public static BriefActivationProperty ofActivationProperty( ActivationProperty model ) {
      return model == null ? null : new BriefActivationProperty( model );
    }

  }

  @Data
  private static class BriefActivationOS {

    private final String name;
    private final String family;
    private final String arch;
    private final String version;

    private BriefActivationOS( ActivationOS model ) {
      this.name = model.getName();
      this.family = model.getFamily();
      this.arch = model.getArch();
      this.version = model.getVersion();
    }

    public static BriefActivationOS ofActivationOS( ActivationOS model ) {
      return model == null ? null : new BriefActivationOS( model );
    }

  }

  @Data
  private static class BriefActivationFile {

    private final String missing;
    private final String exists;

    private BriefActivationFile( ActivationFile model ) {
      this.missing = model.getMissing();
      this.exists = model.getExists();
    }

    public static BriefActivationFile ofActivationFile( ActivationFile model ) {
      return model == null ? null : new BriefActivationFile( model );
    }

  }

  @Data
  @EqualsAndHashCode( callSuper = true )
  private static class BriefReportPlugin extends BriefConfigurationContainer {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final Set<BriefReportSet> reportSets;

    private BriefReportPlugin( ReportPlugin model ) {
      super( model );
      this.groupId = model.getGroupId();
      this.artifactId = model.getArtifactId();
      this.version = model.getVersion();
      this.reportSets = BriefReportSet.ofReportSet( model.getReportSets() );
    }

    public static BriefReportPlugin ofReportPlugin( ReportPlugin model ) {
      return model == null ? null : new BriefReportPlugin( model );
    }

    public static Set<BriefReportPlugin> ofReportPlugin( Collection<ReportPlugin> coll ) {
      if( coll == null )
        return null;
      Set<BriefReportPlugin> set = new HashSet<>();
      for( ReportPlugin model : coll )
        set.add( ofReportPlugin( model ) );
      return set;
    }

  }

  @Data
  @EqualsAndHashCode( callSuper = true )
  private static class BriefReportSet extends BriefConfigurationContainer {

    private final String id;
    private final Set<String> reports;

    private BriefReportSet( ReportSet model ) {
      super( model );
      this.id = model.getId();
      this.reports = model.getReports() == null ? null : new HashSet<>( model.getReports() );
    }

    public static BriefReportSet ofReportSet( ReportSet model ) {
      return model == null ? null : new BriefReportSet( model );
    }

    public static Set<BriefReportSet> ofReportSet( Collection<ReportSet> coll ) {
      if( coll == null )
        return null;
      Set<BriefReportSet> set = new HashSet<>();
      for( ReportSet model : coll )
        set.add( ofReportSet( model ) );
      return set;
    }

  }

  @Data
  private static class BriefRelocation {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String message;

    private BriefRelocation( Relocation model ) {
      this.groupId = model.getGroupId();
      this.artifactId = model.getArtifactId();
      this.version = model.getVersion();
      this.message = model.getMessage();
    }

    public static BriefRelocation ofRelocation( Relocation model ) {
      return model == null ? null : new BriefRelocation( model );
    }

  }

  @Data
  private static class BriefExtension {

    private final String groupId;
    private final String artifactId;
    private final String version;

    private BriefExtension( Extension model ) {
      this.groupId = model.getGroupId();
      this.artifactId = model.getArtifactId();
      this.version = model.getVersion();
    }

    public static BriefExtension ofExtension( Extension model ) {
      return model == null ? null : new BriefExtension( model );
    }

    public static Set<BriefExtension> ofExtension( Collection<Extension> coll ) {
      if( coll == null )
        return null;
      Set<BriefExtension> set = new HashSet<>();
      for( Extension model : coll )
        set.add( ofExtension( model ) );
      return set;
    }

  }

  @Data
  private static class BriefInputLocation {

    private BriefInputLocation( InputLocation model ) {}

  }

  @Data
  private static class BriefInputSource {

    private final String modelId;
    private final String location;

    private BriefInputSource( InputSource model ) {
      this.modelId = model.getModelId();
      this.location = model.getLocation();
    }

  }

}
