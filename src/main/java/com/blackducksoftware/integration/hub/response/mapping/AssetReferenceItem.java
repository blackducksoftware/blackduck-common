package com.blackducksoftware.integration.hub.response.mapping;

public class AssetReferenceItem {

    private EntityItem ownerEntityKey;

    private EntityItem assetEntityKey;

    public EntityItem getOwnerEntityKey() {
        return ownerEntityKey;
    }

    public void setOwnerEntityKey(EntityItem ownerEntityKey) {
        this.ownerEntityKey = ownerEntityKey;
    }

    public EntityItem getAssetEntityKey() {
        return assetEntityKey;
    }

    public void setAssetEntityKey(EntityItem assetEntityKey) {
        this.assetEntityKey = assetEntityKey;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
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
        AssetReferenceItem other = (AssetReferenceItem) obj;
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
