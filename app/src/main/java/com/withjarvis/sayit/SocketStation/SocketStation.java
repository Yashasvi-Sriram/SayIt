package com.withjarvis.sayit.SocketStation;

import android.util.Log;

import com.withjarvis.sayit.JLog.JLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Wrapper for socket formation and communication
 * */
public class SocketStation {
    private String serverAddress; // Must not be an empty string
    private int port;  // Port range 0 - 65536

    private Socket socket;

    private BufferedWriter writer;
    private BufferedReader reader;

    public SocketStation(String serverAddress, int port) {
        this.connect(serverAddress, port);
    }

    /**
     * Calling this method will close the previously connected socket if any
     * */
    public void connect(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
        try {
            // Close the previous instance of socket if any
            if (this.socket != null)
                if (this.socket.isClosed())
                    this.socket.close();
            // Create a new socket
            this.socket = new Socket(this.serverAddress, this.port);
            // Keep track of it's input and output streams
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Log.i(JLog.TAG, "Connection Established to " + this.socket.getRemoteSocketAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes the param and writes it and then flushes it using the BufferedOutputStream
     * */
    public boolean send(String data) {
        try {
            this.writer.write(data);
            this.writer.flush();
            Log.i(JLog.TAG, "Sent data : " + data + " to " + this.serverAddress + " on port " + this.port);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * On the InputStream reads a LINE!!
     * If the InputStream has data without an new line (\n or \r or \n\r)
     *      then it blocks until such a new line char is received in InputStream
     * But if the socket closes in such case, then it reads that remaining data left on buffer
     * */
    public String receive() {
        try {
            String data = this.reader.readLine();
            if (data != null) {
                Log.i(JLog.TAG, "Received data : " + data + " from " + this.serverAddress + " on port " + this.port);
            }
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Closes the socket if not already closed
     * */
    public void close() {
        try {
            // If the socket is not already closed close it
            if (!this.socket.isClosed())
                this.socket.close();
            Log.i(JLog.TAG, "Connection closed properly with " + this.socket.getRemoteSocketAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * If the GC collects this instance release the connection by closing the socket
     * DO NOT depend on this !!
     * Close the sockets by yourself religiously.
     * */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.close();
    }

//    public static void main(String[] args) {
//        SocketStation a = new SocketStation("localhost", 8036);
//        a.send("1");
//        a.send("2");
//        System.out.println(a.receive());
//        a.close();
//    }

}
