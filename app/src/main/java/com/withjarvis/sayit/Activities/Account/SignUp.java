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

import com.withjarvis.sayit.Activities.People;
import com.withjarvis.sayit.JLog.JLog;
import com.withjarvis.sayit.Keys;
import com.withjarvis.sayit.Network.Config;
import com.withjarvis.sayit.Network.Flags;
import com.withjarvis.sayit.Network.SocketStation;
import com.withjarvis.sayit.R;

import java.io.IOException;

public class SignUp extends AppCompatActivity {

    /* Views */
    RelativeLayout sign_up;
    LinearLayout credentials_div;
    EditText name_input;
    EditText handle_input;
    EditText password_input;
    Button submit;
    Button to_log_in;

    boolean can_send_request = true;

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
        this.to_log_in = (Button) this.sign_up.findViewById(R.id.to_log_in);

        /* Submit listener */
        this.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* todo cleanse inputs */
                String name = name_input.getText().toString();
                String handle = handle_input.getText().toString();
                String password = password_input.getText().toString();
                if (name.equals("") || handle.equals("") || password.equals("")) {
                    Toast.makeText(SignUp.this, "Empty Inputs not allowed", Toast.LENGTH_LONG).show();
                    return;
                }

                if (name.length() > 50 || handle.length() > 50 || password.length() > 50) {
                    Toast.makeText(SignUp.this, "Max characters allowed 50", Toast.LENGTH_LONG).show();
                    return;
                }

                if (can_send_request) {
                    new SignUpRequest().execute(
                            name,
                            handle,
                            password
                    );
                }
            }
        });

        /* To Sign Up listener */
        this.to_log_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent to_sign_up = new Intent(SignUp.this, LogIn.class);
                startActivity(to_sign_up);
            }
        });

    }

    @Override
    public void onBackPressed() {
    }

    /**
     * Connects to Server via TCP socket and requests for sign up
     * with the given params
     * <p>
     * Format Sent
     * query_type, name, handle, password (blocks in that order)
     */
    private class SignUpRequest extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog = new ProgressDialog(SignUp.this);

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
         * params seq : name, handle, password
         */
        @Override
        protected String doInBackground(String[] params) {
            String name = params[0];
            String handle = params[1];
            String password = params[2];

            can_send_request = false;
            // Creating a socket
            try {
                SocketStation ss = new SocketStation(Config.SERVER_IP, Config.SERVER_PORT);

                // Sending QueryType
                ss.send(Flags.QueryType.SIGN_UP);
                // Sending name
                ss.send(name);
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

                // If sign up success store new name, handle, password
                // for automatic login next time user opens app
                if (response.equals(Flags.ResponseType.SUCCESS)) {
                    SharedPreferences shp = getSharedPreferences(Keys.SHARED_PREFERENCES.FILE, Context.MODE_PRIVATE);
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
                Toast.makeText(SignUp.this, "Network Error", Toast.LENGTH_LONG).show();
                return;
            }
            switch (response) {
                case Flags.ResponseType.SUCCESS:
                    Intent to_people = new Intent(SignUp.this, People.class);
                    startActivity(to_people);
                    Toast.makeText(SignUp.this, "Account Created", Toast.LENGTH_SHORT).show();
                    break;
                case Flags.ResponseType.HANDLE_ALREADY_EXIST:
                    Toast.makeText(SignUp.this, "Handle Already Exists", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(SignUp.this, response, Toast.LENGTH_LONG).show();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            Log.i(JLog.TAG, "Sign Up Cancelled");
        }
    }

}
