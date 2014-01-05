package com.tatuas.android.httprequesthelper.sample;

import java.io.File;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new UploadTask(this).execute(getStoragePath());
    }

    private String getStoragePath() {
        String storage = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + File.separator;
        storage += "sampleFile.jpg";

        return storage;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
