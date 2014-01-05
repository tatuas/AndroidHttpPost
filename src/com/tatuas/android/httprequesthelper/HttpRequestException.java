package com.tatuas.android.httprequesthelper;

public class HttpRequestException extends Exception {

    private static final long serialVersionUID = 3631844720553767144L;

    private String msg;

    public HttpRequestException(String msg) {
        this.msg = msg;
    }

    public String getMessage() {
        return this.msg;
    }
}
