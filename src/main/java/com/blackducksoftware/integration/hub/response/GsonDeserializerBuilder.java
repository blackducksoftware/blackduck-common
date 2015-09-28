package com.blackducksoftware.integration.hub.response;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonDeserializerBuilder {

    public static Gson buildAutoCompleteDeserializer() {

        Gson gson = new GsonBuilder().addDeserializationExclusionStrategy(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                if (f.getName() == "value") {
                    return false;
                } else if (f.getName() == "uuid") {
                    return false;
                }
                return true;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        }).create();
        return gson;
    }

    public static Gson buildGetProjectDeserializer() {

        Gson gson = new GsonBuilder().addDeserializationExclusionStrategy(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                // if (f.getName() == "id") {
                // return false;
                // } else if (f.getName() == "kb") {
                // return false;
                // } else if (f.getName() == "name") {
                // return false;
                // } else if (f.getName() == "restructured") {
                // return false;
                // } else if (f.getName() == "canonicalReleaseId") {
                // return false;
                // } else if (f.getName() == "internal") {
                // return false;
                // } else if (f.getName() == "openSource") {
                // return false;
                // }
                // return true;
                return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        }).create();
        return gson;
    }
}
