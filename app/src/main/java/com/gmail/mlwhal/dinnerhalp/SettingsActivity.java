package com.gmail.mlwhal.dinnerhalp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import android.view.Menu;
import android.view.MenuItem;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Display SettingsFragment as main content
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Switch for different action bar item clicks
        switch (id) {
            case R.id.action_add_dinner:
                Intent intent = new Intent(this, AddDinnerActivity.class);
                this.startActivity(intent);
                return true;

            case R.id.action_search:
                Intent intent2 = new Intent(this, MainActivity.class);
                intent2.putExtra("FRAGMENT_TRACKER", 0);
                this.startActivity(intent2);
                return true;

            case R.id.action_manage:
                Intent intent3 = new Intent(this, MainActivity.class);
                intent3.putExtra("FRAGMENT_TRACKER", 1);
                this.startActivity(intent3);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            //Load preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        }

    }
}
