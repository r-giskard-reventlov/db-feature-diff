package org.rgiskard.script_build.dao;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;

@Component
public class LogDaoImpl implements LogDao {

	private @Autowired JdbcOperations jdbcTemplate;

	/**
	 * Provides a list of features which the database has implemented
	 */
	public ImmutableList<Map<String, Object>> features() {
		final String query = "select * from feature";

		try {
			ImmutableList<Map<String, Object>> features =
				ImmutableList.copyOf(
					jdbcTemplate.queryForList(query)
				);
			
			return features;
		}
		catch(BadSqlGrammarException e) {
			throw new RuntimeException("Could not access the feature list. It's possible that the issue is because the audit tables do not exist; These can be created using the initialise option (-i). " + e.getMessage());
		}
	}

	/**
	 * This method creates the tables used to track the features which have been
	 * implemented
	 */
	@Transactional
	public void deployLogTables() {
		
		final String queryExists =
			"select table_name from user_tables where upper(table_name) = ?";
		
		final String queryCreateTable =
			"create table feature ( "
			+ "feature_id varchar2(20) not null,"
			+ "added_date date default sysdate not null"
			+ ")";

		// Firstly attempt to find the table
		List<Map<String,Object>> rows = 
			jdbcTemplate.queryForList(
				queryExists, 
				"FEATURE"
			);
		
		if(rows.isEmpty()) {
			jdbcTemplate.execute(queryCreateTable);
			Logger.getLogger(LogDaoImpl.class.getName()).log(Level.INFO, "Audit tables created.");
		}
		else {
			Logger.getLogger(LogDaoImpl.class.getName()).log(Level.INFO, "Audit tables already exist.");
		}
	}

}
