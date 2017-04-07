package com.withjarvis.sayit.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.withjarvis.sayit.R;

public class Launcher extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);

        Intent to_chat_window = new Intent(this, ChatWindow.class);
        this.startActivity(to_chat_window);
    }
}
