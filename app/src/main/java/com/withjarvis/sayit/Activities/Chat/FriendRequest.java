package com.withjarvis.sayit.Activities.Chat;

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
import android.widget.TextView;
import android.widget.Toast;

import com.withjarvis.sayit.Activities.Account.LogIn;
import com.withjarvis.sayit.Activities.People.People;
import com.withjarvis.sayit.JLog.JLog;
import com.withjarvis.sayit.Keys;
import com.withjarvis.sayit.Network.Config;
import com.withjarvis.sayit.Network.Flags;
import com.withjarvis.sayit.Network.SocketStation;
import com.withjarvis.sayit.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class FriendRequest extends AppCompatActivity {

    /* Views */
    RelativeLayout friend_request;
    TextView status;
    TextView name;
    TextView handle;
    EditText message;
    RelativeLayout action_div;
    LinearLayout response_div;
    Button place_request;
    Button accept_request;
    Button reject_request;

    SharedPreferences shp;

    /* Meta data */
    int other_person_pk;
    String other_person_name;
    String other_person_handle;

    String this_person_name;
    String this_person_handle;
    String this_person_password;

    /* Status */

    String fr_status = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_request);

        /* Gets meta data */
        Bundle pressed_list_item = getIntent().getExtras();
        this.other_person_pk = pressed_list_item.getInt(Starter.Keys.OTHER_PERSON_PK);
        this.other_person_name = pressed_list_item.getString(Starter.Keys.OTHER_PERSON_NAME);
        this.other_person_handle = pressed_list_item.getString(Starter.Keys.OTHER_PERSON_HANDLE);

        /* Gets views */
        this.friend_request = (RelativeLayout) findViewById(R.id.friend_request);
        this.status = (TextView) this.friend_request.findViewById(R.id.status);
        this.name = (TextView) this.friend_request.findViewById(R.id.name);
        this.handle = (TextView) this.friend_request.findViewById(R.id.handle);
        this.message = (EditText) this.friend_request.findViewById(R.id.message);
        this.action_div = (RelativeLayout) this.friend_request.findViewById(R.id.action_div);
        this.place_request = (Button) this.action_div.findViewById(R.id.place_request);
        this.response_div = (LinearLayout) this.action_div.findViewById(R.id.response_div);
        this.accept_request = (Button) this.action_div.findViewById(R.id.accept_request);
        this.reject_request = (Button) this.action_div.findViewById(R.id.reject_request);

        /* Applies meta data */
        this.name.setText(this.other_person_name);
        this.handle.setText("@" + this.other_person_handle);

        /* Getting shared preferences */
        this.shp = getSharedPreferences(Keys.SHARED_PREFERENCES.FILE, Context.MODE_PRIVATE);

        this.this_person_name = this.shp.getString(Keys.SHARED_PREFERENCES.NAME, null);
        this.this_person_handle = this.shp.getString(Keys.SHARED_PREFERENCES.HANDLE, null);
        this.this_person_password = this.shp.getString(Keys.SHARED_PREFERENCES.PASSWORD, null);


        /* Place request listener */
        this.place_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fr_status.equals(Flags.ResponseType.FriendRequest.NO_SUCH_FRIEND_REQUEST)) {
                    new PlaceFriendRequest().execute(
                            this_person_handle,
                            this_person_password,
                            message.getText().toString()
                    );
                }
            }
        });

        /* Accept and Reject listeners */
        this.accept_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fr_status.equals(Status.PENDING)) {
                    new AnswerFriendRequest().execute(
                            this_person_handle,
                            this_person_password,
                            Status.ACCEPTED
                    );
                }
            }
        });

        this.reject_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fr_status.equals(Status.PENDING)) {
                    new AnswerFriendRequest().execute(
                            this_person_handle,
                            this_person_password,
                            Status.REJECTED
                    );
                }
            }
        });

        /* Get status of friend request */
        new GetFriendRequestStatus().execute(
                this.this_person_handle,
                this.this_person_password
        );
    }

    /**
     * Status Codes
     */
    private class Status {
        public static final String PENDING = "0";
        public static final String ACCEPTED = "1";
        public static final String REJECTED = "2";
    }

    /**
     * Connects to Server via TCP socket and requests for
     * friend request status with the selected person
     * with the given params
     * <p>
     * Format Sent
     * query_type, handle, password, other person pk
     */
    private class GetFriendRequestStatus extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog = new ProgressDialog(FriendRequest.this);
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
         * params seq : handle, password
         */
        @Override
        protected String doInBackground(String[] params) {
            String handle = params[0];
            String password = params[1];
            // Creating a socket
            try {
                SocketStation ss = new SocketStation(Config.SERVER_IP, Config.SERVER_PORT);

                // Sending QueryType
                ss.send(Flags.QueryType.GET_STATUS_OF_FRIEND_REQUEST);
                // Sending handle
                ss.send(handle);
                // Sending password
                ss.send(password);
                // Sending other person pk
                ss.send(String.valueOf(other_person_pk));

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

            // EOL Exception (Server dies in middle)
            if (response == null) {
                Toast.makeText(FriendRequest.this, "Network Error", Toast.LENGTH_LONG).show();
                return;
            }
            Intent to_people = new Intent(FriendRequest.this, People.class);
            switch (response) {
                case Flags.ResponseType.SUCCESS:
                    try {
                        JSONObject fr_response = new JSONObject(this.json_string_response);
                        fr_status = fr_response.getString(com.withjarvis.sayit.Network.Keys.JSON.STATUS);
                        int sender_pk = fr_response.getInt(com.withjarvis.sayit.Network.Keys.JSON.SENDER_PK);
                        int receiver_pk = fr_response.getInt(com.withjarvis.sayit.Network.Keys.JSON.RECEIVER_PK);
                        String _message = fr_response.getString(com.withjarvis.sayit.Network.Keys.JSON.MESSAGE);

                        if (fr_status.equals(FriendRequest.Status.PENDING)) {
                            if (other_person_pk == receiver_pk) {
                                // this client sent fr to other client
                                response_div.setVisibility(View.GONE);
                                place_request.setVisibility(View.GONE);
                                message.setText(_message);
                                message.setEnabled(false);
                                status.setText("Sent");
                            } else if (other_person_pk == sender_pk) {
                                // this client got fr from other client
                                response_div.setVisibility(View.VISIBLE);
                                place_request.setVisibility(View.GONE);
                                message.setText(_message);
                                message.setEnabled(false);
                                status.setText("Received");
                            }
                        } else if (fr_status.equals(FriendRequest.Status.ACCEPTED)) {
                            response_div.setVisibility(View.GONE);
                            place_request.setVisibility(View.GONE);
                            message.setText(_message);
                            message.setEnabled(false);
                            status.setText("Accepted");
                        } else if (fr_status.equals(FriendRequest.Status.REJECTED)) {
                            response_div.setVisibility(View.GONE);
                            place_request.setVisibility(View.GONE);
                            message.setText(_message);
                            message.setEnabled(false);
                            status.setText("Blocked");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case Flags.ResponseType.FriendRequest.NO_SUCH_FRIEND_REQUEST:
                    fr_status = Flags.ResponseType.FriendRequest.NO_SUCH_FRIEND_REQUEST;
                    response_div.setVisibility(View.GONE);
                    place_request.setVisibility(View.VISIBLE);
                    status.setText("Available");
                    break;
                case Flags.ResponseType.INVALID_CREDENTIALS:
                    Toast.makeText(FriendRequest.this, "Invalid Credentials", Toast.LENGTH_LONG).show();
                    Intent to_log_in = new Intent(FriendRequest.this, LogIn.class);
                    startActivity(to_log_in);
                    break;
                case Flags.ResponseType.INVALID_PK:
                    Toast.makeText(FriendRequest.this, "No such user", Toast.LENGTH_LONG).show();
                    startActivity(to_people);
                    break;
                case Flags.ResponseType.IDENTICAL_PKS:
                    Toast.makeText(FriendRequest.this, "You are always your friend", Toast.LENGTH_LONG).show();
                    startActivity(to_people);
                    break;
                default:
                    Toast.makeText(FriendRequest.this, response, Toast.LENGTH_LONG).show();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            Log.i(JLog.TAG, "Get FriendRequestStatus Cancelled");
        }
    }

    /**
     * Connects to Server via TCP socket and
     * places a friend request with message
     * with the given params
     * <p>
     * Format Sent
     * query_type, handle, password, other person pk, message
     */
    private class PlaceFriendRequest extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog = new ProgressDialog(FriendRequest.this);

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
         * params seq : handle, password, message
         */
        @Override
        protected String doInBackground(String[] params) {
            String handle = params[0];
            String password = params[1];
            String message = params[2];
            // Creating a socket
            try {
                SocketStation ss = new SocketStation(Config.SERVER_IP, Config.SERVER_PORT);

                // Sending QueryType
                ss.send(Flags.QueryType.PLACE_FRIEND_REQUEST);
                // Sending handle
                ss.send(handle);
                // Sending password
                ss.send(password);
                // Sending other person pk
                ss.send(String.valueOf(other_person_pk));
                // Send Message
                ss.send(message);

                // Receive Response
                String response = ss.receive();

                // EOL Exception (Server dies in middle)
                if (response == null) {
                    return null;
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

            // EOL Exception (Server dies in middle)
            if (response == null) {
                Toast.makeText(FriendRequest.this, "Network Error", Toast.LENGTH_LONG).show();
                return;
            }
            Intent to_people = new Intent(FriendRequest.this, People.class);
            switch (response) {
                case Flags.ResponseType.SUCCESS:
                    response_div.setVisibility(View.GONE);
                    place_request.setVisibility(View.GONE);
                    message.setEnabled(false);
                    status.setText("Sent");
                    Toast.makeText(FriendRequest.this, "Friend Request Sent", Toast.LENGTH_LONG).show();
                    break;
                case Flags.ResponseType.INVALID_CREDENTIALS:
                    Toast.makeText(FriendRequest.this, "Invalid Credentials", Toast.LENGTH_LONG).show();
                    Intent to_log_in = new Intent(FriendRequest.this, LogIn.class);
                    startActivity(to_log_in);
                    break;
                case Flags.ResponseType.INVALID_PK:
                    Toast.makeText(FriendRequest.this, "No such user", Toast.LENGTH_LONG).show();
                    startActivity(to_people);
                    break;
                case Flags.ResponseType.FriendRequest.REQUEST_ALREADY_PLACED:
                    Toast.makeText(FriendRequest.this, "Request already placed", Toast.LENGTH_LONG).show();
                    startActivity(to_people);
                    break;
                case Flags.ResponseType.IDENTICAL_PKS:
                    Toast.makeText(FriendRequest.this, "You are always your friend", Toast.LENGTH_LONG).show();
                    startActivity(to_people);
                    break;
                default:
                    Toast.makeText(FriendRequest.this, response, Toast.LENGTH_LONG).show();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            Log.i(JLog.TAG, "Get FriendRequestStatus Cancelled");
        }
    }

    /**
     * Connects to Server via TCP socket and
     * answers friend request
     * with the given params
     * <p>
     * Format Sent
     * query_type, handle, password, other person pk, answer
     */
    private class AnswerFriendRequest extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog = new ProgressDialog(FriendRequest.this);
        String answer;

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
         * params seq : handle, password, response
         */
        @Override
        protected String doInBackground(String[] params) {
            String handle = params[0];
            String password = params[1];
            this.answer = params[2];
            // Creating a socket
            try {
                SocketStation ss = new SocketStation(Config.SERVER_IP, Config.SERVER_PORT);

                // Sending QueryType
                ss.send(Flags.QueryType.ANSWER_FRIEND_REQUEST);
                // Sending handle
                ss.send(handle);
                // Sending password
                ss.send(password);
                // Sending other person pk
                ss.send(String.valueOf(other_person_pk));
                // Send Answer
                ss.send(this.answer);

                // Receive Response
                String response = ss.receive();

                // EOL Exception (Server dies in middle)
                if (response == null) {
                    return null;
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

            // EOL Exception (Server dies in middle)
            if (response == null) {
                Toast.makeText(FriendRequest.this, "Network Error", Toast.LENGTH_LONG).show();
                return;
            }
            Intent to_people = new Intent(FriendRequest.this, People.class);
            switch (response) {
                case Flags.ResponseType.SUCCESS:
                    if (this.answer.equals(FriendRequest.Status.ACCEPTED)) {
                        Toast.makeText(FriendRequest.this, "Accepted", Toast.LENGTH_LONG).show();
                        Starter.startChatWindow(
                                other_person_name,
                                other_person_handle,
                                other_person_pk,
                                FriendRequest.this
                        );
                    } else if (this.answer.equals(FriendRequest.Status.REJECTED)) {
                        Toast.makeText(FriendRequest.this, "Blocked", Toast.LENGTH_LONG).show();
                        startActivity(to_people);
                    }
                    break;
                case Flags.ResponseType.INVALID_CREDENTIALS:
                    Toast.makeText(FriendRequest.this, "Invalid Credentials", Toast.LENGTH_LONG).show();
                    Intent to_log_in = new Intent(FriendRequest.this, LogIn.class);
                    startActivity(to_log_in);
                    break;
                case Flags.ResponseType.INVALID_PK:
                    Toast.makeText(FriendRequest.this, "No such user", Toast.LENGTH_LONG).show();
                    startActivity(to_people);
                    break;
                case Flags.ResponseType.FriendRequest.REQUEST_ALREADY_ANSWERED:
                    Toast.makeText(FriendRequest.this, "Request already answered", Toast.LENGTH_LONG).show();
                    startActivity(to_people);
                    break;
                case Flags.ResponseType.IDENTICAL_PKS:
                    Toast.makeText(FriendRequest.this, "You are always your friend", Toast.LENGTH_LONG).show();
                    startActivity(to_people);
                    break;
                case Flags.ResponseType.FriendRequest.NO_SUCH_FRIEND_REQUEST:
                    Toast.makeText(FriendRequest.this, "No friend request to accept", Toast.LENGTH_LONG).show();
                    startActivity(to_people);
                    break;
                default:
                    Toast.makeText(FriendRequest.this, response, Toast.LENGTH_LONG).show();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            Log.i(JLog.TAG, "Get FriendRequestStatus Cancelled");
        }
    }

}
