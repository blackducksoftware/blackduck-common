package com.blackduck.integration.blackduck.codelocation.signaturescanner.command;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class ScanCliMetadata {
    @SerializedName("documentVersion")
    private String documentVersion;
    
    @SerializedName("toolVersion")
    private String toolVersion;
    
    @SerializedName("os")
    private String os;
    
    @SerializedName("arch")
    private String arch;
    
    // Default constructor
    public ScanCliMetadata() {
    }
    
    // Constructor with all fields
    public ScanCliMetadata(String documentVersion, String toolVersion, String os, String arch) {
        this.documentVersion = documentVersion;
        this.toolVersion = toolVersion;
        this.os = os;
        this.arch = arch;
    }
    
    // Getters
    public String getDocumentVersion() {
        return documentVersion;
    }
    
    public String getToolVersion() {
        return toolVersion;
    }
    
    public String getOs() {
        return os;
    }
    
    public String getArch() {
        return arch;
    }
    
    // Setters
    public void setDocumentVersion(String documentVersion) {
        this.documentVersion = documentVersion;
    }
    
    public void setToolVersion(String toolVersion) {
        this.toolVersion = toolVersion;
    }
    
    public void setOs(String os) {
        this.os = os;
    }
    
    public void setArch(String arch) {
        this.arch = arch;
    }
    
    /**
     * Reads metadata from a JSON file and deserializes it to a ScanCliMetadata object.
     * The JSON file should contain an array with a single metadata object.
     * 
     * @param metadataFile The metadata JSON file to read
     * @return ScanCliMetadata object containing the deserialized data
     * @throws IOException if there's an error reading the file
     */
    public static ScanCliMetadata getMetadata(File metadataFile) throws IOException {
        Gson gson = new Gson();
        
        try (FileReader reader = new FileReader(metadataFile)) {
            // The JSON file contains an array with a single metadata object
            Type listType = new TypeToken<List<ScanCliMetadata>>(){}.getType();
            List<ScanCliMetadata> metadataList = gson.fromJson(reader, listType);
            
            if (metadataList != null && !metadataList.isEmpty()) {
                return metadataList.get(0);
            } else {
                throw new IOException("No metadata found in the file or file is empty");
            }
        }
    }
}
