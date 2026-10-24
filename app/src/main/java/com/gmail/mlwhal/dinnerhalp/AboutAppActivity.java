package com.gmail.mlwhal.dinnerhalp;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.gmail.mlwhal.dinnerhalp.ui.main.ActivityLayouts;

public class AboutAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        //Handle edge to edge insets
        ActivityLayouts.applyEdgeToEdge(this, R.id.content);

//        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content),
//                (v, windowInsets) -> {
//                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
//                    Log.d("AboutApp", "Top inset is + " + insets.top);
//                    Log.d("AboutApp", "Bottom inset is + " + insets.bottom);
//                    Log.d("AboutApp", "Left inset is + " + insets.left);
//                    Log.d("AboutApp", "Right inset is + " + insets.right);
//                    // Apply the insets paddings to the view.
//                    v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
//
//                    // Return CONSUMED if you don't want the window insets to keep being
//                    // passed down to descendant views.
//                    return WindowInsetsCompat.CONSUMED;
//                    });
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
