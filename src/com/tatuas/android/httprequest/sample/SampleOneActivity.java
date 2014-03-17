package com.tatuas.android.httprequest.sample;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SampleOneActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button getBtn = (Button) findViewById(R.id.getBtn);
        getBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GetManager get = new GetManager(v.getContext());
                int LOADER_ID = 1;
                getSupportLoaderManager().initLoader(LOADER_ID,
                        null, get.getLoaderCallbacks("http://httpbin.org/get"));
            }
        });

        Button postBtn = (Button) findViewById(R.id.postBtn);
        postBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PostManager post = new PostManager(v.getContext());
                int LOADER_ID = 4;
                getSupportLoaderManager().initLoader(LOADER_ID,
                        null, post.getLoaderCallbacks("http://httpbin.org/post"));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}