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
package com.blackducksoftware.integration.hub.dataservice.parallel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.blackducksoftware.integration.hub.dataservice.ItemTransform;
import com.blackducksoftware.integration.log.IntLogger;

public class ParallelResourceProcessor<R, S> {
    private final Map<Class<?>, ItemTransform<List<R>, S>> transformerMap = new HashMap<>();;

    private final ExecutorService executorService;

    private final ExecutorCompletionService<List<R>> completionService;

    private final IntLogger logger;

    public ParallelResourceProcessor(final IntLogger logger) {
        this.logger = logger;
        final ThreadFactory threadFactory = Executors.defaultThreadFactory();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
        completionService = new ExecutorCompletionService<>(executorService);
    }

    public ParallelResourceProcessor(final IntLogger logger, final ExecutorService executorService,
            final ExecutorCompletionService<List<R>> completionService) {
        this.logger = logger;
        this.executorService = executorService;
        this.completionService = completionService;
    }

    public void addTransform(final Class<?> clazz, final ItemTransform<List<R>, S> transform) {
        transformerMap.put(clazz, transform);
    }

    public void removeTransform(final Class<?> clazz) {
        transformerMap.remove(clazz);
    }

    public ParallelResourceProcessorResults<R> process(final List<S> itemsToProcess) {
        final int submitted = submitItems(itemsToProcess);
        final ParallelResourceProcessorResults<R> results = processItems(submitted);
        return results;
    }

    private int submitItems(final List<S> itemList) {
        int submitted = 0;
        for (final S item : itemList) {
            final Class<?> key = item.getClass();
            if (transformerMap.containsKey(key)) {
                final ItemTransform<List<R>, S> converter = transformerMap.get(key);
                final TransformCallable callable = new TransformCallable(item, converter);
                completionService.submit(callable);
                submitted++;
            }
        }

        return submitted;
    }

    private ParallelResourceProcessorResults<R> processItems(final int submitted) {
        final List<R> resultsList = new LinkedList<>();
        final List<String> errorMessages = new ArrayList<>();
        for (int index = 0; index < submitted; index++) {
            try {
                final Future<List<R>> future = completionService.take();
                final List<R> contentItems = future.get();
                resultsList.addAll(contentItems);
            } catch (final ExecutionException | InterruptedException e) {
                final String msg = "Error from parallel task: " + e.getMessage();
                logger.error(msg, e);
                errorMessages.add(msg);
            }
        }
        final ParallelResourceProcessorResults<R> resultsObject = new ParallelResourceProcessorResults<>(resultsList, errorMessages);
        return resultsObject;
    }

    private class TransformCallable implements Callable<List<R>> {
        private final S item;

        private final ItemTransform<List<R>, S> converter;

        public TransformCallable(final S item, final ItemTransform<List<R>, S> converter) {
            this.item = item;
            this.converter = converter;
        }

        @Override
        public List<R> call() throws Exception {
            return converter.transform(item);
        }
    }
}
