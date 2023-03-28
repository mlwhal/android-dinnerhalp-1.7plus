package com.gmail.mlwhal.dinnerhalp;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class AboutAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_about_app, menu);
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

            case R.id.action_settings:
                Intent intent4 = new Intent(this, SettingsActivity.class);
                this.startActivity(intent4);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }
}
