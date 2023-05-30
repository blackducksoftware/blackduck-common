package com.synopsys.integration.blackduck.version;

import com.synopsys.integration.util.Stringable;

public class BlackDuckVersion extends Stringable {
    private final int major;
    private final int minor;
    private final int patch;

    public BlackDuckVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

	/**
	 * Compares this version of BlackDuck with the one in the other parameter.
	 * 
	 * @param other the BlackDuck version to compare to this one.
	 * @return returns true if the other BlackDuck version is equal to (has the same
	 *         major, minor, and patch) or later (is a greater version according to
	 *         SemVer). Returns false otherwise.
	 */
    public boolean isAtLeast(BlackDuckVersion other) {
        if (major > other.getMajor()) {
            return true;
        }
        if (major < other.getMajor()) {
            return false;
        }
        if (minor > other.getMinor()) {
            return true;
        }
        if (minor < other.getMinor()) {
            return false;
        }
        if (patch > other.getPatch()) {
            return true;
        }
        if (patch < other.getPatch()) {
            return false;
        }
        return true;
    }
}
