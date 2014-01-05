package com.tatuas.android.httprequesthelper.sample;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.tatuas.android.httprequesthelper.HttpRequest;
import com.tatuas.android.httprequesthelper.HttpRequestEventListener;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.widget.Toast;

public class UploadTask extends AsyncTask<String, Integer, Integer> implements
        HttpRequestEventListener {

    ProgressDialog dialog;
    Context context;
    HttpRequest req;

    public UploadTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setTitle("Wait");
        dialog.setMessage("uploading data...");
        dialog.setCancelable(true);
        dialog.show();
    }

    public void deleteCacheDirFile() {
        File[] files = context.getCacheDir().listFiles();
        for (File file : files) {
            file.delete();
        }
    }

    @Override
    protected Integer doInBackground(String... params) {
        String url = "http://httpbin.org/post";

        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair("sampleKey",
                "sampleValue"));

        req = new HttpRequest(url, nameValuePair);
        req.setOnHttpRequestListener(this);
        req.setReadTimeOut(100000);
        req.setConnectTimeOut(150000);

        // String fileName = params[0];
        // File file = new File(fileName);
        // List<UploadFile> files = new ArrayList<UploadFile>();
        // files.add(new UploadFile("uploadFile0", "image/jpeg", file));
        // req.setUploadFiles(files);

        // BasicAuth auth = new BasicAuth("user", "password");
        // req.execRequest(auth);

        req.execRequest();

        SystemClock.sleep(10000);

        return 0;
    }

    @Override
    protected void onCancelled() {
        if (req != null) {
            req.close();
        }

        if (dialog != null) {
            dialog.dismiss();
            Toast.makeText(context, "canceled.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (dialog != null) {
            dialog.dismiss();
            Toast.makeText(context, "post completed.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onRequestSuccess(String resultData) {
        // TODO Auto-generated method stub
        writeToLogFile(resultData);
    }

    @Override
    public void onRequestError(int errorCode) {
        // TODO Auto-generated method stub
        writeToLogFile(String.valueOf(errorCode));
    }

    @Override
    public void onNetworkDisable() {
        // TODO Auto-generated method stub
        writeToLogFile("disable");
    }

    @Override
    public void onRequestDataError(String errorMsg) {
        // TODO Auto-generated method stub
        writeToLogFile(errorMsg);
    }

    public void writeToLogFile(String str) {
        if (str != null) {
            File file2 = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/log.html");
            try {
                FileWriter fw = new FileWriter(file2);
                fw.write(str);
                fw.close();
            } catch (Exception e) {
            }
        }
    }
}
