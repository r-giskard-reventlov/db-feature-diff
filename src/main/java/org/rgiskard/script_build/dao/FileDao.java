package org.rgiskard.script_build.dao;

import java.io.File;

import com.google.common.collect.ImmutableList;

public interface FileDao {
	public ImmutableList<File> directoryFiles(final String topDirectory);
	public CharSequence fileContent(final File file);
	public void writeToFileSystem(String script);
	public Boolean checkForDirecotires(ImmutableList<String> directoryNames);
}
