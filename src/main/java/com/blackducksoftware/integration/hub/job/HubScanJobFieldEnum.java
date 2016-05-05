package com.blackducksoftware.integration.hub.job;

public enum HubScanJobFieldEnum {

	PROJECT("hubProject"),
	VERSION("hubVersion"),
	PHASE("hubPhase"),
	DISTRIBUTION("hubDistribution"),
	GENERATE_RISK_REPORT("shouldGenerateRiskReport"),
	MAX_WAIT_TIME_FOR_BOM_UPDATE("maxWaitTimeForBomUpdate"),
	SCANMEMORY("hubScanMemory"),
	TARGETS("hubTargets"),
	FAIL_ON_POLICY_VIOLATION("failOnPolicyViolation");

	private String key;

	private HubScanJobFieldEnum(final String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

}
