package com.blackducksoftware.integration.hub.response;

import java.util.List;

import org.joda.time.DateTime;

public class ReportMetaInformationItem {

    private String reportFormat;

    private String locale;

    private String fileName;

    private int fileSize;

    private DateTime createdAt;

    private DateTime updatedAt;

    private DateTime finishedAt;

    private String createdBy;

    private ReportMetaItem _meta;

    public String getReportFormat() {
        return reportFormat;
    }

    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(DateTime createdAt) {
        this.createdAt = createdAt;
    }

    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(DateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public DateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(DateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ReportMetaItem get_meta() {
        return _meta;
    }

    public void set_meta(ReportMetaItem _meta) {
        this._meta = _meta;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_meta == null) ? 0 : _meta.hashCode());
        result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
        result = prime * result + ((createdBy == null) ? 0 : createdBy.hashCode());
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + fileSize;
        result = prime * result + ((finishedAt == null) ? 0 : finishedAt.hashCode());
        result = prime * result + ((locale == null) ? 0 : locale.hashCode());
        result = prime * result + ((reportFormat == null) ? 0 : reportFormat.hashCode());
        result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
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
        ReportMetaInformationItem other = (ReportMetaInformationItem) obj;
        if (_meta == null) {
            if (other._meta != null) {
                return false;
            }
        } else if (!_meta.equals(other._meta)) {
            return false;
        }
        if (createdAt == null) {
            if (other.createdAt != null) {
                return false;
            }
        } else if (!createdAt.equals(other.createdAt)) {
            return false;
        }
        if (createdBy == null) {
            if (other.createdBy != null) {
                return false;
            }
        } else if (!createdBy.equals(other.createdBy)) {
            return false;
        }
        if (fileName == null) {
            if (other.fileName != null) {
                return false;
            }
        } else if (!fileName.equals(other.fileName)) {
            return false;
        }
        if (fileSize != other.fileSize) {
            return false;
        }
        if (finishedAt == null) {
            if (other.finishedAt != null) {
                return false;
            }
        } else if (!finishedAt.equals(other.finishedAt)) {
            return false;
        }
        if (locale == null) {
            if (other.locale != null) {
                return false;
            }
        } else if (!locale.equals(other.locale)) {
            return false;
        }
        if (reportFormat == null) {
            if (other.reportFormat != null) {
                return false;
            }
        } else if (!reportFormat.equals(other.reportFormat)) {
            return false;
        }
        if (updatedAt == null) {
            if (other.updatedAt != null) {
                return false;
            }
        } else if (!updatedAt.equals(other.updatedAt)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ReportMetaInformationItem [reportFormat=");
        builder.append(reportFormat);
        builder.append(", locale=");
        builder.append(locale);
        builder.append(", fileName=");
        builder.append(fileName);
        builder.append(", fileSize=");
        builder.append(fileSize);
        builder.append(", createdAt=");
        builder.append(createdAt);
        builder.append(", updatedAt=");
        builder.append(updatedAt);
        builder.append(", finishedAt=");
        builder.append(finishedAt);
        builder.append(", createdBy=");
        builder.append(createdBy);
        builder.append(", _meta=");
        builder.append(_meta);
        builder.append("]");
        return builder.toString();
    }

    public class ReportMetaItem {
        List<String> allow;

        String href;

        List<ReportMetaLinkItem> links;

        public List<String> getAllow() {
            return allow;
        }

        public void setAllow(List<String> allow) {
            this.allow = allow;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public List<ReportMetaLinkItem> getLinks() {
            return links;
        }

        public void setLinks(List<ReportMetaLinkItem> links) {
            this.links = links;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((allow == null) ? 0 : allow.hashCode());
            result = prime * result + ((href == null) ? 0 : href.hashCode());
            result = prime * result + ((links == null) ? 0 : links.hashCode());
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
            ReportMetaItem other = (ReportMetaItem) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (allow == null) {
                if (other.allow != null) {
                    return false;
                }
            } else if (!allow.equals(other.allow)) {
                return false;
            }
            if (href == null) {
                if (other.href != null) {
                    return false;
                }
            } else if (!href.equals(other.href)) {
                return false;
            }
            if (links == null) {
                if (other.links != null) {
                    return false;
                }
            } else if (!links.equals(other.links)) {
                return false;
            }
            return true;
        }

        private ReportMetaInformationItem getOuterType() {
            return ReportMetaInformationItem.this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ReportMetaItem [allow=");
            builder.append(allow);
            builder.append(", href=");
            builder.append(href);
            builder.append(", links=");
            builder.append(links);
            builder.append("]");
            return builder.toString();
        }

    }

    public class ReportMetaLinkItem {
        String rel;

        String href;

        public String getRel() {
            return rel;
        }

        public void setRel(String rel) {
            this.rel = rel;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((href == null) ? 0 : href.hashCode());
            result = prime * result + ((rel == null) ? 0 : rel.hashCode());
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
            ReportMetaLinkItem other = (ReportMetaLinkItem) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (href == null) {
                if (other.href != null) {
                    return false;
                }
            } else if (!href.equals(other.href)) {
                return false;
            }
            if (rel == null) {
                if (other.rel != null) {
                    return false;
                }
            } else if (!rel.equals(other.rel)) {
                return false;
            }
            return true;
        }

        private ReportMetaInformationItem getOuterType() {
            return ReportMetaInformationItem.this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ReportMetaLinkItem [rel=");
            builder.append(rel);
            builder.append(", href=");
            builder.append(href);
            builder.append("]");
            return builder.toString();
        }

    }
}
