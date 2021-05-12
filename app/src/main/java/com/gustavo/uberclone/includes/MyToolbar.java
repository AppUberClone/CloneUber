package com.gustavo.uberclone.includes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.gustavo.uberclone.R;

public class MyToolbar {

    public static void show (AppCompatActivity activity, String title, Boolean upButton){
         Toolbar toolbar = activity.findViewById(R.id.toolbar);
         activity.setSupportActionBar(toolbar);
         activity.getSupportActionBar().setTitle(title);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
    }
}
