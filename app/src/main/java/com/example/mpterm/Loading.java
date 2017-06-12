package com.example.mpterm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by 남지니 on 2016-06-18.
 */
public class Loading extends Activity {
    @Override
    public void onCreate(Bundle ise)
    {
        super.onCreate(ise);
        setContentView(R.layout.loading);



        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(getApplicationContext(),Login.class));
                        finish();
                    }
                }, 3000);
    }
}
