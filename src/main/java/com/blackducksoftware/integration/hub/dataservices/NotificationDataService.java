package com.blackducksoftware.integration.hub.dataservices;

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
import com.blackducksoftware.integration.hub.dataservices.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.transforms.AbstractNotificationTransform;
import com.blackducksoftware.integration.hub.dataservices.transforms.PolicyViolationOverrideTransform;
import com.blackducksoftware.integration.hub.dataservices.transforms.PolicyViolationTransform;
import com.blackducksoftware.integration.hub.dataservices.transforms.VulnerabilityTransform;
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
	private final Map<Class<?>, AbstractNotificationTransform> transformMap;
	private final ExecutorService executorService;
	private final ExecutorCompletionService<List<NotificationContentItem>> completionService;

	public NotificationDataService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser);
		notificationService = new NotificationRestService(restConnection, jsonParser);
		projectVersionService = new ProjectVersionRestService(restConnection, gson, jsonParser);
		policyService = new PolicyRestService(restConnection, gson, jsonParser);
		bomVersionPolicyService = new VersionBomPolicyRestService(restConnection, gson, jsonParser);
		componentVersionService = new ComponentVersionRestService(restConnection, gson, jsonParser);
		transformMap = new HashMap<>();
		transformMap.put(RuleViolationNotificationItem.class, new PolicyViolationTransform(notificationService,
				projectVersionService, policyService, bomVersionPolicyService, componentVersionService));
		transformMap.put(PolicyOverrideNotificationItem.class, new PolicyViolationOverrideTransform(notificationService,
				projectVersionService, policyService, bomVersionPolicyService, componentVersionService));
		transformMap.put(VulnerabilityNotificationItem.class, new VulnerabilityTransform(notificationService,
				projectVersionService, policyService, bomVersionPolicyService, componentVersionService));

		final ThreadFactory threadFactory = Executors.defaultThreadFactory();
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
		completionService = new ExecutorCompletionService<>(executorService);
	}

	public List<NotificationContentItem> getAllNotifications(final Date startDate, final Date endDate)
			throws IOException, URISyntaxException, BDRestException {
		final List<NotificationContentItem> contentList = new ArrayList<>();
		final List<NotificationItem> itemList = notificationService.getAllNotifications(startDate, endDate);

		final int count = itemList.size();
		for (final Map.Entry<Class<?>, AbstractNotificationTransform> entry : transformMap.entrySet()) {
			entry.getValue().reset();
		}

		for (final NotificationItem item : itemList) {
			if (transformMap.containsKey(item.getClass())) {
				final AbstractNotificationTransform converter = transformMap.get(item.getClass());
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
