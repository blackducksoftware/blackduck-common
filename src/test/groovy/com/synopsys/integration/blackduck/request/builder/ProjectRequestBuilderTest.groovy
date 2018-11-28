package com.synopsys.integration.blackduck.request.builder

import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionPhaseType
import com.synopsys.integration.blackduck.service.model.ProjectRequestBuilder
import com.synopsys.integration.rest.RestConstants
import org.junit.jupiter.api.Test

import java.text.SimpleDateFormat

import static org.junit.jupiter.api.Assertions.*

class ProjectRequestBuilderTest {
    @Test
    public void testInvalidValues() {
        ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder()
        projectRequestBuilder.setProjectName(null)
        projectRequestBuilder.setProjectLevelAdjustments(null)
        projectRequestBuilder.setProjectOwner(null)
        projectRequestBuilder.setProjectTier(null)
        projectRequestBuilder.setDescription(null)
        projectRequestBuilder.setVersionName(null)
        projectRequestBuilder.setDistribution(null)
        projectRequestBuilder.setPhase(null)
        projectRequestBuilder.setReleaseComments(null)
        projectRequestBuilder.setReleasedOn(null)
        projectRequestBuilder.setVersionNickname(null)

        try {
            projectRequestBuilder.build()
            fail("Should have thrown an exception")
        } catch (IllegalArgumentException e) {
            assertNotNull(e)
        }

        projectRequestBuilder = new ProjectRequestBuilder()
        projectRequestBuilder.setProjectName("FAKE")
        projectRequestBuilder.setProjectLevelAdjustments(false)
        projectRequestBuilder.setProjectOwner("FAKE")
        projectRequestBuilder.setProjectTier(-1)
        projectRequestBuilder.setDescription("FAKE")
        projectRequestBuilder.setVersionName("FAKE")
        projectRequestBuilder.setDistribution("FAKE")
        projectRequestBuilder.setPhase("FAKE")
        projectRequestBuilder.setReleaseComments("FAKE")
        projectRequestBuilder.setReleasedOn("FAKE")
        projectRequestBuilder.setVersionNickname("FAKE")

        try {
            projectRequestBuilder.build()
            fail("Should have thrown an exception")
        } catch (IllegalArgumentException e) {
            assertNotNull(e)
        }
    }

    @Test
    public void testValidValues() {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        final String releasedOn = sdf.format(new Date());

        String projectName = 'Project'
        Boolean projectLevelAdjustments = true
        String projectOwner = 'User'
        Integer projectTier = 2
        String description = 'Project description'
        String versionName = 'Version'
        String distribution = 'inTernal'
        String phase = 'archiVED'
        String releaseComments = 'Comment 1'
        String versionNickname = 'Cobra'

        ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder()
        projectRequestBuilder.setProjectName(projectName)
        projectRequestBuilder.setProjectLevelAdjustments(projectLevelAdjustments)
        projectRequestBuilder.setProjectOwner(projectOwner)
        projectRequestBuilder.setProjectTier(projectTier)
        projectRequestBuilder.setDescription(description)
        projectRequestBuilder.setVersionName(versionName)
        projectRequestBuilder.setDistribution(distribution)
        projectRequestBuilder.setPhase(phase)
        projectRequestBuilder.setReleaseComments(releaseComments)
        projectRequestBuilder.setReleasedOn(releasedOn)
        projectRequestBuilder.setVersionNickname(versionNickname)
        ProjectRequest request = projectRequestBuilder.build()

        assertNotNull(request)
        assertEquals(projectName, request.name)
        assertEquals(projectLevelAdjustments, request.projectLevelAdjustments)
        assertEquals(projectOwner, request.projectOwner)
        assertEquals(projectTier, request.projectTier)
        assertEquals(description, request.description)
        assertNotNull(request.versionRequest)
        assertEquals(versionName, request.versionRequest.versionName)
        assertEquals(ProjectVersionDistributionType.valueOf(distribution.toUpperCase()), request.versionRequest.distribution)
        assertEquals(ProjectVersionPhaseType.valueOf(phase.toUpperCase()), request.versionRequest.phase)
        assertEquals(releaseComments, request.versionRequest.releaseComments)
        assertEquals(sdf.parse(releasedOn), request.versionRequest.releasedOn)
        assertEquals(versionNickname, request.versionRequest.nickname)
    }

    @Test
    public void testConciseValidValues() {
        String projectName = 'Project'
        String versionName = 'Version'

        ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder()
        projectRequestBuilder.setProjectName(projectName)
        projectRequestBuilder.setVersionName(versionName)

        ProjectRequest request = projectRequestBuilder.build()

        assertNotNull(request)
        assertEquals(projectName, request.name)
        assertNull(request.projectLevelAdjustments)
        assertNull(request.projectOwner)
        assertNull(request.projectTier)
        assertNull(request.description)
        assertNotNull(request.versionRequest)
        assertEquals(versionName, request.versionRequest.versionName)
        assertEquals(ProjectVersionDistributionType.EXTERNAL, request.versionRequest.distribution)
        assertEquals(ProjectVersionPhaseType.DEVELOPMENT, request.versionRequest.phase)
        assertNull(request.versionRequest.releaseComments)
        assertNull(request.versionRequest.releasedOn)
        assertNull(request.versionRequest.nickname)
    }
}
