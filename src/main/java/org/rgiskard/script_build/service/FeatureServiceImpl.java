package org.rgiskard.script_build.service;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.rgiskard.script_build.dao.FileDao;
import org.rgiskard.script_build.dao.LogDao;
import org.rgiskard.script_build.dao.LogDaoImpl;
import org.rgiskard.script_build.dao.XmlDao;
import org.rgiskard.script_build.model.Feature;
import org.rgiskard.script_build.model.Features;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

@Component
public class FeatureServiceImpl implements FeatureService {

	private @Autowired FileDao fileDao;
	private @Autowired LogDao logDao;
	private @Autowired XmlDao xmlDao;
	
	public Features sourceFeatures() {
		
		// Load ordered features list
		org.rgiskard.script_build.jaxbentities.Features features = xmlDao.features();
		
		// Load into model features (seems like overkill but might be useful long term
		Features modelFeatures =
			new Features(
				ImmutableList.copyOf(
					Iterables.transform(
						features.getFeature(),
						new Function<org.rgiskard.script_build.jaxbentities.Feature, Feature>() {
							public Feature apply(final org.rgiskard.script_build.jaxbentities.Feature feature) {
								return
									new Feature(feature.getName());
							}
						}
					)
				)
			);
		
		// Make sure script directories exist in source tree
		Boolean exists = 
			fileDao.checkForDirecotires(
				ImmutableList.copyOf(
					Iterables.transform(
						modelFeatures.getFeatures(), 
						new Function<Feature,String>() {
							public String apply(Feature feature) {
								return feature.getFeatureName();
							}
						}
					)
				)
			);
		
		if(!exists) {
			throw new RuntimeException("Cannot proceed... features.xml indicates a feature which is not present in source code.");
		}
		
		return modelFeatures;
	}
	
	public Features implementedFeatures() {
		
		return
			new Features(
				ImmutableList.copyOf(
					Iterables.transform(
						logDao.features(),
						new Function<Map<String, Object>, Feature>() {
							public Feature apply(Map<String, Object> row) {
								return new Feature((String) row.get("feature_id"));
							}
						}
					)
				)
			);
	}
	
	public String createScript(final Features missingFeatures) {
		if(missingFeatures.getFeatures().size() == 0) {
			return "";
		}
		StringWriter script = new StringWriter();
		for(Feature feature : missingFeatures.getFeatures()) {
			Logger.getLogger(FeatureServiceImpl.class.getName()).log(Level.INFO, "Missing feature [" + feature.getFeatureName() + "] being added to script.");
			script.append("-------- Start Feature: " + feature.getFeatureName() + " --------\n");
			
			// Obtain ordered list of features
			ImmutableList<File> featureFiles =
				fileDao.directoryFiles(feature.getFeatureName());
			
			for(File featureFile : featureFiles) {
				// Add to String buffer
				script.append(fileDao.fileContent(featureFile));
				script.append("\n");
			}
			script.append("-- Update audit to add feature\n");
			script.append("insert into feature (feature_id, added_date) values (\"" + feature.getFeatureName() + "\", sysdate);");
			script.append("\n");
			script.append("-------- End Feature: " + feature.getFeatureName() + " --------\n\n");
		}
		
		return script.toString();
	}
}
