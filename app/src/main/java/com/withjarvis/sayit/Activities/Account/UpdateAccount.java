package com.withjarvis.sayit.Activities.Account;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.withjarvis.sayit.R;

public class UpdateAccount extends AppCompatActivity {


    /* Views */
    RelativeLayout update_account;
    LinearLayout credentials_div;
    EditText name_input;
    EditText handle_input;
    EditText old_password_input;
    EditText new_password_input;
    EditText confirm_password_input;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_account);

        /* Getting Views */
        this.update_account = (RelativeLayout) findViewById(R.id.update_account);
        this.credentials_div = (LinearLayout) this.update_account.findViewById(R.id.credentials_div);
        this.name_input = (EditText) this.credentials_div.findViewById(R.id.name_input);
        this.handle_input = (EditText) this.credentials_div.findViewById(R.id.handle_input);
        this.old_password_input = (EditText) this.credentials_div.findViewById(R.id.old_password_input);
        this.new_password_input = (EditText) this.credentials_div.findViewById(R.id.new_password_input);
        this.confirm_password_input = (EditText) this.credentials_div.findViewById(R.id.confirm_password_input);
        this.submit = (Button) this.credentials_div.findViewById(R.id.submit);
    }
}
