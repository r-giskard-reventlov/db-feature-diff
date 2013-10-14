package org.rgiskard.script_build.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

public class Features {

	private final ImmutableList<Feature> features;

	public Features(ImmutableList<Feature> features) {
		this.features = features;
	}
	
	/**
	 * Provides a List of all features in this Features List
	 * which are not in the provided List of Features
	 * 
	 * @param otherFeatures
	 * @return
	 */
	public Features missingFeatures(Features otherFeatures) {
		return
			new Features(
				ImmutableList.copyOf(
					Sets.difference(
						Sets.newHashSet(this.features), 
						Sets.newHashSet(otherFeatures.getFeatures()))
					)
				);
	}

	public ImmutableList<Feature> getFeatures() {
		return features;
	}
	
	
	
}
