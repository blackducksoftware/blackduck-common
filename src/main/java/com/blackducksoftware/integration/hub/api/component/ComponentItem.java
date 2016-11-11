package com.blackducksoftware.integration.hub.api.component;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.google.gson.annotations.SerializedName;

public class ComponentItem extends HubItem {
    @SerializedName("component")
    private final String componentUrl;

    private final String componentName;

    private final String originId;

    @SerializedName("version")
    private final String versionUrl;

    private final String versionName;

    public ComponentItem(MetaInformation meta, String componentUrl, String componentName, String originId, String versionUrl, String versionName) {
        super(meta);
        this.componentUrl = componentUrl;
        this.componentName = componentName;
        this.originId = originId;
        this.versionUrl = versionUrl;
        this.versionName = versionName;
    }

    public String getComponentUrl() {
        return componentUrl;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getOriginId() {
        return originId;
    }

    public String getVersionUrl() {
        return versionUrl;
    }

    public String getVersionName() {
        return versionName;
    }

}
