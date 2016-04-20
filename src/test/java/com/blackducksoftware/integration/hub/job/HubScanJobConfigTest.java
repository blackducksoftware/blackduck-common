/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
package com.blackducksoftware.integration.hub.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class HubScanJobConfigTest {


	@Test
	public void testHubScanJobConfig() {

		final String projectName1 = "projectName1";
		final String version1 = "version1";
		final String phase1 = "phase1";
		final String dist1 = "dist1";
		final String workingDir1 = "workingDir1";
		final int scanMemory1 = 0;
		final boolean shouldGenReport1 = false;
		final int waitTime1 = 0;
		final String target1 = "target1";
		final ImmutableList<String> targets1 = new ImmutableList.Builder<String>().add(target1).build();

		final String projectName2 = "projectName2";
		final String version2 = "version2";
		final String phase2 = "phase2";
		final String dist2 = "dist2";
		final String workingDir2 = "workingDir2";
		final int scanMemory2 = 1342;
		final boolean shouldGenReport2 = false;
		final int waitTime2 = 1234;
		final String target2 = "target2";
		final ImmutableList<String> targets2 = new ImmutableList.Builder<String>().add(target2).build();


		final HubScanJobConfig item1 = new HubScanJobConfig(projectName1, version1, phase1, dist1, workingDir1,
				scanMemory1, shouldGenReport1, waitTime1, targets1);
		final HubScanJobConfig item2 = new HubScanJobConfig(projectName2, version2, phase2, dist2, workingDir2,
				scanMemory2, shouldGenReport2, waitTime2, targets2);
		final HubScanJobConfig item3 = new HubScanJobConfig(projectName1, version1, phase1, dist1, workingDir1,
				scanMemory1, shouldGenReport1, waitTime1, targets1);

		assertEquals(projectName1, item1.getProjectName());
		assertEquals(version1, item1.getVersion());
		assertEquals(phase1, item1.getPhase());
		assertEquals(dist1, item1.getDistribution());
		assertEquals(workingDir1, item1.getWorkingDirectory());
		assertEquals(scanMemory1, item1.getScanMemory());
		assertEquals(shouldGenReport1, item1.isShouldGenerateRiskReport());
		assertEquals(waitTime1, item1.getMaxWaitTimeForBomUpdate());
		assertEquals(targets1, item1.getScanTargetPaths());

		assertEquals(projectName2, item2.getProjectName());
		assertEquals(version2, item2.getVersion());
		assertEquals(phase2, item2.getPhase());
		assertEquals(dist2, item2.getDistribution());
		assertEquals(workingDir2, item2.getWorkingDirectory());
		assertEquals(scanMemory2, item2.getScanMemory());
		assertEquals(shouldGenReport2, item2.isShouldGenerateRiskReport());
		assertEquals(waitTime2, item2.getMaxWaitTimeForBomUpdate());
		assertEquals(waitTime2 * 60 * 1000, item2.getMaxWaitTimeForBomUpdateInMilliseconds());
		assertEquals(targets2, item2.getScanTargetPaths());


		assertTrue(item1.equals(item3));
		assertTrue(!item1.equals(item2));

		EqualsVerifier.forClass(HubScanJobConfig.class).suppress(Warning.STRICT_INHERITANCE).verify();

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());


		final StringBuilder builder = new StringBuilder();
		builder.append("HubScanJobConfig [projectName=");
		builder.append(item1.getProjectName());
		builder.append(", version=");
		builder.append(item1.getVersion());
		builder.append(", phase=");
		builder.append(item1.getPhase());
		builder.append(", distribution=");
		builder.append(item1.getDistribution());
		builder.append(", workingDirectory=");
		builder.append(item1.getWorkingDirectory());
		builder.append(", shouldGenerateRiskReport=");
		builder.append(item1.isShouldGenerateRiskReport());
		builder.append(", maxWaitTimeForBomUpdate=");
		builder.append(item1.getMaxWaitTimeForBomUpdate());
		builder.append(", scanMemory=");
		builder.append(item1.getScanMemory());
		builder.append(", scanTargetPaths=");
		builder.append(item1.getScanTargetPaths());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());

	}
}
