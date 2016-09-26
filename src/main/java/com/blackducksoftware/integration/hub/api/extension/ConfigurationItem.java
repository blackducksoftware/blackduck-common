package com.blackducksoftware.integration.hub.api.extension;

import java.util.List;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ConfigurationItem extends HubItem {

	private final String name;
	private final OptionTypeEnum optionType;
	private final String title;
	private final boolean required;
	private final boolean singleValue;
	private final String description;
	private final List<OptionItem> options;
	private final List<String> defaultValue;

	public ConfigurationItem(final MetaInformation meta, final String name, final OptionTypeEnum optionType,
			final String title, final boolean required, final boolean singleValue, final String description,
			final List<OptionItem> options, final List<String> defaultValue) {
		super(meta);
		this.name = name;
		this.optionType = optionType;
		this.title = title;
		this.required = required;
		this.singleValue = singleValue;
		this.description = description;
		this.options = options;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	public OptionTypeEnum getOptionType() {
		return optionType;
	}

	public String getTitle() {
		return title;
	}

	public boolean isRequired() {
		return required;
	}

	public boolean isSingleValue() {
		return singleValue;
	}

	public String getDescription() {
		return description;
	}

	public List<OptionItem> getOptions() {
		return options;
	}

	public List<String> getDefaultValue() {
		return defaultValue;
	}
}
