Script Builder
--------------

This is a simple project which can be used to track implemented features through you
different database versions. To map database changes to source features a convention
is used to pick up the file "features.xml" in the directory:

${project.home}/src/main/resources/database (based on Maven directory layout)

This directory can however be changed by providing the optional override argument (-d)
to specify a directory relative to ${project.home}.

Typical usage will be:

  java -jar script-build-{version}.jar

  followed by the following arguments

  Arguments   Value			Description
  p	      property file location	The property file can be used in place of
  	      		     		supplying multiple command line options
  i	    				Initialise audit tables in the schema to
  					keep track of implemented features
  u	      database username		The username of the database
  x	      database password		The password of the database
  l	      database location		The location of the database using the format:
  	      				jdbc:oracle:thin:@<host>:<port>:<sid>
  o	      output diff location	The location of the output file, incluing
  	      	      	   		the file name.
  d           database script path	An override to provide the directory where
              relative to project       script files are located
	      home directory 
  t	      project home		The path to the workspace directory of
  	      	      			the project

By convention (and there is currently no way to change this) the database script
directory should contain the following structure:

  {database.script.path}/database/features.xml
  {database.script.path}/database/features/

Where each feature is included by adding the feature to the features.xml list:

  <sta:feature>
    <sta:name>FEATURE1</sta:name>
  </sta:feature>

[This is an ordered list of feature which determines the order in which scripts are
added to the diff file]

Then including the feature in the directory structure:

  {database.script.path}/database/features/FEATURE1/

Files inside each feature directory will be added to the diff script in a natural 
order e.g.

  a.sql
  b.sql
  c.sql

Or

  1.0.1.sql
  1.2.2.sql
  2.0.0.sql

This project is still a work in progress but it's currently possible to include this
as a build step in a Jenkins automated build so that a diff script can be attached
to each build.

The following is an example using the maven exec plugin:

  <plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>1.2.1</version>
    <executions>
      <execution>
        <id>package-script</id>
        <!-- Don't map to an execute phase for the life-cycle, execute goal independently -->
        <goals>
          <goal>java</goal>
        </goals>
      </execution>
    </executions>
    <configuration>
      <includeProjectDependencies>false</includeProjectDependencies>
      <includePluginDependencies>true</includePluginDependencies>
      <executableDependency>
        <groupId>org.rgiskard</groupId>
        <artifactId>script-build</artifactId>
      </executableDependency>
      <mainClass>org.rgiskard.script_build.BuildScript</mainClass>
      <arguments>
        <argument>-i</argument>
        <argument>-p</argument>
        <argument>/Path/to/test.properties</argument>
      </arguments>
    </configuration>
    <dependencies>
      <dependency>
        <groupId>org.rgiskard</groupId>
        <artifactId>script-build</artifactId>
        <version>0.0.3-SNAPSHOT</version>
        <type>jar</type>
      </dependency>
    </dependencies>
  </plugin>

  [Note that the dependency must also be added to the <dependencies> section of the pom]
