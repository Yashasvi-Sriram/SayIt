package com.withjarvis.sayit.Activities.People;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.withjarvis.sayit.Activities.Chat.Starter;
import com.withjarvis.sayit.Network.Keys;
import com.withjarvis.sayit.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FriendsListAdapter extends BaseAdapter {
    private Context context;
    private JSONArray users;

    private static LayoutInflater inflater = null;

    public FriendsListAdapter(Context mainActivity, JSONArray list) {
        this.context = mainActivity;
        this.users = list;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return this.users.length();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = inflater.inflate(R.layout.people_list_item, null);

        Holder holder = new Holder(rowView);
        holder.setText(position);

        /* To ChatWindow */
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject user = (JSONObject) users.get(position);

                    Starter.startChatWindow(
                            user.getString(Keys.JSON.NAME),
                            user.getString(Keys.JSON.HANDLE),
                            user.getInt(Keys.JSON.PK),
                            context
                    );

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return rowView;
    }

    private class Holder {
        RelativeLayout people_list_item_layout;
        TextView name, handle;

        public Holder(View rowView) {
            this.people_list_item_layout = (RelativeLayout) rowView.findViewById(R.id.people_list_item_layout);
            this.name = (TextView) this.people_list_item_layout.findViewById(R.id.name);
            this.handle = (TextView) this.people_list_item_layout.findViewById(R.id.handle);
        }

        public void setText(final int position) {
            try {
                JSONObject user = (JSONObject) users.get(position);

                this.name.setText(user.getString(com.withjarvis.sayit.Network.Keys.JSON.NAME));
                this.handle.setText("@" + user.getString(com.withjarvis.sayit.Network.Keys.JSON.HANDLE));
            } catch (JSONException e) {
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