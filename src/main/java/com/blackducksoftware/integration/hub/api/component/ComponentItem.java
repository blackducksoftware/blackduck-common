package com.blackducksoftware.integration.hub.api.component;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ComponentItem extends HubItem {
    private final String component; // ****URL**** //

    private final String componentName;

    private final String originId;

    private final String version; // ****URL**** //

    private final String versionName;

    public ComponentItem(MetaInformation meta, String componentUrl, String componentName, String originId, String versionUrl, String versionName) {
        super(meta);
        this.component = componentUrl;
        this.componentName = componentName;
        this.originId = originId;
        this.version = versionUrl;
        this.versionName = versionName;
    }

    public String getComponent() {
        return component;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getOriginId() {
        return originId;
    }

    public String getVersion() {
        return version;
    }

    public String getVersionName() {
        return versionName;
    }

}
