package org.rgiskard.script_build;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import org.rgiskard.script_build.dao.FileDao;
import org.rgiskard.script_build.dao.LogDao;
import org.rgiskard.script_build.model.Feature;
import org.rgiskard.script_build.model.Features;
import org.rgiskard.script_build.service.FeatureService;

@Component
public class BuildScript 
{
	private @Autowired LogDao logDao;
	private @Autowired FileDao fileDao;
	private @Autowired FeatureService featureService;
	
	public BuildScript() {}
	
	public void generateAuditObjects() {
		logDao.deployLogTables();
	}
	
	public String diff() {
		// Obtain List of Features in source tree
		Features sourceFeatures =
			featureService.sourceFeatures();

		for(Feature sourceFeature : sourceFeatures.getFeatures()) {
			Logger.getLogger(BuildScript.class.getName()).log(Level.INFO, "Found SOURCE feature [" + sourceFeature.getFeatureName() + "].");
		}
		
		// Obtain List of Features in database
		Features implementedFeatures =
			featureService.implementedFeatures();
		
		for(Feature implementedFeature : implementedFeatures.getFeatures()) {
			Logger.getLogger(BuildScript.class.getName()).log(Level.INFO, "Found IMPLEMENTED feature [" + implementedFeature.getFeatureName() + "].");
		}
		
		// Determine the missing features which need to be added to the output script
		Features missingFeatures =
			sourceFeatures.missingFeatures(implementedFeatures);
		
		for(Feature missingFeature : missingFeatures.getFeatures()) {
			Logger.getLogger(BuildScript.class.getName()).log(Level.INFO, "DIFF feature [" + missingFeature.getFeatureName() + "].");
		}
		
		// Differences
		return featureService.createScript(missingFeatures);
	}
	
	private void writeDifference(String diff) {
		fileDao.writeToFileSystem(diff);
	}

	public static void main(String[] args) throws ParseException
    {
		final Options options = availableOptions();
    	final String usage = "Usage: java -jar <jar file> [-p <porperty file> (property file)] [-i (initialise audot tables)] [-e (ececute)] [-u (database username)] [-x (database password] [-l (database location)] [-o (output file path)] [-d (override script path)] [-t (project path)]";
    	
    	// Parse the command line arguments
    	CommandLineParser parser = new BasicParser();
    	CommandLine cmd = parser.parse(options, args);
    	final String propertyFileLocation = cmd.getOptionValue("p");
    	final Boolean hasInitialiseOption = cmd.hasOption("i") ;
    	final Boolean hasExecuteOption = cmd.hasOption("e");
    	
    	if(!cmd.hasOption("p")) {
    		if(!cmd.hasOption("u") || !cmd.hasOption("x") || !cmd.hasOption("l") || !cmd.hasOption("o") || !cmd.hasOption("t")) {
    			throw new RuntimeException("\n\nCorrect arguments were not supplied. \n" + usage + "\n\n");
    		}
    		else {
    			Logger.getLogger(BuildScript.class.getName()).log(Level.INFO, "Using database: " + cmd.getOptionValue("l"));
    			
    	        // Override properties if no property file is specified
    			System.setProperty("script.database.url", cmd.getOptionValue("l"));
    			System.setProperty("script.database.username", cmd.getOptionValue("u"));
    			System.setProperty("script.database.password", cmd.getOptionValue("x"));
    			System.setProperty("project.path", cmd.getOptionValue("t"));
    			System.setProperty("script.defaultPath", (cmd.hasOption("d") ? cmd.getOptionValue("d") : ""));
        		System.setProperty("script.output", cmd.getOptionValue("o"));
        		
    		}
    		// Easiest way to initialise the proprty placeholder with no properties file
    		System.setProperty("location", "classpath:beans/default.properties");
    	}
    	else {
    		// Picked up by Spring and used to configure beans as context is loaded
            System.setProperty("location", "file: " + propertyFileLocation);
    	}

    	
    	@SuppressWarnings("resource")
		ApplicationContext ctx = new ClassPathXmlApplicationContext("beans/BuildScript.xml");
        BuildScript bs = (BuildScript) ctx.getBean("buildScript");
        
        // Initialise the database audit tables if requested
    	if(hasInitialiseOption) {
    		bs.generateAuditObjects();
    	}
    	
    	// Get the diff between source and database
    	String diff = bs.diff();
    	
    	// Write diff to file for audit
    	bs.writeDifference(diff);
    	
    	// Execute the diff against the database
    	if(hasExecuteOption) {
    		// Not implemented yet
    	}
    }
	
	private static Options availableOptions() {
		Options options = new Options();
		options.addOption("p", true, "location of the properties file");
		options.addOption("i", false, "flag to indicate that the audit tables are to be created");
		options.addOption("u", true, "database username [only used when either properties file is not supplied or the properties file does not include property]");
		options.addOption("x", true, "database password [only used when either properties file is not supplied or the properties file does not include property]");
		options.addOption("l", true, "database location [only used when either properties file is not supplied or the properties file does not include property]");
		options.addOption("o", true, "output file location [only used when either properties file is not supplied or the properties file does not include property]");
		options.addOption("t", true, "project path [only used when either properties file is not supplied or the properties file does not include property]");
		options.addOption("d", true, "override default feature script directory [only used when either properties file is not supplied or the properties file does not include property]");
		
		return options;
	}
	
}
	