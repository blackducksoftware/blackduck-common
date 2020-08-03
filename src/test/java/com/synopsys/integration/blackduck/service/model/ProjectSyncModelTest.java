package com.synopsys.integration.blackduck.service.model;

import com.synopsys.integration.blackduck.api.generated.enumeration.LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectCloneCategoriesType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.component.ProjectVersionRequest;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.enumeration.ProjectVersionPhaseType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectSyncModelTest {
    @Test
    public void testSyncModelHasAllFieldsForProjectAndVersionRequests() throws Exception {
        Field ignoredFieldsField = ProjectSyncModel.class.getDeclaredField("IGNORED_REQUEST_FIELDS");
        ignoredFieldsField.setAccessible(true);
        Set<Field> ignoredFields = (Set<Field>) ignoredFieldsField.get(null);

        List<Field> allFields = new ArrayList<>();
        allFields.addAll(Arrays.asList(ProjectRequest.class.getDeclaredFields()));
        allFields.addAll(Arrays.asList(ProjectVersionRequest.class.getDeclaredFields()));

        List<Field> fieldsToCheck =
                allFields
                        .stream()
                        .filter(field -> !Modifier.isStatic(field.getModifiers()) && !ignoredFields.contains(field.getName()))
                        .collect(Collectors.toList());

        Map<String, Class<?>> fieldNameToType = new HashMap<>();
        for (Field field : fieldsToCheck) {
            String fieldName = field.getName();
            assertFalse(fieldNameToType.containsKey(fieldName), String.format("Already had %s", fieldName));
            fieldNameToType.put(fieldName, field.getType());
        }

        Map<String, Class<?>> syncModelFields =
                Arrays
                        .stream(ProjectSyncModel.class.getDeclaredFields())
                        .collect(Collectors.toMap(Field::getName, Field::getType));

        for (Map.Entry<String, Class<?>> entry : fieldNameToType.entrySet()) {
            String fieldName = entry.getKey();
            assertTrue(syncModelFields.containsKey(fieldName), String.format("Should contain %s", fieldName));

            Class<?> expectedType = syncModelFields.get(fieldName);
            assertEquals(expectedType, entry.getValue(), String.format("Should match type %s for field %s", expectedType, fieldName));
        }
    }

    @Test
    public void testAllFieldsSetValues() throws Exception {
        Field allFieldsField = ProjectSyncModel.class.getDeclaredField("ALL_FIELDS");
        allFieldsField.setAccessible(true);
        Set<Field> allFields = (Set<Field>) allFieldsField.get(null);

        List<Field> syncModelFieldsToCheck =
                Arrays
                        .stream(ProjectSyncModel.class.getDeclaredFields())
                        .filter(field -> !Modifier.isStatic(field.getModifiers()) && !"fieldsWithSetValues".equals(field.getName()))
                        .collect(Collectors.toList());

        for (Field syncModelField : syncModelFieldsToCheck) {
            assertTrue(allFields.contains(syncModelField), String.format("Should contain %s", syncModelField.getName()));
        }

        Field setFields = ProjectSyncModel.class.getDeclaredField("fieldsWithSetValues");
        setFields.setAccessible(true);
        ProjectSyncModel projectSyncModel = new ProjectSyncModel();
        assertTrue(((Set) setFields.get(projectSyncModel)).isEmpty());

        projectSyncModel.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        projectSyncModel.setDistribution(LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.INTERNAL);
        projectSyncModel.setDescription("desc");
        projectSyncModel.setVersionName("version name");
        projectSyncModel.setName("name");
        projectSyncModel.setCloneCategories(Arrays.asList(ProjectCloneCategoriesType.COMPONENT_DATA));
        projectSyncModel.setCloneFromReleaseUrl("url");
        projectSyncModel.setCustomSignatureEnabled(true);
        projectSyncModel.setNickname("nick");
        projectSyncModel.setProjectLevelAdjustments(false);
        projectSyncModel.setProjectOwner("owner");
        projectSyncModel.setProjectTier(2);
        projectSyncModel.setReleaseComments("release comments");
        projectSyncModel.setReleasedOn(new Date());

        assertFalse(((Set) setFields.get(projectSyncModel)).isEmpty());
        assertEquals(allFields, setFields.get(projectSyncModel));
    }

    @Test
    public void testOnlySetFieldsPopulateView() {
        ProjectView projectView = new ProjectView();
        projectView.setName("test name");
        projectView.setDescription("test description");

        ProjectSyncModel projectSyncModel = new ProjectSyncModel();
        projectSyncModel.setDescription("an override description");
        projectSyncModel.populateProjectView(projectView);

        assertEquals("test name", projectView.getName());
        assertEquals("an override description", projectView.getDescription());
    }

    @Test
    public void testAllFieldsPopulateRequest() {
        Date releasedOn = new Date();

        ProjectSyncModel projectSyncModel = new ProjectSyncModel();
        projectSyncModel.setProjectTier(1);
        projectSyncModel.setNickname("nick");
        projectSyncModel.setProjectOwner("owner href");
        projectSyncModel.setDescription("desc");
        projectSyncModel.setCloneCategories(Arrays.asList(ProjectCloneCategoriesType.COMPONENT_DATA));
        projectSyncModel.setReleaseComments("released!");
        projectSyncModel.setDistribution(LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.INTERNAL);
        projectSyncModel.setReleasedOn(releasedOn);
        projectSyncModel.setProjectLevelAdjustments(true);
        projectSyncModel.setCloneFromReleaseUrl("version href");
        projectSyncModel.setCustomSignatureEnabled(true);
        projectSyncModel.setName("project name");
        projectSyncModel.setVersionName("version name");
        projectSyncModel.setPhase(ProjectVersionPhaseType.DEVELOPMENT);

        ProjectRequest projectRequest = projectSyncModel.createProjectRequest();
        assertEquals("project name", projectRequest.getName());
        assertEquals("desc", projectRequest.getDescription());
        assertEquals("owner href", projectRequest.getProjectOwner());
        assertEquals(new Integer(1), projectRequest.getProjectTier());
        assertEquals(true, projectRequest.getProjectLevelAdjustments());
        assertEquals(Arrays.asList(ProjectCloneCategoriesType.COMPONENT_DATA), projectRequest.getCloneCategories());
        assertEquals(true, projectRequest.getCustomSignatureEnabled());
        assertEquals("version name", projectRequest.getVersionRequest().getVersionName());
        assertEquals("version href", projectRequest.getVersionRequest().getCloneFromReleaseUrl());
        assertEquals("nick", projectRequest.getVersionRequest().getNickname());
        assertEquals("released!", projectRequest.getVersionRequest().getReleaseComments());
        assertEquals(releasedOn, projectRequest.getVersionRequest().getReleasedOn());
        assertEquals(ProjectVersionPhaseType.DEVELOPMENT, projectRequest.getVersionRequest().getPhase());
        assertEquals(LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.INTERNAL, projectRequest.getVersionRequest().getDistribution());
    }

}
