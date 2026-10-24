package com.gmail.mlwhal.dinnerhalp.ui.main;

import android.util.Log;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ActivityLayouts {
    private ActivityLayouts() {
    }

    public static void applyEdgeToEdge(ComponentActivity activity, int viewId) {
        //Set window to full screen if you want to have edge to edge work for earlier SDKs
//        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);

        View view = activity.findViewById(viewId);
        //Set listener
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Log.d("ActivityLayouts", "Current activity is " + activity);
            Log.d("ActivityLayouts", "Top inset is + " + insets.top);
            Log.d("ActivityLayouts", "Bottom inset is + " + insets.bottom);
            Log.d("ActivityLayouts", "Left inset is + " + insets.left);
            Log.d("ActivityLayouts", "Right inset is + " + insets.right);
            // Apply the insets paddings to the view.
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);

            // Return WindowInsetsCompat.CONSUMED if you don't want the window insets to keep being
            // passed down to descendant views. Otherwise return windowInsets
//            return WindowInsetsCompat.CONSUMED;
            return windowInsets;
        });

    }
}
