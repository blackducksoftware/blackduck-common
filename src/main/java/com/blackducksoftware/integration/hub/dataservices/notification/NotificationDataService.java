package com.blackducksoftware.integration.hub.dataservices.notification;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
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
import com.blackducksoftware.integration.hub.api.VulnerabilityRestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationClearedNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.hub.dataservices.AbstractDataService;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.notification.items.ProjectAggregateBuilder;
import com.blackducksoftware.integration.hub.dataservices.notification.items.ProjectAggregateData;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.AbstractNotificationTransformer;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.NotificationCounter;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.PolicyViolationClearedTransformer;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.PolicyViolationOverrideTransformer;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.PolicyViolationTransformer;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.VulnerabilityTransformer;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class NotificationDataService extends AbstractDataService {
	private final NotificationRestService notificationService;
	private final ProjectVersionRestService projectVersionService;
	private final PolicyRestService policyService;
	private final VersionBomPolicyRestService bomVersionPolicyService;
	private final ComponentVersionRestService componentVersionService;
	private final Map<Class<?>, AbstractNotificationTransformer> transformerMap = new HashMap<>();;
	private final ExecutorService executorService;
	private final ExecutorCompletionService<List<NotificationContentItem>> completionService;
	private PolicyNotificationFilter policyFilter = null;
	private final VulnerabilityRestService vulnerabilityRestService;

	public NotificationDataService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser);
		notificationService = new NotificationRestService(restConnection, jsonParser);
		projectVersionService = new ProjectVersionRestService(restConnection, gson, jsonParser);
		policyService = new PolicyRestService(restConnection, gson, jsonParser);
		bomVersionPolicyService = new VersionBomPolicyRestService(restConnection, gson, jsonParser);
		componentVersionService = new ComponentVersionRestService(restConnection, gson, jsonParser);
		vulnerabilityRestService = new VulnerabilityRestService(restConnection, gson, jsonParser);

		populateTransformerMap();

		final ThreadFactory threadFactory = Executors.defaultThreadFactory();
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
		completionService = new ExecutorCompletionService<>(executorService);
	}

	public NotificationDataService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser,
			final PolicyNotificationFilter policyFilter) {
		this(restConnection, gson, jsonParser);
		this.policyFilter = policyFilter;
	}

	private void populateTransformerMap() {
		transformerMap.put(RuleViolationNotificationItem.class, new PolicyViolationTransformer(notificationService,
				projectVersionService, policyService, bomVersionPolicyService, componentVersionService, policyFilter));
		transformerMap.put(PolicyOverrideNotificationItem.class,
				new PolicyViolationOverrideTransformer(notificationService, projectVersionService, policyService,
						bomVersionPolicyService, componentVersionService, policyFilter));
		transformerMap.put(VulnerabilityNotificationItem.class, new VulnerabilityTransformer(notificationService,
				projectVersionService, policyService, bomVersionPolicyService, componentVersionService));
		transformerMap.put(RuleViolationClearedNotificationItem.class,
				new PolicyViolationClearedTransformer(notificationService, projectVersionService, policyService,
						bomVersionPolicyService, componentVersionService, policyFilter));
	}

	public List<NotificationContentItem> getAllNotifications(final Date startDate, final Date endDate)
			throws IOException, URISyntaxException, BDRestException {
		final List<NotificationContentItem> contentList = new ArrayList<>();
		final List<NotificationItem> itemList = notificationService.getAllNotifications(startDate, endDate);

		int submitted = 0;
		for (final NotificationItem item : itemList) {
			final Class<? extends NotificationItem> key = item.getClass();
			if (transformerMap.containsKey(key)) {
				final AbstractNotificationTransformer converter = transformerMap.get(key);
				final TransformCallable callable = new TransformCallable(item, converter);
				completionService.submit(callable);
				submitted++;
			}
		}

		for (int index = 0; index < submitted; index++) {
			try {
				final Future<List<NotificationContentItem>> future = completionService.take();
				contentList.addAll(future.get());
			} catch (final ExecutionException | InterruptedException e) {

			}
		}

		return contentList;
	}

	public List<ProjectAggregateData> getNotificationCounts(final Date startDate, final Date endDate)
			throws IOException, URISyntaxException, BDRestException, InterruptedException {

		final Map<String, ProjectAggregateBuilder> projectCounterMap = new ConcurrentHashMap<>();
		final NotificationCounter counter = new NotificationCounter(projectCounterMap);
		final List<NotificationContentItem> itemList = getAllNotifications(startDate, endDate);
		for (final NotificationContentItem item : itemList) {
			counter.count(item);
		}

		final List<ProjectAggregateData> dataList = new ArrayList<>();
		for (final Map.Entry<String, ProjectAggregateBuilder> entry : projectCounterMap.entrySet()) {
			ProjectAggregateBuilder builder = entry.getValue().updateDateRange(startDate, endDate);
			builder = builder.updateVulnerabilitiesRestService(vulnerabilityRestService);
			dataList.add(builder.build());
		}
		return dataList;
	}

	private class TransformCallable implements Callable<List<NotificationContentItem>> {
		private final NotificationItem item;
		private final AbstractNotificationTransformer converter;

		public TransformCallable(final NotificationItem item, final AbstractNotificationTransformer converter) {
			this.item = item;
			this.converter = converter;
		}

		@Override
		public List<NotificationContentItem> call() throws Exception {
			return converter.transform(item);
		}
	}
}
