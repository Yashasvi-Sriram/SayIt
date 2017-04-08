package com.withjarvis.sayit.Activities.Account;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.withjarvis.sayit.R;

public class DeleteAccount extends AppCompatActivity {

    /* Views */
    RelativeLayout delete_account;
    LinearLayout credentials_div;
    EditText handle_input;
    EditText password_input;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_account);

        /* Getting Views */
        this.delete_account = (RelativeLayout) findViewById(R.id.delete_account);
        this.credentials_div = (LinearLayout) this.delete_account.findViewById(R.id.credentials_div);
        this.handle_input = (EditText) this.credentials_div.findViewById(R.id.old_handle_input);
        this.password_input = (EditText) this.credentials_div.findViewById(R.id.password_input);
        this.submit = (Button) this.credentials_div.findViewById(R.id.submit);
    }
}
