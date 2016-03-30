package com.blackducksoftware.integration.hub.scan.api;

public class AssetReferenceItem {

	private EntityItem ownerEntityKey;

	private EntityItem assetEntityKey;

	public EntityItem getOwnerEntityKey() {
		return ownerEntityKey;
	}

	public void setOwnerEntityKey(final EntityItem ownerEntityKey) {
		this.ownerEntityKey = ownerEntityKey;
	}

	public EntityItem getAssetEntityKey() {
		return assetEntityKey;
	}

	public void setAssetEntityKey(final EntityItem assetEntityKey) {
		this.assetEntityKey = assetEntityKey;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("AssetReferenceItem [ownerEntityKey=");
		builder.append(ownerEntityKey);
		builder.append(", assetEntityKey=");
		builder.append(assetEntityKey);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((assetEntityKey == null) ? 0 : assetEntityKey.hashCode());
		result = prime * result + ((ownerEntityKey == null) ? 0 : ownerEntityKey.hashCode());
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
		if (!(obj instanceof AssetReferenceItem)) {
			return false;
		}
		final AssetReferenceItem other = (AssetReferenceItem) obj;
		if (assetEntityKey == null) {
			if (other.assetEntityKey != null) {
				return false;
			}
		} else if (!assetEntityKey.equals(other.assetEntityKey)) {
			return false;
		}
		if (ownerEntityKey == null) {
			if (other.ownerEntityKey != null) {
				return false;
			}
		} else if (!ownerEntityKey.equals(other.ownerEntityKey)) {
			return false;
		}
		return true;
	}

}
