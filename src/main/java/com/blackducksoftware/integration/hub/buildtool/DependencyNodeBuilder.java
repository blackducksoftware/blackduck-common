/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.hub.buildtool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;

public class DependencyNodeBuilder {

    private final Gav rootGav;

    private final Map<Gav, List<Gav>> nodeMap = new HashMap<>();

    public DependencyNodeBuilder(final Gav rootGav) {
        this.rootGav = rootGav;
    }

    public void addNodeWithChildren(final Gav gav, final List<Gav> childGavs) throws HubIntegrationException {
        if (gav == null) {
            throw new HubIntegrationException("The gav was null");
        }
        if (childGavs == null) {
            throw new HubIntegrationException("The list of child gav's was null.");
        }
        addToMap(gav, childGavs);
    }

    public void addNodeWithChild(final Gav gav, final Gav childGav) throws HubIntegrationException {
        if (gav == null) {
            throw new HubIntegrationException("The gav was null");
        }
        if (childGav == null) {
            throw new HubIntegrationException("The child Gav was null.");
        }
        final List<Gav> childGavs = new ArrayList<>();
        childGavs.add(childGav);
        addToMap(gav, childGavs);
    }

    public void addNode(final Gav gav) throws HubIntegrationException {
        if (gav == null) {
            throw new HubIntegrationException("The gav was null");
        }
        final List<Gav> childGavs = new ArrayList<>();
        addToMap(gav, childGavs);
    }

    private void addToMap(final Gav gav, final List<Gav> childGavs) {
        if (nodeMap.containsKey(gav)) {
            final List<Gav> children = nodeMap.get(gav);
            children.addAll(childGavs);
            nodeMap.put(gav, children);
        } else {
            nodeMap.put(gav, childGavs);
        }
        if (!childGavs.isEmpty()) {
            for (final Gav childGav : childGavs) {
                if (!nodeMap.containsKey(childGav)) {
                    nodeMap.put(childGav, new ArrayList<Gav>());
                }
            }
        }
    }

    public DependencyNode buildRootNode() {
        final List<DependencyNode> children = getChildren(rootGav);
        return new DependencyNode(rootGav, children);
    }

    private List<DependencyNode> getChildren(final Gav gav) {
        final List<DependencyNode> childrenNodes = new ArrayList<>();
        final List<Gav> childrenGavs = nodeMap.get(gav);
        if (childrenGavs == null) {
            System.out.println("WTF GOT NULL");
        }
        if (!childrenGavs.isEmpty()) {
            for (final Gav childGav : childrenGavs) {
                final List<DependencyNode> currentChildrenNodes = getChildren(childGav);
                final DependencyNode childNode = new DependencyNode(childGav, currentChildrenNodes);
                childrenNodes.add(childNode);
            }
        }
        return childrenNodes;
    }

}
