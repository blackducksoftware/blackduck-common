package com.blackducksoftware.integration.hub.api.report;

public enum ReportCategoriesEnum {
	VERSION, CODE_LOCATIONS, COMPONENTS, SECURITY, FILES, UNKNOWN_CATEGORY;

	public static ReportCategoriesEnum getReportCategoriesEnum(final String reportCategory) {
		if (reportCategory == null) {
			return ReportCategoriesEnum.UNKNOWN_CATEGORY;
		}

		ReportCategoriesEnum reportCategoriesEnum;
		try {
			reportCategoriesEnum = ReportCategoriesEnum.valueOf(reportCategory.toUpperCase());
		} catch (final IllegalArgumentException e) {
			reportCategoriesEnum = UNKNOWN_CATEGORY;
		}

		return reportCategoriesEnum;
	}

}
