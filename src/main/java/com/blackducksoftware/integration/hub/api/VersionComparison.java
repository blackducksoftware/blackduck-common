package com.blackducksoftware.integration.hub.api;

public class VersionComparison {

	private final String consumerVersion;

	private final String producerVersion;

	private final Integer numericResult;

	private final String operatorResult;

	public VersionComparison(final String consumerVersion, final String producerVersion, final Integer numericResult, final String operatorResult) {
		this.consumerVersion = consumerVersion;
		this.producerVersion = producerVersion;
		this.numericResult = numericResult;
		this.operatorResult = operatorResult;
	}

	public String getConsumerVersion() {
		return consumerVersion;
	}

	public String getProducerVersion() {
		return producerVersion;
	}

	public Integer getNumericResult() {
		return numericResult;
	}

	public String getOperatorResult() {
		return operatorResult;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
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
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof VersionComparison)) {
			return false;
		}
		final VersionComparison other = (VersionComparison) obj;
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
