package com.tatuas.android.httprequest;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class GetTaskLoader extends AsyncTaskLoader<GetResult> {
    private String urlStr;
    private GetResult result;
    private BasicAuth auth;
    private Get req;
    private boolean useSSL;

    public GetTaskLoader(Context context, String urlStr, boolean useSSL) {
        super(context);
        this.urlStr = urlStr;
        this.useSSL = useSSL;
    }

    public void setBasicAuth(BasicAuth auth) {
        this.auth = auth;
    }

    @Override
    public GetResult loadInBackground() {
        req = new Get(urlStr);
        req.setReadTimeOut(100000);
        req.setConnectTimeOut(150000);

        if (useSSL) {
            return req.execOnSSL(auth);
        } else {
            return req.exec(auth);
        }
    }

    @Override
    public void stopLoading() {
        if (req != null) {
            req.close();
        }
        super.stopLoading();
    }

    @Override
    public void deliverResult(GetResult data) {
        if (isReset()) {
            return;
        }

        result = data;

        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (result != null) {
            deliverResult(result);
        }

        if (takeContentChanged() || result == null) {
            forceLoad();
        }
    }
}
