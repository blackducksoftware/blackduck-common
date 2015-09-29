package com.blackducksoftware.integration.hub.response;

public class ReleaseItem {

    private String id;

    private Boolean kb;

    private Boolean ohloh;

    private String projectId;

    private String version;

    private String phase;

    private String distribution;

    private String fileBomCodeLocationsUrl;

    private String fileBomEntriesUrl;

    private String codeLocationsUrl;

    private String bomCountsUrl;

    private String vulnerabilityCountsUrl;

    private String riskProfileUrl;

    public ReleaseItem() {

    }

    public ReleaseItem(String id, Boolean kb, Boolean ohloh, String projectId, String version, String phase, String distribution,
            String fileBomCodeLocationsUrl, String fileBomEntriesUrl, String codeLocationsUrl, String bomCountsUrl, String vulnerabilityCountsUrl,
            String riskProfileUrl) {
        this.id = id;
        this.kb = kb;
        this.ohloh = ohloh;
        this.projectId = projectId;
        this.version = version;
        this.phase = phase;
        this.distribution = distribution;
        this.fileBomCodeLocationsUrl = fileBomCodeLocationsUrl;
        this.fileBomEntriesUrl = fileBomEntriesUrl;
        this.codeLocationsUrl = codeLocationsUrl;
        this.bomCountsUrl = bomCountsUrl;
        this.vulnerabilityCountsUrl = vulnerabilityCountsUrl;
        this.riskProfileUrl = riskProfileUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getKb() {
        return kb;
    }

    public void setKb(Boolean kb) {
        this.kb = kb;
    }

    public Boolean getOhloh() {
        return ohloh;
    }

    public void setOhloh(Boolean ohloh) {
        this.ohloh = ohloh;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public String getFileBomCodeLocationsUrl() {
        return fileBomCodeLocationsUrl;
    }

    public void setFileBomCodeLocationsUrl(String fileBomCodeLocationsUrl) {
        this.fileBomCodeLocationsUrl = fileBomCodeLocationsUrl;
    }

    public String getFileBomEntriesUrl() {
        return fileBomEntriesUrl;
    }

    public void setFileBomEntriesUrl(String fileBomEntriesUrl) {
        this.fileBomEntriesUrl = fileBomEntriesUrl;
    }

    public String getCodeLocationsUrl() {
        return codeLocationsUrl;
    }

    public void setCodeLocationsUrl(String codeLocationsUrl) {
        this.codeLocationsUrl = codeLocationsUrl;
    }

    public String getBomCountsUrl() {
        return bomCountsUrl;
    }

    public void setBomCountsUrl(String bomCountsUrl) {
        this.bomCountsUrl = bomCountsUrl;
    }

    public String getVulnerabilityCountsUrl() {
        return vulnerabilityCountsUrl;
    }

    public void setVulnerabilityCountsUrl(String vulnerabilityCountsUrl) {
        this.vulnerabilityCountsUrl = vulnerabilityCountsUrl;
    }

    public String getRiskProfileUrl() {
        return riskProfileUrl;
    }

    public void setRiskProfileUrl(String riskProfileUrl) {
        this.riskProfileUrl = riskProfileUrl;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ReleaseItem [id=");
        builder.append(id);
        builder.append(", kb=");
        builder.append(kb);
        builder.append(", ohloh=");
        builder.append(ohloh);
        builder.append(", projectId=");
        builder.append(projectId);
        builder.append(", version=");
        builder.append(version);
        builder.append(", phase=");
        builder.append(phase);
        builder.append(", distribution=");
        builder.append(distribution);
        builder.append(", fileBomCodeLocationsUrl=");
        builder.append(fileBomCodeLocationsUrl);
        builder.append(", fileBomEntriesUrl=");
        builder.append(fileBomEntriesUrl);
        builder.append(", codeLocationsUrl=");
        builder.append(codeLocationsUrl);
        builder.append(", bomCountsUrl=");
        builder.append(bomCountsUrl);
        builder.append(", vulnerabilityCountsUrl=");
        builder.append(vulnerabilityCountsUrl);
        builder.append(", riskProfileUrl=");
        builder.append(riskProfileUrl);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bomCountsUrl == null) ? 0 : bomCountsUrl.hashCode());
        result = prime * result + ((codeLocationsUrl == null) ? 0 : codeLocationsUrl.hashCode());
        result = prime * result + ((distribution == null) ? 0 : distribution.hashCode());
        result = prime * result + ((fileBomCodeLocationsUrl == null) ? 0 : fileBomCodeLocationsUrl.hashCode());
        result = prime * result + ((fileBomEntriesUrl == null) ? 0 : fileBomEntriesUrl.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((kb == null) ? 0 : kb.hashCode());
        result = prime * result + ((ohloh == null) ? 0 : ohloh.hashCode());
        result = prime * result + ((phase == null) ? 0 : phase.hashCode());
        result = prime * result + ((projectId == null) ? 0 : projectId.hashCode());
        result = prime * result + ((riskProfileUrl == null) ? 0 : riskProfileUrl.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((vulnerabilityCountsUrl == null) ? 0 : vulnerabilityCountsUrl.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ReleaseItem other = (ReleaseItem) obj;
        if (bomCountsUrl == null) {
            if (other.bomCountsUrl != null) {
                return false;
            }
        } else if (!bomCountsUrl.equals(other.bomCountsUrl)) {
            return false;
        }
        if (codeLocationsUrl == null) {
            if (other.codeLocationsUrl != null) {
                return false;
            }
        } else if (!codeLocationsUrl.equals(other.codeLocationsUrl)) {
            return false;
        }
        if (distribution == null) {
            if (other.distribution != null) {
                return false;
            }
        } else if (!distribution.equals(other.distribution)) {
            return false;
        }
        if (fileBomCodeLocationsUrl == null) {
            if (other.fileBomCodeLocationsUrl != null) {
                return false;
            }
        } else if (!fileBomCodeLocationsUrl.equals(other.fileBomCodeLocationsUrl)) {
            return false;
        }
        if (fileBomEntriesUrl == null) {
            if (other.fileBomEntriesUrl != null) {
                return false;
            }
        } else if (!fileBomEntriesUrl.equals(other.fileBomEntriesUrl)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (kb == null) {
            if (other.kb != null) {
                return false;
            }
        } else if (!kb.equals(other.kb)) {
            return false;
        }
        if (ohloh == null) {
            if (other.ohloh != null) {
                return false;
            }
        } else if (!ohloh.equals(other.ohloh)) {
            return false;
        }
        if (phase == null) {
            if (other.phase != null) {
                return false;
            }
        } else if (!phase.equals(other.phase)) {
            return false;
        }
        if (projectId == null) {
            if (other.projectId != null) {
                return false;
            }
        } else if (!projectId.equals(other.projectId)) {
            return false;
        }
        if (riskProfileUrl == null) {
            if (other.riskProfileUrl != null) {
                return false;
            }
        } else if (!riskProfileUrl.equals(other.riskProfileUrl)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        if (vulnerabilityCountsUrl == null) {
            if (other.vulnerabilityCountsUrl != null) {
                return false;
            }
        } else if (!vulnerabilityCountsUrl.equals(other.vulnerabilityCountsUrl)) {
            return false;
        }
        return true;
    }

}
