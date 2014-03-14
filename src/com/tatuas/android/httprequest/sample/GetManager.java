package com.tatuas.android.httprequest.sample;

import org.apache.http.HttpStatus;

import com.tatuas.android.httprequest.GetResult;
import com.tatuas.android.httprequest.GetTaskLoader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.Toast;

public class GetManager {
    private Context context;
    private static final int LOADER_ID = 1;

    public GetManager(Context context) {
        this.context = context;
    }

    public LoaderCallbacks<GetResult> getLoaderCallbacks(final String uri) {
        return new LoaderCallbacks<GetResult>() {

            @Override
            public Loader<GetResult> onCreateLoader(int arg0, Bundle arg1) {
                boolean useSSL = false;
                GetTaskLoader loader = new GetTaskLoader(context, uri, useSSL);
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<GetResult> loader,
                    GetResult result) {
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
            public void onLoaderReset(Loader<GetResult> arg0) {
                // TODO Auto-generated method stub
                
            }

        };
    }

}
