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

import com.withjarvis.sayit.JLog.JLog;
import com.withjarvis.sayit.Keys;
import com.withjarvis.sayit.Network.Config;
import com.withjarvis.sayit.Network.Flags;
import com.withjarvis.sayit.Network.SocketStation;
import com.withjarvis.sayit.R;

import java.io.IOException;

public class DeleteAccount extends AppCompatActivity {

    /* Views */
    RelativeLayout delete_account;
    LinearLayout credentials_div;
    EditText password_input;
    Button submit;

    SharedPreferences shp;

    boolean can_send_request = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_account);

        /* Gets shared preferences */
        this.shp = getSharedPreferences(Keys.SHARED_PREFERENCES.FILE, Context.MODE_PRIVATE);

        /* Getting Views */
        this.delete_account = (RelativeLayout) findViewById(R.id.delete_account);
        this.credentials_div = (LinearLayout) this.delete_account.findViewById(R.id.credentials_div);
        this.password_input = (EditText) this.credentials_div.findViewById(R.id.password_input);
        this.submit = (Button) this.credentials_div.findViewById(R.id.submit);

        /* Submit listener */
        this.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* todo cleanse inputs */
                String handle = shp.getString(Keys.SHARED_PREFERENCES.HANDLE, null);
                String password = password_input.getText().toString();

                assert handle != null;

                if (password.equals("")) {
                    Toast.makeText(DeleteAccount.this, "Empty Inputs not allowed", Toast.LENGTH_LONG).show();
                    return;
                }

                if (can_send_request) {
                    new DeleteAccountRequest().execute(
                            handle,
                            password
                    );
                }
            }
        });
    }

    /**
     * Connects to Server via TCP socket and requests for delete account
     * with the given params
     * <p>
     * Format Sent
     * query_type, handle, password (blocks in that order)
     */
    private class DeleteAccountRequest extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog = new ProgressDialog(DeleteAccount.this);

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
         * params seq : handle, password
         */
        @Override
        protected String doInBackground(String[] params) {
            String handle = params[0];
            String password = params[1];
            can_send_request = false;
            // Creating a socket
            try {
                SocketStation ss = new SocketStation(Config.SERVER_IP, Config.SERVER_PORT);

                // Sending QueryType
                ss.send(Flags.QueryType.DELETE_ACCOUNT);
                // Sending handle
                ss.send(handle);
                // Sending password
                ss.send(password);

                // Receive Response
                String response = ss.receive();

                // EOL Exception (Server dies in middle)
                if (response == null) {
                    return null;
                }

                // If delete account success remove stored name, handle, password
                if (response.equals(Flags.ResponseType.SUCCESS)) {
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
                Toast.makeText(DeleteAccount.this, "Network Error", Toast.LENGTH_LONG).show();
                return;
            }

            Intent to_log_in = new Intent(DeleteAccount.this, LogIn.class);
            switch (response) {
                case Flags.ResponseType.SUCCESS:
                    startActivity(to_log_in);
                    Toast.makeText(DeleteAccount.this, "De-activated successfully", Toast.LENGTH_LONG).show();
                    break;
                case Flags.ResponseType.INVALID_CREDENTIALS:
                    startActivity(to_log_in);
                    Toast.makeText(DeleteAccount.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    Toast.makeText(DeleteAccount.this, "Log In to continue", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(DeleteAccount.this, response, Toast.LENGTH_LONG).show();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            can_send_request = true;
            Log.i(JLog.TAG, "Delete Account Request Cancelled");
        }
    }

}
