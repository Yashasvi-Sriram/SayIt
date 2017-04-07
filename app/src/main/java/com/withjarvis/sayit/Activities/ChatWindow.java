package com.withjarvis.sayit.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.withjarvis.sayit.R;

public class ChatWindow extends AppCompatActivity {

    /* Views */
    RelativeLayout chat_window;
    ScrollView chat_slide;
    LinearLayout chat_stack;
    RelativeLayout new_message_factory;
    EditText text_input;
    Button send_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_window);

        /* Getting Views */
        this.chat_window = (RelativeLayout) findViewById(R.id.chat_window);
        this.chat_slide = (ScrollView) this.chat_window.findViewById(R.id.chat_slide);
        this.chat_stack = (LinearLayout) this.chat_slide.findViewById(R.id.chat_stack);
        this.new_message_factory = (RelativeLayout) this.chat_window.findViewById(R.id.new_message_factory);
        this.text_input = (EditText) this.new_message_factory.findViewById(R.id.text_input);
        this.send_message = (Button) this.new_message_factory.findViewById(R.id.send_message);
    }


}
