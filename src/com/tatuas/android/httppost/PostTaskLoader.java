package com.tatuas.android.httppost;

import java.util.List;

import org.apache.http.NameValuePair;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class PostTaskLoader extends AsyncTaskLoader<PostResult> {
    private String urlStr;
    private PostResult result;
    private List<PostFile> files;
    private List<NameValuePair> nameValuePair;
    private BasicAuth auth;
    private Post req;
    private boolean useSSL;

    public PostTaskLoader(Context context, String urlStr, boolean useSSL) {
        super(context);
        this.urlStr = urlStr;
        this.useSSL = useSSL;
    }

    public void setPostData(List<NameValuePair> nameValuePair) {
        this.nameValuePair = nameValuePair;
    }

    public void setPostFiles(List<PostFile> files) {
        this.files = files;
    }

    public void setBasicAuth(BasicAuth auth) {
        this.auth = auth;
    }

    @Override
    public PostResult loadInBackground() {
        if (nameValuePair == null) {
            return new PostResult("", 0,
                    PostResult.Error.REQUEST_DATA_ERROR, "loader error.");
        }

        req = new Post(urlStr, nameValuePair);
        req.setReadTimeOut(100000);
        req.setConnectTimeOut(150000);

        if (files != null) {
            req.setUploadFiles(files);
        }

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
    public void deliverResult(PostResult data) {
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
