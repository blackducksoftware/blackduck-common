package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import org.apache.commons.compress.archivers.ArchiveException;

import java.io.File;
import java.io.IOException;

public abstract class ApiScannerInstaller implements ScannerInstaller {

    @Override
    public abstract File installOrUpdateScanner() throws BlackDuckIntegrationException;

    protected abstract HttpUrl getDownloadUrl() throws BlackDuckIntegrationException;

    protected abstract String downloadSignatureScanner(File scannerExpansionDirectory, HttpUrl downloadUrl, String localScannerVersion) throws IOException, IntegrationException, ArchiveException;

}
