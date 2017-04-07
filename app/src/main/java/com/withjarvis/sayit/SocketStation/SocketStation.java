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
 */
public class SocketStation {
    // Must be same for client and server
    // Cannot be a digit
    // Must not be an empty string
    // Cannot be empty string or char
    private static final char head_body_delimiter = '\n';

    // Max number of digits in length of body received
    // Must be same for client and server
    private static final int r_body_limit = 10;

    private String serverAddress; // Must not be empty
    private int port;  // Port range 0 - 65536

    private Socket socket;

    private BufferedWriter writer;
    private BufferedReader reader;

    public SocketStation(String serverAddress, int port) {
        this.connect(serverAddress, port);
    }

    /**
     * Calling this method will close the previously connected socket if any
     */
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
     * Packet Format = body_length(delimiter)body
     */
    public boolean send(String body) {
        try {
            int data_length = body.length();
            StringBuilder packet_builder = new StringBuilder();
            packet_builder.append(String.valueOf(data_length))
                    .append(SocketStation.head_body_delimiter)
                    .append(body);
            System.out.println(packet_builder.toString());
            this.writer.write(packet_builder.toString());
            this.writer.flush();
            Log.i(JLog.TAG, "Sent data : to " + this.serverAddress + " on port " + this.port);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Expected Packet Format = body_length(delimiter)body'\n'
     * Returns body
     */
    public String receive() {
        try {
            int no_digits = 0;
            StringBuilder body_length_builder = new StringBuilder();
            // Handles (nothing)(delimiter) case (not req if format is proper)
            body_length_builder.append('0');
            // Finds body length
            char letter;
            while (true) {
                letter = (char) this.reader.read();
                if (letter == SocketStation.head_body_delimiter)
                    break;
                body_length_builder.append(letter);
                // Limits body size
                no_digits++;
                if (no_digits > SocketStation.r_body_limit)
                    return null;
            }
            int body_length = Integer.parseInt(body_length_builder.toString());
            // Extracts the body
            StringBuilder body_builder = new StringBuilder();
            String chunk;
            while (body_length > 0) {
                // This will at most read total body
                chunk = this.reader.readLine();
                body_builder.append(chunk).append('\n');
                // + 1 is for the extra \n which is silently removed in readline()
                body_length -= (chunk.length() + 1);
            }
            String body_with_newline = body_builder.toString();
            // Removes the last newline appended for reading purpose
            Log.i(JLog.TAG, "Received data : from " + this.serverAddress + " on port " + this.port);
            return body_with_newline.substring(0, body_with_newline.length() - 1);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Closes the socket if not already closed
     */
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
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.close();
    }

//    public static void main(String[] args) {
//        SocketStation a = new SocketStation("localhost", 8036);
//        a.send("jklsdkfa;klufieif4987&*&(*&))&$#&_()_)(78~");
//        a.send("");
//        a.send("#&_()_)(78~");
//        a.send("\n");
//        a.send("This is second one and should be interpreted like wise ...");
//        System.out.println(a.receive());
//        System.out.println(a.receive());
//        System.out.println(a.receive());
//        System.out.println(a.receive());
//        System.out.println(a.receive());
//        System.out.println(a.receive());
//    }

}
