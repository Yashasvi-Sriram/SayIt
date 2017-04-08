package com.withjarvis.sayit.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.withjarvis.sayit.R;

public class People extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people);
    }

    @Override
    public void onBackPressed() {
    }
}
