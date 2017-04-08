package com.withjarvis.sayit.Activities.Account;

import android.content.Context;
import android.content.SharedPreferences;
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
    EditText old_handle_input;
    EditText old_password_input;
    EditText new_name_input;
    EditText new_handle_input;
    EditText new_password_input;
    EditText confirm_password_input;
    Button submit;

    SharedPreferences shp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_account);

        /* Getting Views */
        this.update_account = (RelativeLayout) findViewById(R.id.update_account);
        this.credentials_div = (LinearLayout) this.update_account.findViewById(R.id.credentials_div);
        this.old_handle_input = (EditText) this.credentials_div.findViewById(R.id.old_handle_input);
        this.old_password_input = (EditText) this.credentials_div.findViewById(R.id.old_password_input);
        this.new_name_input = (EditText) this.credentials_div.findViewById(R.id.new_name_input);
        this.new_handle_input = (EditText) this.credentials_div.findViewById(R.id.new_handle_input);
        this.new_password_input = (EditText) this.credentials_div.findViewById(R.id.new_password_input);
        this.confirm_password_input = (EditText) this.credentials_div.findViewById(R.id.confirm_password_input);
        this.submit = (Button) this.credentials_div.findViewById(R.id.submit);

        /* Getting previously stored information */
        this.shp = getSharedPreferences(Keys.SHARED_PREFERENCES.FILE, Context.MODE_PRIVATE);

        String name = this.shp.getString(Keys.SHARED_PREFERENCES.NAME, null);
        String handle = this.shp.getString(Keys.SHARED_PREFERENCES.HANDLE, null);

        this.old_handle_input.setText(handle);

        this.new_name_input.setText(name);
        this.new_handle_input.setText(handle);
    }
}
