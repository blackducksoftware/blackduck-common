package com.synopsys.integration.blackduck.http;

public class BlackDuckUrlSearchTerm {
    public static final BlackDuckUrlSearchTerm PROJECTS = new BlackDuckUrlSearchTerm("projects");
    public static final BlackDuckUrlSearchTerm VERSIONS = new BlackDuckUrlSearchTerm("versions");
    public static final BlackDuckUrlSearchTerm COMPONENTS = new BlackDuckUrlSearchTerm("components");
    public static final BlackDuckUrlSearchTerm ORIGINS = new BlackDuckUrlSearchTerm("origins");

    private final String term;

    public BlackDuckUrlSearchTerm(String term) {
        this.term = term;
    }

    public String getTerm() {
        return term;
    }

}
