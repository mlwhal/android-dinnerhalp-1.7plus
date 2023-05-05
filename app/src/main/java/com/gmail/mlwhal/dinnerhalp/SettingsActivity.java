package com.gmail.mlwhal.dinnerhalp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Objects;


public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    static SharedPreferences mSharedPrefs;
    //Set up a change listener to deal with changes to night mode preference
    private static final SharedPreferences.OnSharedPreferenceChangeListener mListener = new
            SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d(TAG, "Prefs changed; key is " + key);
            if (key.equals("pref_theme") ) {
                String darkModePref = sharedPreferences.getString(key, "MODE_NIGHT_FOLLOW_SYSTEM");
                switch (darkModePref) {
                    case "MODE_NIGHT_FOLLOW_SYSTEM":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);;
                        break;
                    case "MODE_NIGHT_NO":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    case "MODE_NIGHT_YES":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                }
                Log.d(TAG, "Night mode set to " + darkModePref);
            }
        }
    };

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

            //Check API version; below 29 there is no dark mode so the prefs omit it
//            Log.d(TAG, "Build code is " + Build.VERSION.SDK_INT);
            if (Build.VERSION.SDK_INT >= 29) {
                //Load preferences from an XML resource
                addPreferencesFromResource(R.xml.preferences);
            } else {
                addPreferencesFromResource(R.xml.preferences_pre29);
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        }

        //onResume is where to register the theme change listener
        //Note that when the theme is changed, the app automatically relaunches any live activities
        @Override
        public void onResume () {
            Log.d(TAG, "onResume has fired");
            mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            mSharedPrefs.registerOnSharedPreferenceChangeListener(mListener);
            super.onResume();
        }

    }
}
