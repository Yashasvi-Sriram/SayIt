package com.withjarvis.sayit.Activities.Account;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.withjarvis.sayit.R;

public class LogIn extends AppCompatActivity {

    /* Views */
    RelativeLayout log_in;
    LinearLayout credentials_div;
    EditText handle_input;
    EditText password_input;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);

        /* Getting Views */
        this.log_in = (RelativeLayout) findViewById(R.id.log_in);
        this.credentials_div = (LinearLayout) this.log_in.findViewById(R.id.credentials_div);
        this.handle_input = (EditText) this.credentials_div.findViewById(R.id.handle_input);
        this.password_input = (EditText) this.credentials_div.findViewById(R.id.password_input);
        this.submit = (Button) this.credentials_div.findViewById(R.id.submit);
    }
}
