package com.tatuas.android.httppost.sample;

import org.apache.http.HttpStatus;

import com.tatuas.android.httppost.GetResult;
import com.tatuas.android.httppost.GetTaskLoader;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.widget.Toast;

public class SubActivity extends FragmentActivity implements LoaderCallbacks<GetResult>{
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        final int LOADER_ID1 = 1;
        getSupportLoaderManager().initLoader(LOADER_ID1, null, this);
 }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sub, menu);
        return true;
    }

    @Override
    public Loader<GetResult> onCreateLoader(int arg0, Bundle arg1) {
        dialog = new ProgressDialog(this);
        dialog.setTitle("Wait");
        dialog.setMessage("uploading data...");
        dialog.setCancelable(false);
        dialog.show();
        String urlStr = "http://httpbin.org/get";
        GetTaskLoader loader = new GetTaskLoader(this, urlStr, false);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<GetResult> arg0, GetResult arg1) {
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
    public void onLoaderReset(Loader<GetResult> arg0) {
        // TODO Auto-generated method stub
        
    }

}
