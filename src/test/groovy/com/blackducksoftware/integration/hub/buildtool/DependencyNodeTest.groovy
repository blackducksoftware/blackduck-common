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
package com.blackducksoftware.integration.hub.buildtool

import static org.junit.Assert.*

import org.junit.Test

class DependencyNodeTest {

    def Gav root = new Gav('testRoot','root','1.0')
    def Gav firstChild = new Gav('children','first','1.0')
    def Gav secondChild = new Gav('children','second','2.0')
    def Gav thirdChild = new Gav('children','third','3.0')
    def Gav fourthChild = new Gav('children','fourth','4.0')
    def Gav subFirstChild = new Gav('subChild','first','1.0')
    def Gav subSecondChild = new Gav('subChild','second','2.0')
    def Gav subThirdChild = new Gav('subChild','third','3.0')

    DependencyNode getRootNodeToCompareWith(){
        //Constructing the root node in a specific structure
        def children = []
        def firstChildList = [
            createDependencyNode(subFirstChild),
            createDependencyNode(subSecondChild)
        ]
        children.add(createDependencyNodeWithChildren(firstChild, firstChildList))
        children.add(createDependencyNodeWithChildren(secondChild, [
            createDependencyNode(subThirdChild)
        ]))
        children.add(createDependencyNode(thirdChild))

        def fourthChildList = [
            createDependencyNode(subThirdChild)
        ]
        fourthChildList.addAll(firstChildList)
        children.add(createDependencyNodeWithChildren(fourthChild, fourthChildList))
        createDependencyNodeWithChildren(root,children)
    }

    DependencyNode createDependencyNode(Gav gav){
        new DependencyNode(gav, [])
    }

    DependencyNode createDependencyNodeWithChildren(Gav gav, ArrayList<DependencyNode> children){
        new DependencyNode(gav, children)
    }


    @Test
    void testDependencyNodeBuilder() {
        def rootToCompareTo = getRootNodeToCompareWith()
        println("$rootToCompareTo")

        //Adding the relationships randomly
        DependencyNodeBuilder builder = new DependencyNodeBuilder(root);
        builder.addNodeWithChild(fourthChild,subFirstChild)
        builder.addNodeWithChild(firstChild,subFirstChild)
        builder.addNodeWithChildren(fourthChild,[
            subThirdChild,
            subSecondChild
        ])
        builder.addNodeWithChildren(root,[
            firstChild,
            secondChild,
            thirdChild,
            fourthChild
        ])
        builder.addNodeWithChild(firstChild,subSecondChild)
        builder.addNodeWithChild(secondChild, subThirdChild)
        builder.addNode(thirdChild)
        DependencyNode rootNode = builder.buildRootNode()
        println("$rootNode")

        compareNode(rootToCompareTo, rootNode)
    }

    void compareNode(DependencyNode expected, DependencyNode actual){
        assertEquals(expected.gav, actual.gav)
        assert actual.children.size() == expected.children.size()
        if(actual.children.size() > 0){
            actual.children.each{ actualChild ->
                DependencyNode expectedChildMatch = expected.children.find{ expectedChild ->
                    expectedChild.gav == actualChild.gav
                }
                compareNode(expectedChildMatch, actualChild)
            }
        }
    }
}
