package com.blackducksoftware.integration.hub.policy.api;

import java.util.List;

public class PolicyExpressions {
	private final String operator;
	private final List<PolicyExpression> expressions;

	public PolicyExpressions(final String operator, final List<PolicyExpression> expressions) {
		this.operator = operator;
		this.expressions = expressions;
	}

	public String getOperator() {
		return operator;
	}

	public List<PolicyExpression> getExpressions() {
		return expressions;
	}

	public boolean hasOnlyProjectLevelConditions() {
		boolean hasNonProjectLevelCondition = false;
		if (getExpressions() != null && !getExpressions().isEmpty()) {
			for (final PolicyExpression expression : getExpressions()) {
				final PolicyRuleConditionEnum condition = expression.getNameConditionEnum();
				if (condition == PolicyRuleConditionEnum.UNKNOWN_RULE_CONDTION) {
					continue;
				}
				if (condition != PolicyRuleConditionEnum.PROJECT_TIER
						&& condition != PolicyRuleConditionEnum.VERSION_PHASE
						&& condition != PolicyRuleConditionEnum.VERSION_DISTRIBUTION) {
					hasNonProjectLevelCondition = true;
				}
			}
		}

		return !hasNonProjectLevelCondition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expressions == null) ? 0 : expressions.hashCode());
		result = prime * result + ((operator == null) ? 0 : operator.hashCode());
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
		if (!(obj instanceof PolicyExpressions)) {
			return false;
		}
		final PolicyExpressions other = (PolicyExpressions) obj;
		if (expressions == null) {
			if (other.expressions != null) {
				return false;
			}
		} else if (!expressions.equals(other.expressions)) {
			return false;
		}
		if (operator == null) {
			if (other.operator != null) {
				return false;
			}
		} else if (!operator.equals(other.operator)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("PolicyExpressions [operator=");
		builder.append(operator);
		builder.append(", expressions=");
		builder.append(expressions);
		builder.append("]");
		return builder.toString();
	}


}
