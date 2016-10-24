package com.blackducksoftware.integration.hub.api.project.version;

import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.api.version.DistributionEnum;
import com.blackducksoftware.integration.hub.api.version.PhaseEnum;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ProjectVersionItem extends HubItem {
    private final DistributionEnum distribution;

    private final ComplexLicense license;

    private final String nickname;

    private final PhaseEnum phase;

    private final String releaseComments;

    private final DateTime releasedOn;

    // description from Hub API: "Read-Only; No matter the value it will always default to 'CUSTOM'",
    private final SourceEnum source;

    private final String versionName;

    public ProjectVersionItem(final MetaInformation meta, final DistributionEnum distribution,
            final ComplexLicense license, final String nickname, final PhaseEnum phase, final String releaseComments,
            final DateTime releasedOn, final SourceEnum source, final String versionName) {
        super(meta);
        this.distribution = distribution;
        this.license = license;
        this.nickname = nickname;
        this.phase = phase;
        this.releaseComments = releaseComments;
        this.releasedOn = releasedOn;
        this.source = source;
        this.versionName = versionName;
    }

    public DistributionEnum getDistribution() {
        return distribution;
    }

    public ComplexLicense getLicense() {
        return license;
    }

    public String getNickname() {
        return nickname;
    }

    public PhaseEnum getPhase() {
        return phase;
    }

    public String getReleaseComments() {
        return releaseComments;
    }

    public DateTime getReleasedOn() {
        return releasedOn;
    }

    public SourceEnum getSource() {
        return source;
    }

    public String getVersionName() {
        return versionName;
    }

}
