package com.tatuas.android.httprequesthelper;

public interface HttpRequestEventListener {
    public void onRequestSuccess(String resultData);
    public void onRequestError(int errorCode);
    public void onNetworkDisable();
    public void onRequestDataError(String errorMsg);
}
