package com.withjarvis.sayit.Activities;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;

import com.withjarvis.sayit.Activities.Account.LogIn;
import com.withjarvis.sayit.Device;
import com.withjarvis.sayit.R;

public class Launcher extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);

        /* Gets dimensions of device in px */
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Device.WIDTH = size.x;
        Device.HEIGHT = size.y;

        /* Goes to log in activity */
        Intent to_log_in = new Intent(this, LogIn.class);
        this.startActivity(to_log_in);

    }
}
