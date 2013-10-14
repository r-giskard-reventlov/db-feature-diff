package org.rgiskard.script_build.model;

public class Feature {

	private final String featureName;

	public Feature(String featureName) {
		this.featureName = featureName;
	}

	public String getFeatureName() {
		return featureName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((featureName == null) ? 0 : featureName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Feature other = (Feature) obj;
		if (featureName == null) {
			if (other.featureName != null)
				return false;
		} else if (!featureName.equals(other.featureName))
			return false;
		return true;
	}
	
}
