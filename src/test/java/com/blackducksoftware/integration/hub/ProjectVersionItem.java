package com.blackducksoftware.integration.hub;


public class ProjectVersionItem extends
	com.blackducksoftware.integration.hub.item.HubItem {
    private String versionName;
    private String phase;
    private String distribution;
    private String source;

    public String getVersionName() {
	return versionName;
    }

    public String getPhase() {
	return phase;
    }

    public String getDistribution() {
	return distribution;
    }

    public String getSource() {
	return source;
    }

    @Override
    public String toString() {
	return "VersionItem [versionName=" + versionName + ", phase=" + phase
		+ ", distribution=" + distribution + ", source=" + source
		+ ", meta=" + getMeta() + "]";
    }

}