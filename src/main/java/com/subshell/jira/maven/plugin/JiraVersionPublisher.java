package com.subshell.jira.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.ProjectRestClient;
import com.atlassian.jira.rest.client.VersionRestClient;
import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.input.VersionInput;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.google.common.base.Objects;

/**
 * Maintains jira versions after a release. </br>
 * A new version is created and the released one is published. </br>
 * The Plugin can be disabled with the VM-Argument <b>-DskipJiraVersionPublisher=true</b> or </br> 
 * with the plugin configuration <b>&lt;skipJiraVersionPublisher&gt;true&lt;/skipJiraVersionPublisher&gt;</b>
 * 
 * @goal publishVersion
 * @phase
 */
public class JiraVersionPublisher extends AbstractMojo {

	private static final String SYSTEM_SKIP_PROPERTY = "skipJiraVersionPublisher";
	private static final String JIRA_KEY_FILE_NAME = ".jira-key";
	private static final String JIRA_USER = "maven";
	private static final String JIRA_PASSWORD = "d3WBMzez";
			
	/**
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;
	
	/**
	 * @parameter
	 * @optional
	 */
	private String[] skipArtifacts;
	
	/**
	 * @parameter
	 * @optional
	 */
	private String keyFileName;
	
	/**
	 * @parameter
	 * @optional
	 */
	private String jiraUser;
	
	/**
	 * @parameter
	 * @optional
	 */
	private String jiraPassword;
	
	/**
	 * @parameter
	 * @required
	 */
	private String jiraUrl; 
	
	/**
	 * @parameter
	 * @optional
	 */
	private String skipJiraVersionPublisher;
	
	public void execute() throws MojoExecutionException {
		jiraUser = Objects.firstNonNull(jiraUser, JIRA_USER);
		jiraPassword = Objects.firstNonNull(jiraPassword, JIRA_PASSWORD);
		
		if (StringUtils.isEmpty(skipJiraVersionPublisher)) {
			skipJiraVersionPublisher = System.getProperty(SYSTEM_SKIP_PROPERTY, "false");
		}
		
		if (Boolean.parseBoolean(skipJiraVersionPublisher)) {
			getLog().info("The Jira Versions are not updated automatically, as the Jira Plugin is disabled.");
			getLog().info("Please update them manually if this is not a test release.");
			return;
		}
			
		try {
			publishVersion();
		} catch (Exception e) {
			throw new MojoExecutionException(StringUtils.EMPTY, e);
		}
	}

	private void publishVersion() throws URISyntaxException {
		String projectKey = getProjectKey();
		String tagVersion = project.getVersion();
		if (StringUtils.isEmpty(projectKey)) {
			getLog().info("It is not possible to publish the jira version " + tagVersion + " as the project " + project.getArtifactId() + " does not define a jira project key");
			return;
		}

		if (!maintainProjectVersion()) {
			getLog().info("The Jira version and the project version are not synchronously maintained.");
			return;
		}
		
		JiraRestClient jiraClient = getJiraClient();
		VersionRestClient versionRestClient = jiraClient.getVersionRestClient();
		ProjectRestClient projectClient = jiraClient.getProjectClient();

		Version versionToPublish = getVersionToPublish(tagVersion, projectKey, projectClient);
		if (versionToPublish == null) {
			getLog().error("It was not possible to maintain the jira version. Please update them manually");
			return;
		}
		
		VersionInput versionInput = new VersionInput(projectKey, versionToPublish.getName(), versionToPublish.getDescription(), new DateTime(), false, true);
		versionRestClient.updateVersion(versionToPublish.getSelf(), versionInput, new NullProgressMonitor());
		getLog().info("Version published " + versionToPublish.getName() + " for project " + projectKey);

		Version newVersion = versionRestClient.createVersion(getNextServiceVersionInput(projectKey, tagVersion), new NullProgressMonitor());
		getLog().info("New Version created " + newVersion.getName() + " for project " + projectKey);
		
		versionRestClient.moveVersionAfter(newVersion.getSelf(), versionToPublish.getSelf(), new NullProgressMonitor());
	}

	private VersionInput getNextServiceVersionInput(String projectKey, String version) {
		VersionIdentifier versionToPublish = VersionIdentifier.createFromString(version);
		int newServiceVersion = versionToPublish.getService() + 1;
		VersionIdentifier newVersion = new VersionIdentifier(versionToPublish.getMajor(), versionToPublish.getMinor(), newServiceVersion);
		return new VersionInput(projectKey, newVersion.toString(), null, null, false, false);
	}

	private JiraRestClient getJiraClient() throws URISyntaxException {
		JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
		return factory.createWithBasicHttpAuthentication(new URI(jiraUrl), jiraUser, jiraPassword);
	}

	private Version getVersionToPublish(String version, String projectKey, ProjectRestClient projectClient) {
		Project project = projectClient.getProject(projectKey, new NullProgressMonitor());
		for (Version jiraVersion : project.getVersions()) {
			if (StringUtils.equals(jiraVersion.getName(), version)) {
				return jiraVersion;
			}
		}

		return null;
	}

	private String getProjectKey() {
		String keyFileName = Objects.firstNonNull(this.keyFileName, JIRA_KEY_FILE_NAME);
		
		File jiraKeyFile = new File(project.getFile().getParent(), keyFileName);
		try {
			String projectKey = FileUtils.readFileToString(jiraKeyFile);
			return StringUtils.trimToEmpty(projectKey);
		} catch (IOException e) {
			return StringUtils.EMPTY;
		}
	}
	
	
	private boolean maintainProjectVersion() {
		String artifactId = project.getArtifactId();
		if (skipArtifacts == null) {
			return true;
		}
		
		for (int i = 0; i < skipArtifacts.length; i++) {
			if (StringUtils.startsWith(artifactId, skipArtifacts[i])) {
				return false;
			}
		}
		
		return true;
	}

}
