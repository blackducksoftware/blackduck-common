package com.blackducksoftware.integration.hub.api.extension;

public class OptionItem {
	private final String name;
	private final String title;

	public OptionItem(final String name, final String title) {
		this.name = name;
		this.title = title;
	}

	public String getName() {
		return name;
	}

	public String getTitle() {
		return title;
	}
}
