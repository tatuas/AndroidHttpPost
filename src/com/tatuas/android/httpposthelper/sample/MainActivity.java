package com.tatuas.android.httpposthelper.sample;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.tatuas.android.httpposthelper.PostResult;
import com.tatuas.android.httpposthelper.PostFile;
import com.tatuas.android.httpposthelper.PostTaskLoader;

import android.os.Bundle;
import android.app.ProgressDialog;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
        LoaderCallbacks<PostResult> {
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int LOADER_ID1 = 1;
        getSupportLoaderManager().initLoader(LOADER_ID1, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public Loader<PostResult> onCreateLoader(int arg0, Bundle arg1) {
        dialog = new ProgressDialog(this);
        dialog.setTitle("Wait");
        dialog.setMessage("uploading data...");
        dialog.setCancelable(false);
        dialog.show();
        String urlStr = "http://httpbin.org/post";
        List<PostFile> files = null;
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair("sampleKey", "sampleValue"));

        PostTaskLoader loader = new PostTaskLoader(this, urlStr, false);
        loader.setPostData(nameValuePair);
        loader.setPostFiles(files);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<PostResult> arg0, PostResult arg1) {
        // TODO Auto-generated method stub
        String toast = "STATUS:" + String.valueOf(arg1.getResultCode());
        toast += ">>>>>" + arg1.getResultData();
        dialog.dismiss();
        if (arg1.getResultCode() == HttpStatus.SC_OK) {
            Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "error " + toast, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<PostResult> arg0) {
        // TODO Auto-generated method stub
    }
}