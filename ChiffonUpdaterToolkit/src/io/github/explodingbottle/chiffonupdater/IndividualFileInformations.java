package io.github.explodingbottle.chiffonupdater;

public class IndividualFileInformations {

	private String fileName;
	private String versionName;
	private String featureName;

	public IndividualFileInformations(String fileName, String versionName, String featureName) {
		this.fileName = fileName;
		this.versionName = versionName;
		this.featureName = featureName;
	}

	public String getFileName() {
		return fileName;
	}

	public String getVersionName() {
		return versionName;
	}

	public String getFeatureName() {
		return featureName;
	}

}
