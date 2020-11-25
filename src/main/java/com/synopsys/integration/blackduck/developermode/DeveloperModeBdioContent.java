package com.synopsys.integration.blackduck.developermode;

import com.synopsys.integration.util.Stringable;

public class DeveloperModeBdioContent extends Stringable {
    private String fileName;
    private String content;

    public DeveloperModeBdioContent(final String fileName, final String content) {
        this.fileName = fileName;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContent() {
        return content;
    }
}
