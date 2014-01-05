package com.tatuas.android.httprequesthelper;

import java.io.File;

public class UploadFile {
    private File uploadFile;
    private String tagName;
    private String mimeType;

    public UploadFile(String tagName, String mimeType, File uploadFile) {
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
