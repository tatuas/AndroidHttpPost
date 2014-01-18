package com.tatuas.android.httppost;

import java.io.File;

public class PostFile {
    private File uploadFile;
    private String tagName;
    private String mimeType;

    public PostFile(String tagName, String mimeType, File uploadFile) {
        this.tagName = tagName;
        this.mimeType = mimeType;
        this.uploadFile = uploadFile;
    }

    public File getUploadFile() {
        return uploadFile;
    }

    public String getTagName() {
        return tagName;
    }

    public String getMimeType() {
        return mimeType;
    }
}
