package com.synopsys.integration.blackduck.signaturescanner;

import java.util.Set;

public class ScanTarget {
    private final String path;
    private final Set<String> exclusionPatterns;
    private final String codeLocationName;
    private final String outputDirectoryPath;
    private final boolean outputDirectoryPathAbsolute;

    public static ScanTarget createBasicTarget(final String path) {
        return new ScanTarget(path, null, null, null, false);
    }

    public static ScanTarget createBasicTarget(final String path, final Set<String> exclusionPatterns, final String codeLocationName) {
        return new ScanTarget(path, exclusionPatterns, codeLocationName, null, false);
    }

    private ScanTarget(final String path, final Set<String> exclusionPatterns, final String codeLocationName, final String outputDirectoryPath, final boolean outputDirectoryPathAbsolute) {
        this.path = path;
        this.exclusionPatterns = exclusionPatterns;
        this.codeLocationName = codeLocationName;
        this.outputDirectoryPath = outputDirectoryPath;
        this.outputDirectoryPathAbsolute = outputDirectoryPathAbsolute;
    }

    public String getPath() {
        return path;
    }

    public Set<String> getExclusionPatterns() {
        return exclusionPatterns;
    }

    public String getCodeLocationName() {
        return codeLocationName;
    }

    public String getOutputDirectoryPath() {
        return outputDirectoryPath;
    }

    public boolean isOutputDirectoryPathAbsolute() {
        return outputDirectoryPathAbsolute;
    }

    public static class Builder {
        private String path;
        private Set<String> exclusionPatterns;
        private String codeLocationName;
        private String outputDirectoryPath;
        private boolean outputDirectoryPathAbsolute;

        public Builder(final String path) {
            this.path = path;
        }

        public ScanTarget build() {
            return new ScanTarget(path, exclusionPatterns, codeLocationName, outputDirectoryPath, outputDirectoryPathAbsolute);
        }

        public String getPath() {
            return path;
        }

        public Builder path(final String path) {
            this.path = path;
            return this;
        }

        public Set<String> getExclusionPatterns() {
            return exclusionPatterns;
        }

        public Builder exclusionPatterns(final Set<String> exclusionPatterns) {
            this.exclusionPatterns = exclusionPatterns;
            return this;
        }

        public String getCodeLocationName() {
            return codeLocationName;
        }

        public Builder codeLocationName(final String codeLocationName) {
            this.codeLocationName = codeLocationName;
            return this;
        }

        public String getOutputDirectoryPath() {
            return outputDirectoryPath;
        }

        public Builder outputDirectoryPath(final String outputDirectoryPath) {
            this.outputDirectoryPath = outputDirectoryPath;
            return this;
        }

        public boolean isOutputDirectoryPathAbsolute() {
            return outputDirectoryPathAbsolute;
        }

        public Builder outputDirectoryPathAbsolute(final boolean outputDirectoryPathAbsolute) {
            this.outputDirectoryPathAbsolute = outputDirectoryPathAbsolute;
            return this;
        }

        public Builder outputDirectoryPath(final String outputDirectoryPath, final boolean isAbsolute) {
            this.outputDirectoryPath = outputDirectoryPath;
            outputDirectoryPathAbsolute = isAbsolute;
            return this;
        }
    }
}
