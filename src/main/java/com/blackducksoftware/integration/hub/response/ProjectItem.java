package com.blackducksoftware.integration.hub.response;

public class ProjectItem {

    private String id;

    private Boolean kb;

    private String name;

    private Boolean restructured;

    private String canonicalReleaseId;

    private Boolean internal;

    private Boolean openSource;

    public ProjectItem(String id, Boolean kb, String name, Boolean restructured, String canonicalReleaseId, Boolean internal, Boolean openSource) {
        this.id = id;
        this.kb = kb;
        this.name = name;
        this.restructured = restructured;
        this.canonicalReleaseId = canonicalReleaseId;
        this.internal = internal;
        this.openSource = openSource;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getRestructured() {
        return restructured;
    }

    public void setRestructured(Boolean restructured) {
        this.restructured = restructured;
    }

    public String getCanonicalReleaseId() {
        return canonicalReleaseId;
    }

    public void setCanonicalReleaseId(String canonicalReleaseId) {
        this.canonicalReleaseId = canonicalReleaseId;
    }

    public Boolean getInternal() {
        return internal;
    }

    public void setInternal(Boolean internal) {
        this.internal = internal;
    }

    public Boolean getOpenSource() {
        return openSource;
    }

    public void setOpenSource(Boolean openSource) {
        this.openSource = openSource;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ProjectItem [id=");
        builder.append(id);
        builder.append(", kb=");
        builder.append(kb);
        builder.append(", name=");
        builder.append(name);
        builder.append(", restructured=");
        builder.append(restructured);
        builder.append(", canonicalReleaseId=");
        builder.append(canonicalReleaseId);
        builder.append(", internal=");
        builder.append(internal);
        builder.append(", openSource=");
        builder.append(openSource);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((canonicalReleaseId == null) ? 0 : canonicalReleaseId.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((internal == null) ? 0 : internal.hashCode());
        result = prime * result + ((kb == null) ? 0 : kb.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((openSource == null) ? 0 : openSource.hashCode());
        result = prime * result + ((restructured == null) ? 0 : restructured.hashCode());
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
        ProjectItem other = (ProjectItem) obj;
        if (canonicalReleaseId == null) {
            if (other.canonicalReleaseId != null) {
                return false;
            }
        } else if (!canonicalReleaseId.equals(other.canonicalReleaseId)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (internal == null) {
            if (other.internal != null) {
                return false;
            }
        } else if (!internal.equals(other.internal)) {
            return false;
        }
        if (kb == null) {
            if (other.kb != null) {
                return false;
            }
        } else if (!kb.equals(other.kb)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (openSource == null) {
            if (other.openSource != null) {
                return false;
            }
        } else if (!openSource.equals(other.openSource)) {
            return false;
        }
        if (restructured == null) {
            if (other.restructured != null) {
                return false;
            }
        } else if (!restructured.equals(other.restructured)) {
            return false;
        }
        return true;
    }

}
