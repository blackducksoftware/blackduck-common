/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.api.policy;

import java.util.UUID;

import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.util.HubUrlParser;

public class PolicyRule extends HubItem {
	public static final String POLICY_RULES_URL_IDENTIFIER = "policy-rules";

	private final String name;
	private final String description;
	private final Boolean enabled;
	private final Boolean overridable;
	private final PolicyExpressions expression;
	private final String createdAt;
	private final String createdBy;
	private final String updatedAt;
	private final String updatedBy;

	public PolicyRule(final MetaInformation meta, final String name, final String description, final Boolean enabled,
			final Boolean overridable, final PolicyExpressions expression, final String createdAt,
			final String createdBy, final String updatedAt, final String updatedBy) {
		super(meta);
		this.name = name;
		this.description = description;
		this.enabled = enabled;
		this.overridable = overridable;
		this.expression = expression;
		this.createdAt = createdAt;
		this.createdBy = createdBy;
		this.updatedAt = updatedAt;
		this.updatedBy = updatedBy;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public Boolean getOverridable() {
		return overridable;
	}

	public PolicyExpressions getExpression() {
		return expression;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public DateTime getUpdatedAtTime() {
		return getDateTime(updatedAt);
	}

	public DateTime getCreatedAtTime() {
		return getDateTime(createdAt);
	}

	@Deprecated
	public UUID getPolicyRuleId() throws MissingUUIDException {
		if (getMeta() == null || getMeta().getHref() == null) {
			return null;
		}
		return HubUrlParser.getUUIDFromURLString(POLICY_RULES_URL_IDENTIFIER, getMeta().getHref());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + ((createdBy == null) ? 0 : createdBy.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((enabled == null) ? 0 : enabled.hashCode());
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((overridable == null) ? 0 : overridable.hashCode());
		result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
		result = prime * result + ((updatedBy == null) ? 0 : updatedBy.hashCode());
		result = prime * result + ((getMeta() == null) ? 0 : getMeta().hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PolicyRule)) {
			return false;
		}
		final PolicyRule other = (PolicyRule) obj;
		if (getMeta() == null) {
			if (other.getMeta() != null) {
				return false;
			}
		} else if (!getMeta().equals(other.getMeta())) {
			return false;
		}
		if (createdAt == null) {
			if (other.createdAt != null) {
				return false;
			}
		} else if (!createdAt.equals(other.createdAt)) {
			return false;
		}
		if (createdBy == null) {
			if (other.createdBy != null) {
				return false;
			}
		} else if (!createdBy.equals(other.createdBy)) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (enabled == null) {
			if (other.enabled != null) {
				return false;
			}
		} else if (!enabled.equals(other.enabled)) {
			return false;
		}
		if (expression == null) {
			if (other.expression != null) {
				return false;
			}
		} else if (!expression.equals(other.expression)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (overridable == null) {
			if (other.overridable != null) {
				return false;
			}
		} else if (!overridable.equals(other.overridable)) {
			return false;
		}
		if (updatedAt == null) {
			if (other.updatedAt != null) {
				return false;
			}
		} else if (!updatedAt.equals(other.updatedAt)) {
			return false;
		}
		if (updatedBy == null) {
			if (other.updatedBy != null) {
				return false;
			}
		} else if (!updatedBy.equals(other.updatedBy)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("PolicyRule [name=");
		builder.append(name);
		builder.append(", description=");
		builder.append(description);
		builder.append(", enabled=");
		builder.append(enabled);
		builder.append(", overridable=");
		builder.append(overridable);
		builder.append(", expression=");
		builder.append(expression);
		builder.append(", createdAt=");
		builder.append(createdAt);
		builder.append(", createdBy=");
		builder.append(createdBy);
		builder.append(", updatedAt=");
		builder.append(updatedAt);
		builder.append(", updatedBy=");
		builder.append(updatedBy);
		builder.append("]");
		return builder.toString();
	}

}
