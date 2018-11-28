package com.synopsys.integration.blackduck.codelocation.bdioupload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.synopsys.integration.bdio.BdioReader;
import com.synopsys.integration.bdio.SimpleBdioFactory;
import com.synopsys.integration.bdio.model.SimpleBdioDocument;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.service.model.HubMediaTypes;

//The 'Simple' entry point to upload files.
public class SimpleUploadService {
    private UploadRunner uploadRunner;

    public SimpleUploadService(UploadRunner uploadRunner){
        this.uploadRunner = uploadRunner;
    }

    public UploadBatchOutput uploadNameMappedFiles(Map<String, File> codeLocationNameMap) throws IOException, HubIntegrationException {
        UploadBatch uploadBatch = new UploadBatch();
        codeLocationNameMap.entrySet().stream()
            .map(pair -> UploadTarget.createDefault(pair.getKey(), pair.getValue()))
            .forEach(uploadBatch::addUploadTarget);
        return uploadRunner.executeUploads(uploadBatch);
    }

    public UploadBatchOutput uploadBdioFiles(List<File> bdioFiles) throws IOException, HubIntegrationException {
        Map<String, File> codeLocationNameMap = new HashMap<>();
        for (File bdioFile : bdioFiles){
            String codeLocationName = readCodeLocationName(bdioFile);
            codeLocationNameMap.put(codeLocationName, bdioFile);
        }
        return uploadNameMappedFiles(codeLocationNameMap);
    }

    private String readCodeLocationName(File bdioFile) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(bdioFile)){
            try (BdioReader bdioReader = new BdioReader(new Gson(), fileInputStream)){
                SimpleBdioDocument simpleBdioDocument = bdioReader.readSimpleBdioDocument();
                String codeLocationName = simpleBdioDocument.billOfMaterials.spdxName;
                return codeLocationName;
            }
        }
    }

    public UploadBatchOutput uploadBdio(File bdioFile) throws IOException, HubIntegrationException {
        String codeLocationName = readCodeLocationName(bdioFile);
        return uploadFile(codeLocationName, bdioFile);
    }

    public UploadBatchOutput uploadFile(String codeLocationName, File uploadFile) throws HubIntegrationException {
        UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(UploadTarget.createDefault(codeLocationName, uploadFile));
        return uploadRunner.executeUploads(uploadBatch);
    }
}
