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

public class LogIn extends AppCompatActivity {

    /* Views */
    RelativeLayout log_in;
    LinearLayout credentials_div;
    EditText handle_input;
    EditText password_input;
    Button submit;
    Button to_sign_up;

    SharedPreferences shp;

    /* Prevents simultaneous requests (rare case, click submit while automatic login occurs) */
    boolean can_send_request = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);

        /* Tries to automatically login with previously used handle and password */
        this.shp = getSharedPreferences(Keys.SHARED_PREFERENCES.FILE, Context.MODE_PRIVATE);

        String name = this.shp.getString(Keys.SHARED_PREFERENCES.NAME, null);
        String handle = this.shp.getString(Keys.SHARED_PREFERENCES.HANDLE, null);
        String password = this.shp.getString(Keys.SHARED_PREFERENCES.PASSWORD, null);

        if (name != null && handle != null && password != null) {
            /* Possible handle and password */
            if (can_send_request) {
                new LogInRequest().execute(
                        handle,
                        password
                );
            }
        }

        /* Getting Views */
        this.log_in = (RelativeLayout) findViewById(R.id.log_in);
        this.credentials_div = (LinearLayout) this.log_in.findViewById(R.id.credentials_div);
        this.handle_input = (EditText) this.credentials_div.findViewById(R.id.handle_input);
        this.password_input = (EditText) this.credentials_div.findViewById(R.id.password_input);
        this.submit = (Button) this.credentials_div.findViewById(R.id.submit);
        this.to_sign_up = (Button) this.log_in.findViewById(R.id.to_sign_up);

        /* Submit listener */
        this.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* todo cleanse inputs */
                String handle = handle_input.getText().toString();
                String password = password_input.getText().toString();

                if (handle.equals("") || password.equals("")) {
                    Toast.makeText(LogIn.this, "Empty Inputs not allowed", Toast.LENGTH_LONG).show();
                    return;
                }

                if (can_send_request) {
                    new LogInRequest().execute(
                            handle,
                            password
                    );
                }
            }
        });

        /* To Sign Up listener */
        this.to_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent to_sign_up = new Intent(LogIn.this, SignUp.class);
                startActivity(to_sign_up);
            }
        });

    }

    @Override
    public void onBackPressed() {
    }

    /**
     * Connects to Server via TCP socket and requests for log in
     * with the given params
     * <p>
     * Format Sent
     * query_type, handle, password (blocks in that order)
     */
    private class LogInRequest extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog = new ProgressDialog(LogIn.this);

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
                ss.send(Flags.QueryType.LOG_IN);
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

                // If login success update stored name, handle, password
                // as login can happen from stored credentials or input credentials
                if (response.equals(Flags.ResponseType.SUCCESS)) {
                    String name = ss.receive();
                    SharedPreferences.Editor shEditor = shp.edit();
                    shEditor.putString(Keys.SHARED_PREFERENCES.NAME, name);
                    shEditor.putString(Keys.SHARED_PREFERENCES.HANDLE, handle);
                    shEditor.putString(Keys.SHARED_PREFERENCES.PASSWORD, password);
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
                Toast.makeText(LogIn.this, "Network Error", Toast.LENGTH_LONG).show();
                return;
            }
            switch (response) {
                case Flags.ResponseType.SUCCESS:
                    Intent to_people = new Intent(LogIn.this, People.class);
                    startActivity(to_people);
                    break;
                case Flags.ResponseType.INVALID_CREDENTIALS:
                    Toast.makeText(LogIn.this, "Invalid Credentials", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(LogIn.this, response, Toast.LENGTH_LONG).show();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            can_send_request = true;
            Log.i(JLog.TAG, "Log In Cancelled");
        }
    }

}
