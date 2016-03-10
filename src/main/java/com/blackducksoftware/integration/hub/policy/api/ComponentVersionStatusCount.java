package com.blackducksoftware.integration.hub.policy.api;

public class ComponentVersionStatusCount {
    private final String name;

    private final int value;

    public ComponentVersionStatusCount(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public PolicyStatusEnum getPolicyStatusFromName() {
        return PolicyStatusEnum.getPolicyStatusEnum(name);
    }

    public int getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + value;
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
        if (!(obj instanceof ComponentVersionStatusCount)) {
            return false;
        }
        ComponentVersionStatusCount other = (ComponentVersionStatusCount) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (value != other.value) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ComponentVersionStatusCount [name=");
        builder.append(name);
        builder.append(", value=");
        builder.append(value);
        builder.append("]");
        return builder.toString();
    }

}
