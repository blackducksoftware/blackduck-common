package com.blackducksoftware.integration.hub;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum BuilderType {

    MAVEN,

    GRADLE,

    ANT,

    UNKNOWN_BUILDER;

    private static Map<String, BuilderType> builderTypeMap = new HashMap<String, BuilderType>();

    static {
        builderTypeMap.put(BuilderType.MAVEN.toString(), BuilderType.MAVEN);
        builderTypeMap.put(BuilderType.GRADLE.toString(), BuilderType.GRADLE);
        builderTypeMap.put(BuilderType.ANT.toString(), BuilderType.ANT);
        builderTypeMap.put(BuilderType.UNKNOWN_BUILDER.toString(), BuilderType.UNKNOWN_BUILDER);
    }

    public static BuilderType getBuilderType(String builder) {

        if (builderTypeMap.containsKey(builder.toUpperCase())) {
            return builderTypeMap.get(builder.toUpperCase());
        } else {
            return BuilderType.UNKNOWN_BUILDER;
        }

    }

    public static List<BuilderType> getBuilderTypes() {
        return Arrays.asList(BuilderType.values());
    }

}
