package com.withjarvis.sayit.Activities.Account;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.withjarvis.sayit.Activities.ChatWindow;
import com.withjarvis.sayit.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PeopleListAdapter extends BaseAdapter {
    Context context;
    JSONArray users;

    private static LayoutInflater inflater = null;

    public PeopleListAdapter(Context mainActivity, String json_string) {
        this.context = mainActivity;
        try {
            this.users = new JSONArray(json_string);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return this.users.length();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = inflater.inflate(R.layout.people_list_item,null);

        Holder holder=new Holder(rowView);
        holder.setText(position);

        /* To ChatWindow */
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject user = (JSONObject) users.get(position);

                    Intent to_chat_window = new Intent(context,ChatWindow.class);
                    to_chat_window.putExtra("name",user.getString(com.withjarvis.sayit.Network.Keys.JSON.NAME));
                    to_chat_window.putExtra("handle",user.getString(com.withjarvis.sayit.Network.Keys.JSON.HANDLE));
                    to_chat_window.putExtra("pk",user.getInt(com.withjarvis.sayit.Network.Keys.JSON.PK));
                    context.startActivity(to_chat_window);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return rowView;
    }

    public class Holder {
        LinearLayout people_list_layout;
        TextView userName , handle;

        public Holder(View rowView){
            this.people_list_layout = (LinearLayout) rowView.findViewById(R.id.people_list_layout);
            this.userName = (TextView) this.people_list_layout.findViewById(R.id.userName);
            this.handle = (TextView) this.people_list_layout.findViewById(R.id.handle);
        }

        public void setText(final int position){
            try {
                JSONObject user = (JSONObject) users.get(position);

                this.userName.setText(user.getString(com.withjarvis.sayit.Network.Keys.JSON.NAME));
                this.handle.setText(user.getString(com.withjarvis.sayit.Network.Keys.JSON.HANDLE));
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}