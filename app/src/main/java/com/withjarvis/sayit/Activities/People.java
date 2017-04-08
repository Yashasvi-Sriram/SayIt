package com.withjarvis.sayit.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.withjarvis.sayit.Activities.Account.DeleteAccount;
import com.withjarvis.sayit.Activities.Account.Keys;
import com.withjarvis.sayit.Activities.Account.LogIn;
import com.withjarvis.sayit.Activities.Account.UpdateAccount;
import com.withjarvis.sayit.R;

public class People extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people);
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

}
