package com.tatuas.android.httppost;

public class BasicAuth {
    private String userName;
    private String passWord;

    public BasicAuth(String userName, String passWord) {
        this.userName = userName;
        this.passWord = passWord;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getPassWord() {
        return this.passWord;
    }
}
