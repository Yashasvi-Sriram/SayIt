package com.withjarvis.sayit.Activities;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.widget.RelativeLayout;

import com.withjarvis.sayit.Activities.Account.LogIn;
import com.withjarvis.sayit.Device;
import com.withjarvis.sayit.R;

public class Launcher extends AppCompatActivity {

    RelativeLayout launcher;

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

        this.launcher = (RelativeLayout) findViewById(R.id.launcher);

        this.launcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Goes to log in activity */
                Intent to_log_in = new Intent(Launcher.this, LogIn.class);
                startActivity(to_log_in);
            }
        });

    }
}
