/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.bdio2;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.blackducksoftware.bdio2.BdioMetadata;
import com.blackducksoftware.bdio2.BdioWriter;
import com.blackducksoftware.bdio2.model.Component;
import com.blackducksoftware.bdio2.model.Project;

public class Bdio2Writer {
    public BdioWriter createBdioWriter(final OutputStream outputStream, final BdioMetadata bdioMetadata) {
        final BdioWriter.StreamSupplier streamSupplier = new BdioWriter.BdioFile(outputStream);
        return new BdioWriter(bdioMetadata, streamSupplier);
    }

    public void writeBdioDocument(final OutputStream outputStream, final Bdio2Document bdio2Document) throws IOException {
        final BdioWriter bdioWriter = createBdioWriter(outputStream, bdio2Document.getBdioMetadata());
        writeBdioDocument(bdioWriter, bdio2Document.getProject(), bdio2Document.getComponents());
    }

    public void writeBdioDocument(final BdioWriter bdioWriter, final Project project, final List<Component> components) throws IOException {
        bdioWriter.start();

        for (Component component : components) {
            bdioWriter.next(component);
        }

        // We put the project node at the end of the document to be more inline with the way Black Duck produces BDIO 2.
        bdioWriter.next(project);

        bdioWriter.close();
    }
}
