package com.blackducksoftware.integration.hub.buildtool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class DependencyNodeTest {
    @Test
    public void buildingNodeTreeFromRealProject() {
        final String root = "com.blackducksoftware:test-project_2.10:0.1.0-SNAPSHOT";
        final Gav rootGav = fromString(root);

        final Map<Gav, DependencyNode> nodes = new HashMap<>();
        nodes.put(rootGav, new DependencyNode(rootGav, new ArrayList<DependencyNode>()));

        addGavData(nodes, "org.scala-lang:scala-library:2.10.4", "com.blackducksoftware:test-project_2.10:0.1.0-SNAPSHOT");
        addGavData(nodes, "com.blackducksoftware.integration:hub-common:7.3.0", "com.blackducksoftware:test-project_2.10:0.1.0-SNAPSHOT");
        addGavData(nodes, "com.blackducksoftware.integration:phone-home-api:1.5.1", "com.blackducksoftware.integration:hub-common:7.3.0");
        addGavData(nodes, "commons-codec:commons-codec:1.10", "com.blackducksoftware.integration:integration-common:5.2.1",
                "com.blackducksoftware.integration:phone-home-api:1.5.1", "org.apache.httpcomponents:httpclient-osgi:4.5.2",
                "org.apache.httpcomponents:httpclient:4.5.2");
        addGavData(nodes, "com.google.code.gson:gson:2.7", "com.blackducksoftware.integration:phone-home-api:1.5.1",
                "com.blackducksoftware.integration:hub-common:7.3.0");
        addGavData(nodes, "com.squareup.okhttp3:okhttp:3.4.2", "com.blackducksoftware.integration:phone-home-api:1.5.1",
                "com.squareup.okhttp3:okhttp-urlconnection:3.4.2", "com.blackducksoftware.integration:hub-common:7.3.0");
        addGavData(nodes, "com.squareup.okio:okio:1.9.0", "com.squareup.okhttp3:okhttp:3.4.2");
        addGavData(nodes, "com.squareup.okhttp3:okhttp-urlconnection:3.4.2", "com.blackducksoftware.integration:phone-home-api:1.5.1",
                "com.blackducksoftware.integration:hub-common:7.3.0");
        addGavData(nodes, "com.blackducksoftware.integration:integration-common:5.2.1", "com.blackducksoftware.integration:hub-common:7.3.0");
        addGavData(nodes, "org.apache.commons:commons-lang3:3.5", "com.blackducksoftware.integration:integration-common:5.2.1",
                "com.blackducksoftware.integration:phone-home-api:1.5.1");
        addGavData(nodes, "commons-io:commons-io:2.5", "com.blackducksoftware.integration:integration-common:5.2.1",
                "com.github.jsonld-java:jsonld-java:0.8.3");
        addGavData(nodes, "org.slf4j:slf4j-api:1.7.21", "com.blackducksoftware.integration:integration-common:5.2.1", "org.slf4j:jcl-over-slf4j:1.7.21",
                "com.github.jsonld-java:jsonld-java:0.8.3");
        addGavData(nodes, "com.blackducksoftware.bdio:bdio:2.0.1", "com.blackducksoftware.integration:hub-common:7.3.0");
        addGavData(nodes, "com.google.code.findbugs:jsr305:2.0.3", "com.blackducksoftware.bdio:bdio:2.0.1");
        addGavData(nodes, "com.google.guava:guava:19.0", "com.fasterxml.jackson.datatype:jackson-datatype-guava:2.3.3",
                "com.blackducksoftware.bdio:bdio:2.0.1");
        addGavData(nodes, "com.fasterxml.jackson.core:jackson-databind:2.3.3", "com.fasterxml.jackson.datatype:jackson-datatype-guava:2.3.3",
                "com.fasterxml.jackson.datatype:jackson-datatype-joda:2.3.3", "com.github.jsonld-java:jsonld-java:0.8.3",
                "com.blackducksoftware.bdio:bdio:2.0.1");
        addGavData(nodes, "com.fasterxml.jackson.core:jackson-annotations:2.3.0", "com.fasterxml.jackson.datatype:jackson-datatype-joda:2.3.3",
                "com.fasterxml.jackson.core:jackson-databind:2.3.3");
        addGavData(nodes, "com.fasterxml.jackson.datatype:jackson-datatype-guava:2.3.3", "com.blackducksoftware.bdio:bdio:2.0.1");
        addGavData(nodes, "com.fasterxml.jackson.datatype:jackson-datatype-joda:2.3.3", "com.blackducksoftware.bdio:bdio:2.0.1");
        addGavData(nodes, "com.github.jsonld-java:jsonld-java:0.8.3", "com.blackducksoftware.bdio:bdio:2.0.1");
        addGavData(nodes, "com.fasterxml.jackson.core:jackson-core:2.7.4", "com.fasterxml.jackson.datatype:jackson-datatype-guava:2.3.3",
                "com.fasterxml.jackson.datatype:jackson-datatype-joda:2.3.3", "com.github.jsonld-java:jsonld-java:0.8.3",
                "com.fasterxml.jackson.core:jackson-databind:2.3.3");
        addGavData(nodes, "org.apache.httpcomponents:httpclient-osgi:4.5.2", "com.github.jsonld-java:jsonld-java:0.8.3");
        addGavData(nodes, "org.apache.httpcomponents:httpclient:4.5.2", "org.apache.httpcomponents:httpclient-cache:4.5.2",
                "org.apache.httpcomponents:httpclient-osgi:4.5.2", "org.apache.httpcomponents:httpmime:4.5.2", "org.apache.httpcomponents:fluent-hc:4.5.2");
        addGavData(nodes, "org.apache.httpcomponents:httpcore:4.4.4", "org.apache.httpcomponents:httpcore-nio:4.4.4",
                "org.apache.httpcomponents:httpclient:4.5.2",
                "org.apache.httpcomponents:httpcore-osgi:4.4.4");
        addGavData(nodes, "org.apache.httpcomponents:httpmime:4.5.2", "org.apache.httpcomponents:httpclient-osgi:4.5.2");
        addGavData(nodes, "org.apache.httpcomponents:httpclient-cache:4.5.2", "org.apache.httpcomponents:httpclient-osgi:4.5.2");
        addGavData(nodes, "org.apache.httpcomponents:fluent-hc:4.5.2", "org.apache.httpcomponents:httpclient-osgi:4.5.2");
        addGavData(nodes, "org.apache.httpcomponents:httpcore-osgi:4.4.4", "com.github.jsonld-java:jsonld-java:0.8.3");
        addGavData(nodes, "org.apache.httpcomponents:httpcore-nio:4.4.4", "org.apache.httpcomponents:httpcore-osgi:4.4.4");
        addGavData(nodes, "io.reactivex:rxjava:1.1.9", "com.blackducksoftware.bdio:bdio:2.0.1");
        addGavData(nodes, "org.slf4j:jcl-over-slf4j:1.7.21", "com.github.jsonld-java:jsonld-java:0.8.3");
        addGavData(nodes, "joda-time:joda-time:2.9.6", "com.fasterxml.jackson.datatype:jackson-datatype-joda:2.3.3", "com.blackducksoftware.bdio:bdio:2.0.1",
                "com.blackducksoftware.integration:hub-common:7.3.0");

        final DependencyNode rootNode = nodes.get(rootGav);
        System.out.println(rootNode);
    }

    private void addGavData(final Map<Gav, DependencyNode> nodes, final String toAdd, final String... parents) {
        final Gav gavToAdd = fromString(toAdd);
        if (!nodes.containsKey(gavToAdd)) {
            final DependencyNode nodeToAdd = new DependencyNode(gavToAdd, new ArrayList<DependencyNode>());
            nodes.put(gavToAdd, nodeToAdd);
        }

        for (final String parent : parents) {
            final Gav parentGav = fromString(parent);
            if (!nodes.containsKey(parentGav)) {
                final DependencyNode parentNode = new DependencyNode(parentGav, new ArrayList<DependencyNode>());
                nodes.put(parentGav, parentNode);
            }

            nodes.get(parentGav).getChildren().add(nodes.get(gavToAdd));
        }
    }

    private Gav fromString(final String gav) {
        final String[] pieces = gav.split(":");
        return new Gav(pieces[0], pieces[1], pieces[2]);
    }

}
