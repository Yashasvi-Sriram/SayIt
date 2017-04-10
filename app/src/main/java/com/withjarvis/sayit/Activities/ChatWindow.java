package com.withjarvis.sayit.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.withjarvis.sayit.Activities.Account.LogIn;
import com.withjarvis.sayit.Activities.People.People;
import com.withjarvis.sayit.Colors;
import com.withjarvis.sayit.Device;
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

public class ChatWindow extends AppCompatActivity {

    /* Views */
    RelativeLayout chat_window;
    ScrollView chat_slide;
    LinearLayout in_sync_stack;
    LinearLayout new_messages_stack;
    RelativeLayout new_message_factory;
    EditText text_input;
    Button send_message_btn;

    int receiver_pk;
    String receiver_name;
    String receiver_handle;

    SharedPreferences shp;
    String sender_name;
    String sender_handle;
    String sender_password;

    JSONArray new_messages_buffer;
    JSONArray in_sync_buffer;
    String latest_sync_timestamp = "";

    private boolean sync_on = true;

    private boolean can_send_request = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_window);

        /* Gets meta data */
        Bundle pressed_list_item = getIntent().getExtras();
        this.receiver_name = pressed_list_item.getString("name");
        this.receiver_handle = pressed_list_item.getString("handle");
        this.receiver_pk = pressed_list_item.getInt("pk");

        /* Getting Shared Preferences */
        this.shp = getSharedPreferences(Keys.SHARED_PREFERENCES.FILE, Context.MODE_PRIVATE);

        this.sender_name = shp.getString(Keys.SHARED_PREFERENCES.NAME, null);
        this.sender_handle = shp.getString(Keys.SHARED_PREFERENCES.HANDLE, null);
        this.sender_password = shp.getString(Keys.SHARED_PREFERENCES.PASSWORD, null);

        /* Getting Views */
        this.chat_window = (RelativeLayout) findViewById(R.id.chat_window);
        this.chat_slide = (ScrollView) this.chat_window.findViewById(R.id.chat_slide);
        //
        this.in_sync_stack = (LinearLayout) this.chat_slide.findViewById(R.id.in_sync_stack);
        this.new_messages_stack = (LinearLayout) this.chat_slide.findViewById(R.id.new_messages_stack);
        //
        this.new_message_factory = (RelativeLayout) this.chat_window.findViewById(R.id.new_message_factory);
        this.text_input = (EditText) this.new_message_factory.findViewById(R.id.text_input);
        this.send_message_btn = (Button) this.new_message_factory.findViewById(R.id.send_message_btn);

        /* Instantiating new_messages_buffer and in_sync_buffer */
        this.new_messages_buffer = new JSONArray();
        this.in_sync_buffer = new JSONArray();

        /* Send listener */
        this.send_message_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String new_message = text_input.getText().toString();
                if (new_message.equals("")) {
                    return;
                }
                appendToNewMessageBuffer(new_message);
                text_input.setText("");
            }
        });

        /* initializing chat */
        new SyncMessages().execute(
                sender_handle,
                sender_password
        );

        /* init Periodic Sync */
        new PeriodicSync().start();

        /* going to latest message */
        this.scrollToLatestMessage();
    }

    private void scrollToLatestMessage() {
        chat_slide.post(new Runnable() {
            @Override
            public void run() {
                chat_slide.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private RelativeLayout getNewMessageStub(String msg_content) {
        final TextView content = new TextView(this);
        TextView sender_handle = new TextView(this);
        RelativeLayout vessel = new RelativeLayout(this);

        RelativeLayout.LayoutParams content_lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        RelativeLayout.LayoutParams sender_handle_lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        RelativeLayout.LayoutParams vessel_lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        /* message content */
        content.setId(1);
        content.setText(msg_content);
        content.setTextColor(Colors.WHITE);
        content.setTextSize(com.withjarvis.sayit.Config.TEXT_SIZE);
        content.setPadding(20, 20, 20, 20);

        /* sender handle */
        Resources res = getResources();
        sender_handle.setText(String.format(res.getString(R.string.handle), this.sender_handle));
        sender_handle.setTextColor(Colors.PINK);
        sender_handle.setPadding(10, 10, 10, 10);
        sender_handle_lp.addRule(RelativeLayout.BELOW, content.getId());

        /* message stub */
        vessel.setPadding(5, 5, 5, 5);

        /* positioning and constraints */
        content.setBackgroundResource(R.drawable.sent_message_bg);
        content_lp.addRule(RelativeLayout.ALIGN_PARENT_END);
        sender_handle_lp.addRule(RelativeLayout.ALIGN_PARENT_END);
        vessel_lp.setMarginStart((int) (Device.WIDTH * 0.55));

        /* sets layout params to views  */
        content.setLayoutParams(content_lp);
        sender_handle.setLayoutParams(sender_handle_lp);
        vessel.setLayoutParams(vessel_lp);

        /* makes hierarchy */
        vessel.addView(content);
        vessel.addView(sender_handle);

        return vessel;
    }

    private RelativeLayout getNewMessageStub(JSONObject msg) throws JSONException {
        String msg_content = msg.getString(com.withjarvis.sayit.Network.Keys.JSON.CONTENT);
        int msg_sender_pk = msg.getInt(com.withjarvis.sayit.Network.Keys.JSON.SENDER_PK);
        int msg_receiver_pk = msg.getInt(com.withjarvis.sayit.Network.Keys.JSON.RECEIVER_PK);

        Log.i(JLog.TAG, String.valueOf(msg_sender_pk));
        Log.i(JLog.TAG, String.valueOf(msg_receiver_pk));

        TextView content = new TextView(this);
        TextView sender_handle = new TextView(this);
        RelativeLayout vessel = new RelativeLayout(this);

        RelativeLayout.LayoutParams content_lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        RelativeLayout.LayoutParams sender_handle_lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        RelativeLayout.LayoutParams vessel_lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        /* message content */
        content.setText(msg_content);
        content.setId(1);
        content.setTextSize(com.withjarvis.sayit.Config.TEXT_SIZE);
        content.setPadding(20, 20, 20, 20);

        /* sender handle */
        Resources res = getResources();
        sender_handle.setTextColor(Colors.PINK);
        sender_handle.setPadding(10, 10, 10, 10);
        sender_handle_lp.addRule(RelativeLayout.BELOW, content.getId());

        /* message stub */
        vessel.setPadding(5, 5, 5, 5);
        if (this.receiver_pk == msg_receiver_pk) {
            // message sent
            content.setTextColor(Colors.WHITE);
            content.setBackgroundResource(R.drawable.sent_message_bg);
            content_lp.addRule(RelativeLayout.ALIGN_PARENT_END);

            sender_handle.setText(String.format(res.getString(R.string.handle), this.sender_handle));
            sender_handle_lp.addRule(RelativeLayout.ALIGN_PARENT_END);

            vessel_lp.setMarginStart((int) (Device.WIDTH * 0.55));
        } else if (this.receiver_pk == msg_sender_pk) {
            // message received
            content.setTextColor(Colors.TEAL);
            content.setBackgroundResource(R.drawable.received_message_bg);
            content_lp.addRule(RelativeLayout.ALIGN_PARENT_START);

            sender_handle.setText(String.format(res.getString(R.string.handle), this.receiver_handle));
            sender_handle_lp.addRule(RelativeLayout.ALIGN_PARENT_START);

            vessel_lp.setMarginEnd((int) (Device.WIDTH * 0.55));
        }

        /* sets layout params to views  */
        content.setLayoutParams(content_lp);
        sender_handle.setLayoutParams(sender_handle_lp);
        vessel.setLayoutParams(vessel_lp);

        /* makes hierarchy */
        vessel.addView(content);
        vessel.addView(sender_handle);

        return vessel;
    }

    private void appendToNewMessageBuffer(String new_message) {
        this.new_messages_buffer.put(new_message);
        this.new_messages_stack.addView(this.getNewMessageStub(new_message));
        this.scrollToLatestMessage();
    }

    private void appendToInSyncBuffer(JSONObject object) throws JSONException {
        this.in_sync_buffer.put(object);
        this.in_sync_stack.addView(getNewMessageStub(object));
    }

    private void renderSyncedMessages(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject ith_message = jsonArray.getJSONObject(i);
                appendToInSyncBuffer(ith_message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /* Slide only if new messages from the other person in the chat comes */
        if (jsonArray.length() > this.new_messages_buffer.length()) {
            this.scrollToLatestMessage();
        }

        /* clearing new message stack and buffer */
        this.new_messages_stack.removeAllViews();
        new_messages_buffer = new JSONArray();
    }

    /**
     * Connects to Server via TCP socket and sends new messages
     * <p>
     * Format Sent
     * query_type, handle, password, pk of receiver, json string of new messages(blocks in that order)
     */
    private class FlushNewMessageBuffer extends AsyncTask<String, String, String> {

        /**
         * params seq : handle, password
         */
        @Override
        protected String doInBackground(String[] params) {
            can_send_request = false;
            /* if now new messages in new message buffer then no need to flush new_message_buffer */
            if (new_messages_buffer.length() == 0) {
                return Flags.Local.NO_NEW_MESSAGES;
            }
            String handle = params[0];
            String password = params[1];
            // Creating a socket
            try {
                SocketStation ss = new SocketStation(Config.SERVER_IP, Config.SERVER_PORT);

                // Sending QueryType
                ss.send(Flags.QueryType.NEW_MESSAGE);
                // Sending handle
                ss.send(handle);
                // Sending password
                ss.send(password);
                // Sending pk of receiver
                ss.send(String.valueOf(receiver_pk));
                // Sending json string of new messages
                ss.send(new_messages_buffer.toString());

                // Receive Response
                String response = ss.receive();

                // EOL Exception (Server dies in middle)
                if (response == null) {
                    return null;
                }

                if (response.equals(Flags.ResponseType.INVALID_CREDENTIALS)) {
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
            can_send_request = true;
            // EOL Exception (Server dies in middle)
            if (response == null) {
                Toast.makeText(ChatWindow.this, "Network Error", Toast.LENGTH_LONG).show();
                return;
            }
            switch (response) {
                case Flags.ResponseType.SUCCESS:
                    new SyncMessages().execute(
                            sender_handle,
                            sender_password
                    );
                    Log.i(JLog.TAG, "New Messages Sent from " + sender_handle + " to " + receiver_handle);
                    break;
                case Flags.ResponseType.INVALID_CREDENTIALS:
                    Toast.makeText(ChatWindow.this, "Invalid Credentials", Toast.LENGTH_LONG).show();
                    Intent to_log_in = new Intent(ChatWindow.this, LogIn.class);
                    startActivity(to_log_in);
                    break;
                case Flags.ResponseType.INVALID_PK:
                    Toast.makeText(ChatWindow.this, "The user recipient has deactivated", Toast.LENGTH_LONG).show();
                    Intent to_people = new Intent(ChatWindow.this, People.class);
                    startActivity(to_people);
                    break;
                case Flags.Local.NO_NEW_MESSAGES:
                    new SyncMessages().execute(
                            sender_handle,
                            sender_password
                    );
                    break;
                default:
                    Toast.makeText(ChatWindow.this, response, Toast.LENGTH_LONG).show();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            can_send_request = true;
            Log.i(JLog.TAG, "Sending new messages cancelled");
        }
    }

    /**
     * Connects to Server via TCP socket and requests for messages
     * <p>
     * Format Sent
     * query_type, handle, password, pk of receiver, timestamp (blocks in that order)
     */
    private class SyncMessages extends AsyncTask<String, String, String> {

        JSONArray response_messages;

        /**
         * params seq : handle, password
         */
        @Override
        protected String doInBackground(String[] params) {
            can_send_request = true;
            String handle = params[0];
            String password = params[1];
            // Creating a socket
            try {
                SocketStation ss = new SocketStation(Config.SERVER_IP, Config.SERVER_PORT);

                // Sending QueryType
                ss.send(Flags.QueryType.FILTER_MESSAGES);
                // Sending handle
                ss.send(handle);
                // Sending password
                ss.send(password);
                // Sending pk of receiver
                ss.send(String.valueOf(receiver_pk));
                // Sending latest sync timestamp
                ss.send(latest_sync_timestamp);

                // Receive Response
                String response = ss.receive();
                // All messages from the sent timestamp
                this.response_messages = new JSONArray(ss.receive());
                // Updating timestamp
                latest_sync_timestamp = ss.receive();

                // EOL Exception (Server dies in middle)
                if (response == null) {
                    return null;
                }

                if (response.equals(Flags.ResponseType.INVALID_CREDENTIALS)) {
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
            } catch (JSONException e) {
                e.printStackTrace();
                return "Response not in proper format";
            }
        }

        @Override
        protected void onPostExecute(String response) {
            can_send_request = true;

            // EOL Exception (Server dies in middle)
            if (response == null) {
                Toast.makeText(ChatWindow.this, "Network Error", Toast.LENGTH_LONG).show();
                return;
            }
            switch (response) {
                case Flags.ResponseType.SUCCESS:
                    Log.i(JLog.TAG, latest_sync_timestamp);
                    renderSyncedMessages(this.response_messages);
                    break;
                case Flags.ResponseType.INVALID_CREDENTIALS:
                    Toast.makeText(ChatWindow.this, "Invalid Credentials", Toast.LENGTH_LONG).show();
                    Intent to_log_in = new Intent(ChatWindow.this, LogIn.class);
                    startActivity(to_log_in);
                    break;
                default:
                    Toast.makeText(ChatWindow.this, response, Toast.LENGTH_LONG).show();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            can_send_request = true;
            Log.i(JLog.TAG, "Sync Messages cancelled");
        }
    }

    private class PeriodicSync extends Thread {

        int time_period = 5; // in seconds

        @Override
        public void run() {
            while (sync_on) {
                try {
                    // Sleep
                    Thread.sleep(1000 * this.time_period);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (can_send_request) {
                    new FlushNewMessageBuffer().execute(
                            sender_handle,
                            sender_password
                    );
                }
            }

            Log.i(JLog.TAG, "Exiting Periodic Sync");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        this.sync_on = false;
    }

}