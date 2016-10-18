package com.blackducksoftware.integration.hub.api.project.version;

import java.util.List;

public class ComplexLicense {
    private final CodeSharingEnum codeSharing;

    private final String license;

    private final List<ComplexLicense> licenses;

    private final String name;

    private final OwnershipEnum ownership;

    private final LicenseType type;

    public ComplexLicense(final CodeSharingEnum codeSharing, final String license, final List<ComplexLicense> licenses,
            final String name, final OwnershipEnum ownership, final LicenseType type) {
        this.codeSharing = codeSharing;
        this.license = license;
        this.licenses = licenses;
        this.name = name;
        this.ownership = ownership;
        this.type = type;
    }

    public CodeSharingEnum getCodeSharing() {
        return codeSharing;
    }

    public String getLicense() {
        return license;
    }

    public List<ComplexLicense> getLicenses() {
        return licenses;
    }

    public String getName() {
        return name;
    }

    public OwnershipEnum getOwnership() {
        return ownership;
    }

    public LicenseType getType() {
        return type;
    }

}
