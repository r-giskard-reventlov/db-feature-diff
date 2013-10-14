package org.rgiskard.script_build.dao;

import java.util.Map;

import com.google.common.collect.ImmutableList;

public interface LogDao {
	public ImmutableList<Map<String, Object>> features(); 
	public void deployLogTables();
}