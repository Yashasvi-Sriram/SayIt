package com.withjarvis.sayit.Activities.Account;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.withjarvis.sayit.Activities.People.People;
import com.withjarvis.sayit.JLog.JLog;
import com.withjarvis.sayit.Keys;
import com.withjarvis.sayit.Network.Config;
import com.withjarvis.sayit.Network.Flags;
import com.withjarvis.sayit.Network.SocketStation;
import com.withjarvis.sayit.R;

import java.io.IOException;

public class UpdateAccount extends AppCompatActivity {

    /* Views */
    RelativeLayout update_account;
    LinearLayout credentials_div;
    EditText old_password_input;
    EditText new_name_input;
    EditText new_password_input;
    EditText confirm_password_input;
    Button submit;

    SharedPreferences shp;

    boolean can_send_request = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_account);

        /* Getting Views */
        this.update_account = (RelativeLayout) findViewById(R.id.update_account);
        this.credentials_div = (LinearLayout) this.update_account.findViewById(R.id.credentials_div);
        this.old_password_input = (EditText) this.credentials_div.findViewById(R.id.old_password_input);
        this.new_name_input = (EditText) this.credentials_div.findViewById(R.id.new_name_input);
        this.new_password_input = (EditText) this.credentials_div.findViewById(R.id.new_password_input);
        this.confirm_password_input = (EditText) this.credentials_div.findViewById(R.id.confirm_password_input);
        this.submit = (Button) this.credentials_div.findViewById(R.id.submit);

        /* Getting previously stored information */
        this.shp = getSharedPreferences(Keys.SHARED_PREFERENCES.FILE, Context.MODE_PRIVATE);

        String name = this.shp.getString(Keys.SHARED_PREFERENCES.NAME, null);
        String handle = this.shp.getString(Keys.SHARED_PREFERENCES.HANDLE, null);

        /* Auto fill */
        this.new_name_input.setText(name);

        /* Submit Listener */
        this.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String handle = shp.getString(Keys.SHARED_PREFERENCES.HANDLE, null);
                String old_password = old_password_input.getText().toString();
                String new_name = new_name_input.getText().toString();
                String new_password = new_password_input.getText().toString();
                String confirm_password = confirm_password_input.getText().toString();

                if (!new_password.equals(confirm_password)) {
                    Toast.makeText(UpdateAccount.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (can_send_request) {
                    new UpdateAccountRequest().execute(
                            handle,
                            old_password,
                            new_name,
                            new_password
                    );
                }
            }
        });
    }

    /**
     * Connects to Server via TCP socket and requests for update account
     * with the given params
     * <p>
     * Format Sent
     * query_type, handle, old_password, new_name, new_password (blocks in that order)
     */
    private class UpdateAccountRequest extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog = new ProgressDialog(UpdateAccount.this);

        @Override
        protected void onPreExecute() {
            this.progressDialog.setMessage("Connecting ...");
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            this.progressDialog.setCancelable(false);
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.setIndeterminate(true);
            this.progressDialog.show();
        }

        /**
         * params seq : handle, old_password, new_name, new_password
         */
        @Override
        protected String doInBackground(String[] params) {
            String handle = params[0];
            String old_password = params[1];
            String new_name = params[2];
            String new_password = params[3];
            can_send_request = false;

            // Creating a socket
            try {
                SocketStation ss = new SocketStation(Config.SERVER_IP, Config.SERVER_PORT);

                // Sending QueryType
                ss.send(Flags.QueryType.UPDATE_ACCOUNT);
                // Sending handle
                ss.send(handle);
                // Sending password
                ss.send(old_password);
                // Sending new name
                ss.send(new_name);
                // Sending new password
                ss.send(new_password);

                // Receive Response
                String response = ss.receive();

                // EOL Exception (Server dies in middle)
                if (response == null) {
                    return null;
                }
                // If update account success store new name, handle, new password
                else if (response.equals(Flags.ResponseType.SUCCESS)) {
                    SharedPreferences.Editor shEditor = shp.edit();
                    shEditor.putString(Keys.SHARED_PREFERENCES.NAME, new_name);
                    shEditor.putString(Keys.SHARED_PREFERENCES.HANDLE, handle);
                    shEditor.putString(Keys.SHARED_PREFERENCES.PASSWORD, new_password);
                    shEditor.commit();
                } else if (response.equals(Flags.ResponseType.INVALID_CREDENTIALS)) {
                    SharedPreferences.Editor shEditor = shp.edit();
                    shEditor.putString(Keys.SHARED_PREFERENCES.NAME, null);
                    shEditor.putString(Keys.SHARED_PREFERENCES.HANDLE, null);
                    shEditor.putString(Keys.SHARED_PREFERENCES.PASSWORD, null);
                    shEditor.commit();
                }

                return response;

            } catch (IOException e) {
                e.printStackTrace();
                return "Server connection refused";
            }
        }

        @Override
        protected void onPostExecute(String response) {
            this.progressDialog.dismiss();

            can_send_request = true;
            // EOL Exception (Server dies in middle)
            if (response == null) {
                Toast.makeText(UpdateAccount.this, "Network Error", Toast.LENGTH_LONG).show();
                return;
            }

            switch (response) {
                case Flags.ResponseType.SUCCESS:
                    Toast.makeText(UpdateAccount.this, "Updated successfully", Toast.LENGTH_LONG).show();
                    Intent to_people = new Intent(UpdateAccount.this, People.class);
                    startActivity(to_people);
                    break;
                case Flags.ResponseType.INVALID_CREDENTIALS:
                    Toast.makeText(UpdateAccount.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    Toast.makeText(UpdateAccount.this, "Log In to continue", Toast.LENGTH_SHORT).show();
                    Intent to_log_in = new Intent(UpdateAccount.this, People.class);
                    startActivity(to_log_in);
                    break;
                default:
                    Toast.makeText(UpdateAccount.this, response, Toast.LENGTH_LONG).show();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            can_send_request = true;
            Log.i(JLog.TAG, "Update Account Request Cancelled");
        }
    }

}
