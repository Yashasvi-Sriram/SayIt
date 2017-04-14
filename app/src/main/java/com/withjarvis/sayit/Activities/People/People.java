package com.withjarvis.sayit.Activities.People;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.withjarvis.sayit.Activities.Account.DeleteAccount;
import com.withjarvis.sayit.Activities.Account.LogIn;
import com.withjarvis.sayit.Activities.Account.UpdateAccount;
import com.withjarvis.sayit.JLog.JLog;
import com.withjarvis.sayit.Keys;
import com.withjarvis.sayit.Network.Config;
import com.withjarvis.sayit.Network.Flags;
import com.withjarvis.sayit.Network.SocketStation;
import com.withjarvis.sayit.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class People extends AppCompatActivity {

    /* Views */
    RelativeLayout people;
    RelativeLayout search_div;
    EditText search_text_input;
    Button submit;
    RelativeLayout friends_list_div;
    ListView friends_list;
    RelativeLayout others_list_div;
    ListView others_list;
    BottomNavigationView friends_selector;

    SharedPreferences shp;

    boolean can_send_request = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people);

        /* Getting shared preferences */
        this.shp = getSharedPreferences(Keys.SHARED_PREFERENCES.FILE, Context.MODE_PRIVATE);

        /* Getting Views */
        this.people = (RelativeLayout) findViewById(R.id.people);
        this.search_div = (RelativeLayout) this.people.findViewById(R.id.search_div);
        this.search_text_input = (EditText) this.search_div.findViewById(R.id.search_text_input);
        this.submit = (Button) this.search_div.findViewById(R.id.submit);
        this.friends_list_div = (RelativeLayout) this.people.findViewById(R.id.friends_list_div);
        this.friends_list = (ListView) this.friends_list_div.findViewById(R.id.friends_list);
        this.others_list_div = (RelativeLayout) this.people.findViewById(R.id.others_list_div);
        this.others_list = (ListView) this.others_list_div.findViewById(R.id.others_list);
        this.friends_selector = (BottomNavigationView) this.people.findViewById(R.id.friends_selector);

        /* Friends Selector Listener */
        this.friends_selector.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.people_menu_show_friends:
                                friends_list_div.setVisibility(View.VISIBLE);
                                others_list_div.setVisibility(View.GONE);
                                return true;
                            case R.id.people_menu_show_others:
                                others_list_div.setVisibility(View.VISIBLE);
                                friends_list_div.setVisibility(View.GONE);
                                return true;
                        }
                        return false;
                    }
                }
        );

        /* Submit Listener */
        this.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String handle = shp.getString(Keys.SHARED_PREFERENCES.HANDLE, null);
                String password = shp.getString(Keys.SHARED_PREFERENCES.PASSWORD, null);
                String regex_string = search_text_input.getText().toString();

                if (can_send_request) {
                    new GetFilteredPeople().execute(
                            handle,
                            password,
                            regex_string
                    );
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.people, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.people_menu_update_account:
                this.update_account();
                break;
            case R.id.people_menu_delete_account:
                this.delete_account();
                break;
            case R.id.people_menu_log_out:
                this.logout();
                break;
            default:
                break;
        }
        return true;
    }

    private void logout() {
        /* Remove the stored credentials and go to log in activity */
        SharedPreferences shp = getSharedPreferences(Keys.SHARED_PREFERENCES.FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor shEditor = shp.edit();
        shEditor.putString(Keys.SHARED_PREFERENCES.NAME, null);
        shEditor.putString(Keys.SHARED_PREFERENCES.HANDLE, null);
        shEditor.putString(Keys.SHARED_PREFERENCES.PASSWORD, null);
        shEditor.apply();

        Intent to_log_in = new Intent(this, LogIn.class);
        startActivity(to_log_in);
    }

    private void delete_account() {
        Intent to_delete_account = new Intent(this, DeleteAccount.class);
        startActivity(to_delete_account);
    }

    private void update_account() {
        Intent to_update_account = new Intent(this, UpdateAccount.class);
        startActivity(to_update_account);
    }

    /**
     * Connects to Server via TCP socket and requests for
     * users with name matching regex string
     * with the given params
     * <p>
     * Format Sent
     * query_type, handle, password, regex_string (blocks in that order)
     */
    private class GetFilteredPeople extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog = new ProgressDialog(People.this);
        String json_string_response;

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
         * params seq : handle, password, regex_string
         */
        @Override
        protected String doInBackground(String[] params) {
            String handle = params[0];
            String password = params[1];
            String regex_string = params[2];
            can_send_request = false;
            // Creating a socket
            try {
                SocketStation ss = new SocketStation(Config.SERVER_IP, Config.SERVER_PORT);

                // Sending QueryType
                ss.send(Flags.QueryType.FILTER_USERS);
                // Sending handle
                ss.send(handle);
                // Sending password
                ss.send(password);
                // Sending regex string
                ss.send(regex_string);

                // Receive Response
                String response = ss.receive();

                // EOL Exception (Server dies in middle)
                if (response == null) {
                    return null;
                } else if (response.equals(Flags.ResponseType.SUCCESS)) {
                    // Receive all matched users
                    this.json_string_response = ss.receive();
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
                Toast.makeText(People.this, "Network Error", Toast.LENGTH_LONG).show();
                return;
            }
            switch (response) {
                case Flags.ResponseType.SUCCESS:
                    try {
                        JSONObject total_list = new JSONObject(this.json_string_response);
                        JSONArray _friends_list = total_list.getJSONArray(com.withjarvis.sayit.Network.Keys.JSON.FRIENDS_LIST);
                        JSONArray _others_list = total_list.getJSONArray(com.withjarvis.sayit.Network.Keys.JSON.OTHERS_LIST);

                        // Indicate no match found else no way to understand that
                        if (_friends_list.length() == 0 && _others_list.length() == 0) {
                            Toast.makeText(People.this, "No match found", Toast.LENGTH_LONG).show();
                        }

                        friends_list.setAdapter(new FriendsListAdapter(People.this, _friends_list));
                        others_list.setAdapter(new OthersListAdapter(People.this, _others_list));
                    } catch (JSONException e) {
                        Toast.makeText(People.this, "Invalid Response", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    break;
                case Flags.ResponseType.INVALID_CREDENTIALS:
                    Toast.makeText(People.this, "Invalid Credentials", Toast.LENGTH_LONG).show();
                    Intent to_log_in = new Intent(People.this, LogIn.class);
                    startActivity(to_log_in);
                    break;
                case Flags.ResponseType.INVALID_REGEX:
                    Toast.makeText(People.this, "Invalid Search String", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(People.this, response, Toast.LENGTH_LONG).show();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            can_send_request = true;
            Log.i(JLog.TAG, "Get Filtered People Cancelled");
        }
    }
}
