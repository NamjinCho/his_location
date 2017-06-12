package com.example.mpterm;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by 남지니 on 2016-06-08.
 */
public class NoteFrag1 extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("Fragment 1", "onCreateView");
//---Inflate the layout for this fragment---
        return inflater.inflate(
                R.layout.fragment1, container, false);
    }
    public void onStart()
    {
        super.onStart();

    }
}
