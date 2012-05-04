Maven-Jira-Plugin
=================

Automatic maintenance of Jira project Versions with Maven
---------------------------------------------------------

The maintenance of project versions in Jira is an unpleasant, recurring and error-prone task. So let's automate it!

One must not forget to update the project version in jira after a release.
But in this case prospective bug fixes will be marked as fixed in wrong versions, which leads to confusion and irritations in the praxis. And furthermore it is an annoying task which can be automated. 

So we developed a small maven plugin which does the work for us. Take the release of a project in version 1.0.0 as an example. The corresponding jira project version is published with the actual release date and the new development version 1.0.1 is created and sorted correctly among the other versions of the project. 
The result can be reviewed in the output of the maven process:


	[INFO] Version published 1.35.0 for project SOSI
	[INFO] New Version created 1.35.1 for project SOSI
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 6.881s
	[INFO] Finished at: Thu May 03 16:56:16 CEST 2012
	[INFO] Final Memory: 14M/334M
	[INFO] ------------------------------------------------------------------------


Although development of the plugin took one day, it simplifies the release process and prevents failures.
The plugin can be easily integrated and configured. How it works and how it is integrated is explained in the following.

How it works
------------

Unfortunately, the maven release plugin can not be extended to do the trick for us. So we need to work around this obstacle. And that’s how it works:

With the help of the release plugin a profile is actived whenever a release is performed. Let’s call this profile jira. This is done with the help of the configuration releaseProfiles of the maven-release-plugin. That’s how it looks like in your pom.xml:

	<project>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-release-plugin</artifactId>
				<configuration>
					<releaseProfiles>jira</releaseProfiles>
				</configuration>
			</plugin>
		...
		</plugins>
	...
	</project>


Furthermore it is  necessary to configure the execution of the plugin.

	<profiles>
	 <profile>
	   <id>jira</id>
	   <build>
	     <plugins>
	       <plugin>
	          <groupId>com.subshell</groupId>
	          <artifactId>maven-jira-plugin</artifactId>
	          <executions>
	              <execution>
	               <phase>deploy</phase>
	               <goals>
	                    <goal>publishVersion</goal>
	               </goals>
	               <configuration>
	                   <jiraUrl>http://support.subshell.com</jiraUrl>
	                   <skipArtifacts>
	                     <skipArtifact>com.subshell.sophora.testutil</skipArtifact>
	                     <skipArtifact>com.subshell.sophora.osgi.api</skipArtifact>
	                     <skipArtifact>com.subshell.sophora.indexer.</skipArtifact>
	                     <skipArtifact>com.subshell.sophora.importer.</skipArtifact>
	                     <skipArtifact>com.subshell.sophora.delivery.</skipArtifact>
	                   </skipArtifacts>
	                   <jiraUser>maven</jiraUser>
	                </configuration>
	              </execution>
	          </executions>
	       </plugin>
	     </plugins>
	   </build>
	  </profile>
	</profiles>


So the release plugin activates the jira profile and consequently the deploy phase is called where our plugin is executed.

Connection to Jira
------------------

The link between the maven project and the jira project is made via a small meta file. It only contains the jira project key of its project. By default this file has the name .jira-key, but it is configurable as well.
The jira rest client is used to communicate with jira. For the authentification it is necessary to specify a username and a password. This may be a security impact in your setup. But you have the choice to specify the password in the plugin itself and not in the maven configuration. Furthermore there is the possibility of using the OAuth authentication. But this method is marked as "work in progress" in the current jira rest client 0.5.

Configuration
-------------

As we put the maven configuration mentioned above in the parent pom of our projects we wanted to exclude some projects, as the version number of these projects are not synchronously maintained with their corresponding jira project. Typically these are plugins or extensions. To do so just add the artifact id to the list of skiped artifacts. It is sufficient for a project to be skipped if its artifate id starts with one of the specified ones.
For testing purposes it is also possible to deactivate this plugin using the parameter skipJiraVersionPublisher

Overview
--------
* jiraUser
* jiraPassword
* jriaUrl
* keyFileName
* skipArtifacts
* skipJiraVersionPublisher

Tested with
-----------
* Jira 4.4.4
* Maven 3.0.4

Interested
----------
Give it a try and check it out from github. Improvements or comments are welcome.
