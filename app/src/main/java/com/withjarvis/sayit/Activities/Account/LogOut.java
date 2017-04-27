package com.withjarvis.sayit.Activities.Account;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.withjarvis.sayit.JLog.JLog;
import com.withjarvis.sayit.Network.Config;
import com.withjarvis.sayit.Network.Flags;
import com.withjarvis.sayit.Network.SocketStation;

import java.io.IOException;

public class LogOut {
    Context context;
    String handle;
    String password;
    static boolean can_send_request = true;

    public LogOut(Context context, String handle, String password) {
        this.context = context;
        this.handle = handle;
        this.password = password;
        new LogOutRequest().execute(
                handle,
                password
        );
    }

    /**
     * Connects to Server via TCP socket and hits it to indicate that logging out
     * <p>
     * Format Sent
     * query_type, handle, password (blocks in that order)
     */
    private class LogOutRequest extends AsyncTask<String, String, String> {

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
                ss.send(Flags.QueryType.LOG_OUT);
                // Sending handle
                ss.send(handle);
                // Sending password
                ss.send(password);

                // Receive Response
                String response = ss.receive();
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
                Toast.makeText(context, "Network Error", Toast.LENGTH_LONG).show();
                return;
            }
            switch (response) {
                case Flags.ResponseType.SUCCESS:
                    Toast.makeText(context, "Logout successful", Toast.LENGTH_LONG).show();
                    break;
                case Flags.ResponseType.INVALID_CREDENTIALS:
                    Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            can_send_request = true;
            Log.i(JLog.TAG, "Log Out Cancelled");
        }
    }
}