package com.blackducksoftware.integration.hub.dataservices.notification;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.blackducksoftware.integration.hub.api.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.NotificationRestService;
import com.blackducksoftware.integration.hub.api.PolicyRestService;
import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.hub.dataservices.AbstractDataService;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationCountData;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationCountBuilder;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.AbstractNotificationCounter;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.AbstractNotificationTransform;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.PolicyOverrideCounter;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.PolicyViolationCounter;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.PolicyViolationOverrideTransform;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.PolicyViolationTransform;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.VulnerabilityCounter;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.VulnerabilityTransform;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class NotificationDataService extends AbstractDataService {
	private final NotificationRestService notificationService;
	private final ProjectVersionRestService projectVersionService;
	private final PolicyRestService policyService;
	private final VersionBomPolicyRestService bomVersionPolicyService;
	private final ComponentVersionRestService componentVersionService;
	private final Map<Class<?>, AbstractNotificationTransform> transformMap;
	private final ExecutorService executorService;
	private final ExecutorCompletionService<List<NotificationContentItem>> completionService;
	private final PolicyNotificationFilter policyFilter;

	public NotificationDataService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser,
			final PolicyNotificationFilter policyFilter) {
		super(restConnection, gson, jsonParser);
		notificationService = new NotificationRestService(restConnection, jsonParser);
		projectVersionService = new ProjectVersionRestService(restConnection, gson, jsonParser);
		policyService = new PolicyRestService(restConnection, gson, jsonParser);
		bomVersionPolicyService = new VersionBomPolicyRestService(restConnection, gson, jsonParser);
		componentVersionService = new ComponentVersionRestService(restConnection, gson, jsonParser);
		this.policyFilter = policyFilter;
		transformMap = createTransformMap();
		final ThreadFactory threadFactory = Executors.defaultThreadFactory();
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
		completionService = new ExecutorCompletionService<>(executorService);
	}

	private Map<Class<?>, AbstractNotificationTransform> createTransformMap() {
		final Map<Class<?>, AbstractNotificationTransform> transformMap = new HashMap<>();
		transformMap.put(RuleViolationNotificationItem.class, new PolicyViolationTransform(notificationService,
				projectVersionService, policyService, bomVersionPolicyService, componentVersionService, policyFilter));
		transformMap.put(PolicyOverrideNotificationItem.class, new PolicyViolationOverrideTransform(notificationService,
				projectVersionService, policyService, bomVersionPolicyService, componentVersionService, policyFilter));
		transformMap.put(VulnerabilityNotificationItem.class, new VulnerabilityTransform(notificationService,
				projectVersionService, policyService, bomVersionPolicyService, componentVersionService));

		return transformMap;
	}

	private Map<Class<?>, AbstractNotificationCounter> createCounterMap(
			final Map<String, NotificationCountBuilder> projectCounterMap) {
		final Map<Class<?>, AbstractNotificationCounter> transformMap = new HashMap<>();
		transformMap.put(RuleViolationNotificationItem.class,
				new PolicyViolationCounter(projectVersionService, projectCounterMap));
		transformMap.put(PolicyOverrideNotificationItem.class,
				new PolicyOverrideCounter(projectVersionService, projectCounterMap));
		transformMap.put(VulnerabilityNotificationItem.class,
				new VulnerabilityCounter(projectVersionService, projectCounterMap));

		return transformMap;
	}

	public List<NotificationContentItem> getAllNotifications(final Date startDate, final Date endDate)
			throws IOException, URISyntaxException, BDRestException {
		final List<NotificationContentItem> contentList = new ArrayList<>();
		final List<NotificationItem> itemList = notificationService.getAllNotifications(startDate, endDate);

		final int count = itemList.size();

		for (final NotificationItem item : itemList) {
			final Class<? extends NotificationItem> key = item.getClass();
			if (transformMap.containsKey(key)) {
				final AbstractNotificationTransform converter = transformMap.get(key);
				final TransformCallable callable = new TransformCallable(item, converter);
				completionService.submit(callable);
			}
		}

		for (int index = 0; index < count; index++) {
			try {
				final Future<List<NotificationContentItem>> future = completionService.take();
				contentList.addAll(future.get());
			} catch (final ExecutionException | InterruptedException e) {

			}
		}

		return contentList;
	}

	public List<NotificationCountData> getNotificationCounts(final Date startDate, final Date endDate)
			throws IOException, URISyntaxException, BDRestException {

		final List<NotificationItem> itemList = notificationService.getAllNotifications(startDate, endDate);
		final Map<String, NotificationCountBuilder> projectCounterMap = new HashMap<>();
		final Map<Class<?>, AbstractNotificationCounter> transformMap = createCounterMap(projectCounterMap);

		for (final NotificationItem item : itemList) {
			try {
				final Class<? extends NotificationItem> key = item.getClass();
				if (transformMap.containsKey(key)) {
					final AbstractNotificationCounter counter = transformMap.get(key);
					counter.count(item);
				}
			} catch (final HubItemTransformException e) {

			}
		}

		// reset counters
		final List<NotificationCountData> dataList = new ArrayList<>();
		for (final Map.Entry<String, NotificationCountBuilder> entry : projectCounterMap.entrySet()) {
			dataList.add(entry.getValue().build());
		}
		return dataList;
	}

	private class TransformCallable implements Callable<List<NotificationContentItem>> {
		private final NotificationItem item;
		private final AbstractNotificationTransform converter;

		public TransformCallable(final NotificationItem item, final AbstractNotificationTransform converter) {
			this.item = item;
			this.converter = converter;
		}

		@Override
		public List<NotificationContentItem> call() throws Exception {
			return converter.transform(item);
		}
	}
}
