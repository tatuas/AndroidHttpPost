package com.tatuas.android.httprequest.sample;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.tatuas.android.httprequest.PostFile;
import com.tatuas.android.httprequest.PostResult;
import com.tatuas.android.httprequest.PostTaskLoader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.Toast;

public class PostManager {
    private Context context;
    private static final int LOADER_ID = 4;

    public PostManager(Context context) {
        this.context = context;
    }

    public LoaderCallbacks<PostResult> getLoaderCallbacks(final String uri) {
        return new LoaderCallbacks<PostResult>() {

            @Override
            public Loader<PostResult> onCreateLoader(int arg0, Bundle arg1) {
                boolean useSSL = false;
                List<PostFile> files = null;
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                nameValuePair.add(new BasicNameValuePair("sampleKey",
                        "sampleValue"));

                PostTaskLoader loader = new PostTaskLoader(context, uri, useSSL);
                loader.setPostData(nameValuePair);
                loader.setPostFiles(files);
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<PostResult> loader,
                    PostResult result) {
                if (loader != null) {
                    loader.stopLoading();
                    loader.reset();
                }

                ((FragmentActivity) context).getSupportLoaderManager()
                        .destroyLoader(LOADER_ID);

                if (result.getResultCode() == HttpStatus.SC_OK) {
                    Toast.makeText(context, result.getResultData(),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "NG", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onLoaderReset(Loader<PostResult> arg0) {
                // TODO Auto-generated method stub

            }

        };
    }

}
