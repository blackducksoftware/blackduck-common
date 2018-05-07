package com.blackducksoftware.integration.hub.throwaway;

import java.io.Closeable;
import java.io.IOException;
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

import com.blackducksoftware.integration.log.IntLogger;

public class ParallelResourceProcessor<R, S> implements Closeable {
    private final Map<Class<?>, ItemTransformer<R, S>> transformerMap = new HashMap<>();
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

    public void addTransformer(final Class<?> clazz, final ItemTransformer<R, S> transform) {
        transformerMap.put(clazz, transform);
    }

    public void removeTransformer(final Class<?> clazz) {
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
                final ItemTransformer<R, S> converter = transformerMap.get(key);
                final TransformCallable callable = new TransformCallable(item, converter);
                completionService.submit(callable);
                submitted++;
            }
        }

        return submitted;
    }

    private ParallelResourceProcessorResults<R> processItems(final int submitted) {
        final List<R> resultsList = new LinkedList<>();
        final List<Exception> exceptions = new ArrayList<>();
        for (int index = 0; index < submitted; index++) {
            try {
                final Future<List<R>> future = completionService.take();
                final List<R> contentItems = future.get();
                resultsList.addAll(contentItems);
            } catch (final ExecutionException | InterruptedException e) {
                final String msg = "Error from parallel task: " + e.getMessage();
                logger.error(msg, e);
                exceptions.add(e);
            }
        }
        final ParallelResourceProcessorResults<R> resultsObject = new ParallelResourceProcessorResults<>(resultsList, exceptions);
        return resultsObject;
    }

    private class TransformCallable implements Callable<List<R>> {
        private final S item;
        private final ItemTransformer<R, S> converter;

        public TransformCallable(final S item, final ItemTransformer<R, S> converter) {
            this.item = item;
            this.converter = converter;
        }

        @Override
        public List<R> call() throws Exception {
            return converter.transform(item);
        }
    }

    @Override
    public void close() throws IOException {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
