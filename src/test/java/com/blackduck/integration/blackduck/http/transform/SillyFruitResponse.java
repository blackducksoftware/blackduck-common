package com.blackduck.integration.blackduck.http.transform;

import com.blackduck.integration.blackduck.api.core.BlackDuckResponse;

import java.util.List;

public class SillyFruitResponse extends BlackDuckResponse {
    public String name;
    public FruitCollection fruits;

    public enum PossibleFruits {
        APPLE,
        BANANA
    }

    public class Fruits {
        public boolean apple;
        public boolean banana;
    }

    public class FruitCollection {
        public List<PossibleFruits> possibleFruits;
        public List<Fruits> nestedList;
    }

}
