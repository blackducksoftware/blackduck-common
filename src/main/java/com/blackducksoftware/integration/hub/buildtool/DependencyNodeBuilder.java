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
    final Map<Gav, DependencyNode> nodes = new HashMap<>();

    final DependencyNode root;

    public DependencyNodeBuilder(final Gav rootGav) {
        root = new DependencyNode(rootGav, new ArrayList<DependencyNode>());
        nodes.put(rootGav, root);
    }

    public void addNodeWithChildren(final Gav gav, final List<Gav> childGavs) throws HubIntegrationException {
        addNodesToMap(gav, childGavs);
    }

    public void addNodeWithChild(final Gav gav, final Gav childGav) throws HubIntegrationException {
    }

    public void addNode(final Gav gav) throws HubIntegrationException {
    }

    public DependencyNode buildRootNode() {
        return root;
    }

    private void addNodesToMap(final Gav toAdd, final List<Gav> parents) {
        if (!nodes.containsKey(toAdd)) {
            final DependencyNode nodeToAdd = new DependencyNode(toAdd, new ArrayList<DependencyNode>());
            nodes.put(toAdd, nodeToAdd);
        }

        for (final Gav parent : parents) {
            if (!nodes.containsKey(parent)) {
                final DependencyNode parentNode = new DependencyNode(parent, new ArrayList<DependencyNode>());
                nodes.put(parent, parentNode);
            }

            nodes.get(parent).getChildren().add(nodes.get(toAdd));
        }
    }

}
