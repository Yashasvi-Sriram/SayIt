package com.withjarvis.sayit.Activities.Account;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.withjarvis.sayit.R;

public class SignUp extends AppCompatActivity {

    /* Views */
    RelativeLayout sign_up;
    LinearLayout credentials_div;
    EditText name_input;
    EditText handle_input;
    EditText password_input;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        /* Getting Views */
        this.sign_up = (RelativeLayout) findViewById(R.id.sign_up);
        this.credentials_div = (LinearLayout) this.sign_up.findViewById(R.id.credentials_div);
        this.name_input = (EditText) this.credentials_div.findViewById(R.id.name_input);
        this.handle_input = (EditText) this.credentials_div.findViewById(R.id.handle_input);
        this.password_input = (EditText) this.credentials_div.findViewById(R.id.password_input);
        this.submit = (Button) this.credentials_div.findViewById(R.id.submit);
    }
}
