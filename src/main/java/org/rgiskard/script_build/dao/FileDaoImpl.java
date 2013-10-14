package org.rgiskard.script_build.dao;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

@Component
public class FileDaoImpl implements FileDao {

	private final File scriptPath;
	private final File outputFile;
	
	@Autowired
	public FileDaoImpl(
			@Value("${project.path}") String projectPath, 
			@Value("${script.defaultPath}") String defaultProjectRelativeScriptPath, 
			@Value("${script.output}") String outputFile) {
		
		this.scriptPath = new File(projectPath + (defaultProjectRelativeScriptPath.isEmpty() ? "/src/main/resources/database" : defaultProjectRelativeScriptPath));
		this.outputFile = new File(outputFile);
		
		// Assertions
		if(!this.scriptPath.isDirectory()) {
			throw new RuntimeException("The script provided is not a directory: " + this.scriptPath.getAbsolutePath());
		}
	}
 	
	public void writeToFileSystem(String script) {
		try {
			FileUtils.writeStringToFile(this.outputFile, script);
		} 
		catch (IOException e) {
			throw new RuntimeException("Could not write the file content to file [" + this.outputFile.getAbsolutePath() + "] in the file system.\n" + e.getMessage());
		}
		
	}

	public CharSequence fileContent(final File file) {
		try {
			return FileUtils.readFileToString(file);
		} 
		catch (IOException e) {
			throw new RuntimeException("Could not read the file content for file [" + file.getAbsolutePath() + "].\n" + e.getMessage());
		}
	}

	/**
	 * Provides a List of files ordered naturally
	 * 
	 * @param feature
	 * @return
	 */
	public ImmutableList<File> directoryFiles(final String topDirectory) {
		// Assertions
		final File directory = new File(this.scriptPath + File.separator + "features" +  File.separator + topDirectory);
		if(!directory.isDirectory()) {
			throw new RuntimeException("Could not find the directory in the file system : " + directory.getAbsolutePath());
		}
		
		return 
			ImmutableList.copyOf(
				// Make sure the files are ordered using the default ordering for a file (See File's comparable implementation)
				Ordering.<File>natural().sortedCopy(
					// Transform each of the filtered file into a File instance
					Iterables.transform(
						// Filter non-files (e.g. other directories)
						Iterables.filter(
							Lists.newArrayList(directory.list()),
							new Predicate<String>() {
								public boolean apply(final String file) {
									File myFile = new File(scriptPath + File.separator + "features" + File.separator + topDirectory + File.separator + file);
									return myFile.isFile();
								}
							}
						),
						new Function<String, File>() {
							public File apply(final String file) {
								return new File(scriptPath + File.separator + "features" + File.separator + topDirectory + File.separator + file);
							}
						}
					)
				)
			);
	}

	public Boolean checkForDirecotires(ImmutableList<String> directoryNames) {
		final String featureDirectoryPath = this.scriptPath + File.separator + "features" + File.separator;
		for(String directoryName : directoryNames) {
			File file = new File(featureDirectoryPath + directoryName);
			if(!file.exists() || !file.isDirectory()) {
				Logger.getLogger(LogDaoImpl.class.getName()).log(Level.SEVERE, "A supplied directory does not exist in the file system [" + file.getAbsolutePath() + "].");
				return false;
			}
		}
		return true;
	}

}
