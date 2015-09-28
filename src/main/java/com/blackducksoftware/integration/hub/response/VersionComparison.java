package com.blackducksoftware.integration.hub.response;

public class VersionComparison {

    private String consumerVersion;

    private String producerVersion;

    private Integer numericResult;

    private String operatorResult;

    public String getConsumerVersion() {
        return consumerVersion;
    }

    public void setConsumerVersion(String consumerVersion) {
        this.consumerVersion = consumerVersion;
    }

    public String getProducerVersion() {
        return producerVersion;
    }

    public void setProducerVersion(String producerVersion) {
        this.producerVersion = producerVersion;
    }

    public Integer getNumericResult() {
        return numericResult;
    }

    public void setNumericResult(Integer numericResult) {
        this.numericResult = numericResult;
    }

    public String getOperatorResult() {
        return operatorResult;
    }

    public void setOperatorResult(String operatorResult) {
        this.operatorResult = operatorResult;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VersionComparison [consumerVersion=");
        builder.append(consumerVersion);
        builder.append(", producerVersion=");
        builder.append(producerVersion);
        builder.append(", numericResult=");
        builder.append(numericResult);
        builder.append(", operatorResult=");
        builder.append(operatorResult);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((consumerVersion == null) ? 0 : consumerVersion.hashCode());
        result = prime * result + ((numericResult == null) ? 0 : numericResult.hashCode());
        result = prime * result + ((operatorResult == null) ? 0 : operatorResult.hashCode());
        result = prime * result + ((producerVersion == null) ? 0 : producerVersion.hashCode());
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
        VersionComparison other = (VersionComparison) obj;
        if (consumerVersion == null) {
            if (other.consumerVersion != null) {
                return false;
            }
        } else if (!consumerVersion.equals(other.consumerVersion)) {
            return false;
        }
        if (numericResult == null) {
            if (other.numericResult != null) {
                return false;
            }
        } else if (!numericResult.equals(other.numericResult)) {
            return false;
        }
        if (operatorResult == null) {
            if (other.operatorResult != null) {
                return false;
            }
        } else if (!operatorResult.equals(other.operatorResult)) {
            return false;
        }
        if (producerVersion == null) {
            if (other.producerVersion != null) {
                return false;
            }
        } else if (!producerVersion.equals(other.producerVersion)) {
            return false;
        }
        return true;
    }

}
