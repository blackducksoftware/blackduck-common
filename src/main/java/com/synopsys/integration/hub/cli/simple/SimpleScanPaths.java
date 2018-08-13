package com.synopsys.integration.hub.cli.simple;

public class SimpleScanPaths {
    private final String pathToJavaExecutable;
    private final String pathToOneJar;
    private final String pathToScanExecutable;

    public SimpleScanPaths(final String pathToJavaExecutable, final String pathToOneJar, final String pathToScanExecutable) {
        this.pathToJavaExecutable = pathToJavaExecutable;
        this.pathToOneJar = pathToOneJar;
        this.pathToScanExecutable = pathToScanExecutable;
    }

    public String getPathToJavaExecutable() {
        return pathToJavaExecutable;
    }

    public String getPathToOneJar() {
        return pathToOneJar;
    }

    public String getPathToScanExecutable() {
        return pathToScanExecutable;
    }

}
