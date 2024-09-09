package com.blackduck.integration.blackduck.bdio2.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.blackduck.integration.blackduck.bdio2.util.Bdio2Factory;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackducksoftware.bdio2.model.Component;
import com.blackducksoftware.bdio2.model.Project;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.dependency.ProjectDependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;

class Bdio2FactoryTest {
    @Test
    void testCreateAndLinkComponents() {
        ExternalIdFactory externalIdFactory = new ExternalIdFactory();
        String rootProjectGroup = "testRootProjectGroup";
        String rootProjectName = "testRootProjectName";
        String rootProjectVersion = "testRootProjectVersion";

        String subProjectGroup = "testSubProjectGroup";
        String subProjectName = "testSubProjectName";
        String subProjectVersion = "testSubProjectVersion";

        String compGroup = "testCompGroup";
        String compName = "testCompName";
        String compVersion = "testCompVersion";

        ExternalId rootProjectExternalId = externalIdFactory.createMavenExternalId(rootProjectGroup, rootProjectName, rootProjectVersion);
        ExternalId subProjectExternalId = externalIdFactory.createMavenExternalId(subProjectGroup, subProjectName, subProjectVersion);
        ExternalId componentExternalId = externalIdFactory.createMavenExternalId(compGroup, compName, compVersion);

        Bdio2Factory bdio2Factory = new Bdio2Factory();
        Project rootProject = bdio2Factory.createProject(rootProjectExternalId, true);

        DependencyGraph dependencyGraph = Mockito.mock(DependencyGraph.class);
        Set<Dependency> dependencies = new HashSet<>();
        ProjectDependency subProjectDependency = new ProjectDependency(subProjectName, subProjectVersion, subProjectExternalId);
        Dependency componentDependency = new Dependency(componentExternalId);
        dependencies.add(subProjectDependency);
        dependencies.add(componentDependency);
        Mockito.when(dependencyGraph.getDirectDependencies()).thenReturn(dependencies);

        Pair<List<Project>, List<Component>> results = bdio2Factory.createAndLinkComponents(dependencyGraph, rootProject);

        assertEquals(1, results.getLeft().size());
        assertEquals("http:maven/" + subProjectGroup + "/" + subProjectName + "/" + subProjectVersion, results.getLeft().get(0).id());
        assertEquals(1, results.getRight().size());
        assertEquals("http:maven/" + compGroup + "/" + compName + "/" + compVersion, results.getRight().get(0).id());
    }
}
