package org.rgiskard.script_build.service;

import org.rgiskard.script_build.model.Features;

public interface FeatureService {
	public Features sourceFeatures();
	public Features implementedFeatures();
	public String createScript(final Features missingFeatures);
}
