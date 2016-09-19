package com.blackducksoftware.integration.hub.policy.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.policy.PolicyExpression;
import com.blackducksoftware.integration.hub.api.policy.PolicyExpressions;
import com.blackducksoftware.integration.hub.api.policy.PolicyRuleConditionEnum;

public class PolicyExpressionsTest {

	@Test
	public void testPolicyExpressions() {
		final String name1 = "name1";

		final String name2 = "name2";

		final PolicyExpression expression1 = new PolicyExpression(name1, null, null);

		final List<PolicyExpression> expressions1 = new ArrayList<>();
		expressions1.add(expression1);

		final PolicyExpression expression2 = new PolicyExpression(name2, null, null);

		final List<PolicyExpression> expressions2 = new ArrayList<>();
		expressions2.add(expression2);

		final String operator1 = "operator1";

		final String operator2 = "operator2";

		final PolicyExpressions item1 = new PolicyExpressions(operator1, expressions1);
		final PolicyExpressions item2 = new PolicyExpressions(operator2, expressions2);
		final PolicyExpressions item3 = new PolicyExpressions(operator1, expressions1);

		assertEquals(operator1, item1.getOperator());
		assertEquals(expressions1, item1.getExpressions());

		assertEquals(operator2, item2.getOperator());
		assertEquals(expressions2, item2.getExpressions());

		assertTrue(!item1.equals(item2));
		assertTrue(item1.equals(item3));

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());
	}

	@Test
	public void testHasOnlyProjectLevelConditions() {
		final PolicyExpressions item1 = new PolicyExpressions(null, null);
		assertTrue(item1.hasOnlyProjectLevelConditions());

		final List<PolicyExpression> expressions = new ArrayList<>();

		final PolicyExpressions item2 = new PolicyExpressions(null, expressions);
		assertTrue(item2.hasOnlyProjectLevelConditions());

		String name = null;

		final PolicyExpression expression1 = new PolicyExpression(name, null, null);
		expressions.add(expression1);

		final PolicyExpressions item3 = new PolicyExpressions(null, expressions);
		assertTrue(item3.hasOnlyProjectLevelConditions());

		name = "";

		final PolicyExpression expression2 = new PolicyExpression(name, null, null);
		expressions.add(expression2);

		final PolicyExpressions item4 = new PolicyExpressions(null, expressions);
		assertTrue(item4.hasOnlyProjectLevelConditions());

		name = "gobbletygook";

		final PolicyExpression expression3 = new PolicyExpression(name, null, null);
		expressions.add(expression3);

		final PolicyExpressions item5 = new PolicyExpressions(null, expressions);
		assertTrue(item5.hasOnlyProjectLevelConditions());

		name = PolicyRuleConditionEnum.PROJECT_TIER.name();

		final PolicyExpression expression4 = new PolicyExpression(name, null, null);
		expressions.add(expression4);

		final PolicyExpressions item6 = new PolicyExpressions(null, expressions);
		assertTrue(item6.hasOnlyProjectLevelConditions());

		name = PolicyRuleConditionEnum.NEWER_VERSIONS_COUNT.name();

		final PolicyExpression expression5 = new PolicyExpression(name, null, null);
		expressions.add(expression5);

		final PolicyExpressions item7 = new PolicyExpressions(null, expressions);
		assertTrue(!item7.hasOnlyProjectLevelConditions());
	}

}
