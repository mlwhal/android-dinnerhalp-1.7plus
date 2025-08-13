package com.gmail.mlwhal.dinnerhalp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
//import androidx.appcompat.widget.ShareActionProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.FileNotFoundException;

public class ViewDinnerActivity extends AppCompatActivity {

    private DinnersDbAdapter mDbHelper;

    //Data members for various text and image views
    private TextView mTitleText;
    private TextView mMethodText;
    private TextView mTimeText;
    private TextView mServingsText;
    private ImageView mDinnerImage;
    private int mImageScalePref;
    private boolean mImageStorePref;
    private TextView mRecipeText;
    private Long mRowId;

    //TextViews for help button and hint text
    private TextView helpButton;
    private TextView hintText;
    private TextView okButton;

    //TAG String used for logging
    private static final String TAG = ViewDinnerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_dinner);

        mDbHelper = new DinnersDbAdapter(this);

        mTitleText = findViewById(R.id.section_label);
        mMethodText = findViewById(R.id.textview_method);
        mTimeText = findViewById(R.id.textview_time);
        mServingsText = findViewById(R.id.textview_servings);
        mDinnerImage = findViewById(R.id.image_dinner_thumb);
        mRecipeText = findViewById(R.id.textview_recipe);

        //Initialize hint TextView and buttons that show and dismiss it
        helpButton = findViewById(R.id.button_help);
        hintText = findViewById(R.id.hint_text);
        okButton = findViewById(R.id.button_ok);

        //Hint text and button are never shown onCreate
        hintText.setVisibility(View.GONE);
        okButton.setVisibility(View.GONE);

        mRowId =
                (savedInstanceState == null) ?
                        null :
                        (Long) savedInstanceState.getSerializable(DinnersDbContract.DinnerEntry.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(DinnersDbContract.DinnerEntry.KEY_ROWID)
                    : null;
        }

        //Check SharedPreferences to determine whether to allow screen to sleep, whether promode
        //is active, and what size images to display
        checkSharedPrefs();

        populateDinnerText();
//        Log.d(TAG, "RowID onCreate is " + mRowId);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_dinner, menu);

//        // Get the action provider associated with the menu item whose id is share
//        MenuItem shareItem = menu.findItem(R.id.action_share);
//        ShareActionProvider mShareActionProvider =
//                (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
//        mShareActionProvider.setShareIntent(getShareIntent());

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Switch for different action bar item clicks
        switch (id) {

            case R.id.action_edit:
                Intent intent1 = new Intent(this, AddDinnerActivity.class);
                intent1.putExtra(DinnersDbContract.DinnerEntry.KEY_ROWID, mRowId);
                startActivity(intent1);
                finish();
                return true;

            case R.id.action_delete:
                showDeleteDialog();
                return true;

            case R.id.action_share:
               shareDinner();
//                Log.d(TAG, "Share button pushed");
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

    private void populateDinnerText() {

        if (mRowId != null) {
            mDbHelper.open();
            Cursor dinner = mDbHelper.fetchDinner(mRowId);
            mDbHelper.close();
            startManagingCursor(dinner);

            mTitleText.setText(dinner.getString(
                    dinner.getColumnIndexOrThrow(DinnersDbContract.DinnerEntry.KEY_NAME)));

            mMethodText.setText(dinner.getString(dinner.getColumnIndexOrThrow(
                    DinnersDbContract.DinnerEntry.KEY_METHOD)));

            mTimeText.setText(dinner.getString(dinner.getColumnIndexOrThrow(
                    DinnersDbContract.DinnerEntry.KEY_TIME)));

            mServingsText.setText(dinner.getString(dinner.getColumnIndexOrThrow(
                    DinnersDbContract.DinnerEntry.KEY_SERVINGS)));

            String picPath = dinner.getString(dinner.getColumnIndexOrThrow(
                    DinnersDbContract.DinnerEntry.KEY_PICPATH));
            byte[] imageByteArray = dinner.getBlob(dinner.getColumnIndexOrThrow(
                    DinnersDbContract.DinnerEntry.KEY_PICDATA));

            //Handle image if there's one stored in the database, either as byte array or URI value
            //Priority goes to images stored in database; check there first
            if (imageByteArray != null) {
                //Scale image for display according to current scale pref
                long imageSizePref = ImageHandler.getImageWidthPref(getApplicationContext(),
                        mImageScalePref);
                Bitmap scaledDinnerImage = ImageHandler.resizeByteArray(getApplicationContext(),
                        imageByteArray, imageSizePref);
                //No need to rotate; EXIF metadata is not stored in the db
                mDinnerImage.setImageBitmap(scaledDinnerImage);

            } else if (picPath != null && !mImageStorePref) {
                //If there isn't an image stored in the db but there is a picpath
                //Turn picPath into Uri and put downsampled bitmap in ImageView
                Uri picUri = Uri.parse(picPath);
                //Get preferred size for image
                long imageSizePref = ImageHandler.getImageWidthPref(getApplicationContext(),
                        mImageScalePref);

                //Handle errors when retrieving images with picPath
                try {
                    Bitmap dinnerBitmap = ImageHandler.resizeImage(getApplicationContext(),
                            picUri, imageSizePref);
                    dinnerBitmap = ImageHandler.rotateImage(getApplicationContext(),
                            picUri, dinnerBitmap);
                    mDinnerImage.setImageBitmap(dinnerBitmap);

                } catch (FileNotFoundException | SecurityException e) {
                    Log.d(TAG, Log.getStackTraceString(e));
                    //Notify the user if image path is bad
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.toast_image_exception),
                            Toast.LENGTH_LONG).show();
                    //Show the missing image icon
                    mDinnerImage.setImageResource(R.drawable.ic_missing_image);
                }
            } else if (picPath == null || picPath.equalsIgnoreCase("")) {
                //Show no image if picPath is null or empty
                mDinnerImage.setVisibility(View.GONE);
            }

            mRecipeText.setText(dinner.getString(
                    dinner.getColumnIndexOrThrow(DinnersDbContract.DinnerEntry.KEY_RECIPE)));
            stopManagingCursor(dinner);
            dinner.close();
        }
    }

    //Method to check SharedPreferences to handle the screen timeout,
    // pro mode, and image size preferences
    private void checkSharedPrefs() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //Handle preference for screen timeout
        boolean timeoutPref = sharedPref.getBoolean(getResources()
                .getString(R.string.pref_switch_timeout_key), true);
        if (!timeoutPref) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//            Log.d(TAG, "Screen timeout disabled");
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//            Log.d(TAG, "Screen timeout enabled");
        }

        //Handle preference for pro mode being on or off
        boolean proModePref = sharedPref.getBoolean(getResources()
                .getString(R.string.pref_switch_promode_key), true);
        if (proModePref) {
            helpButton.setVisibility(View.GONE);
        } else {
            helpButton.setVisibility(View.VISIBLE);
            helpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Show hint and OK button; hide help button
                    hintText.setVisibility(View.VISIBLE);
                    okButton.setVisibility(View.VISIBLE);
                    helpButton.setVisibility(View.GONE);
                }
            });

            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Hide hint and OK button; show help button
                    hintText.setVisibility(View.GONE);
                    okButton.setVisibility(View.GONE);
                    helpButton.setVisibility(View.VISIBLE);
                }
            });
        }

        //Check preference for displaying the dinner image
        String imageScalePrefString = sharedPref.getString(getResources()
                .getString(R.string.pref_image_size_key), "192");
        mImageScalePref = Integer.parseInt(imageScalePrefString);

        //Check preference to see where images are stored
        //False: stored in picpath; true: stored in picdata
        mImageStorePref = sharedPref.getBoolean(getResources()
                .getString(R.string.pref_switch_image_storage_key), true);

//        Log.d(TAG, "mImageStorePref is " + mImageStorePref);
    }

    //Method to build an intent to share dinner names/recipes
    //No longer needed because share action provider isn't used anymore
//    private Intent getShareIntent() {
//        String dinnerTitle = mTitleText.getText().toString();
//        String dinnerRecipe = mRecipeText.getText().toString();
//
//        //Get title for shareIntent; ShareActionProvider doesn't use this
////        CharSequence shareTitle = getResources().getString(R.string.intent_share_recipe);
//
//        //Set up shareIntent and put dinner title and recipe in the intent as extras
//        Intent shareIntent = new Intent();
//        shareIntent.setAction(Intent.ACTION_SEND);
//        shareIntent.setType("text/plain");
//        shareIntent.putExtra(Intent.EXTRA_SUBJECT, dinnerTitle);
//        shareIntent.putExtra(Intent.EXTRA_TEXT, dinnerRecipe);
//
//        return shareIntent;
//    }

    //Method to share dinner name/recipe
    private void shareDinner() {
        String dinnerTitle = mTitleText.getText().toString();
        String dinnerRecipe = mRecipeText.getText().toString();

        //Set up shareIntent and put dinner title and recipe in the intent as extras
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, dinnerTitle);
        shareIntent.putExtra(Intent.EXTRA_TEXT, dinnerRecipe);

        //Launch a chooser activity
        Intent sendIntent = Intent.createChooser(shareIntent, null);
        Log.d(TAG, "shareDinner: Chooser being created");
//        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(sendIntent);
//        } else {
//            Toast.makeText(getApplicationContext(), "Please download an app before trying to share",
//                    Toast.LENGTH_SHORT).show();
//        }

    }

    public static class DeleteDialogFragment extends AppCompatDialogFragment {

        static DeleteDialogFragment newInstance(int title) {
            DeleteDialogFragment frag = new DeleteDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog (Bundle savedInstanceState) {
            assert getArguments() != null;
            int title = getArguments().getInt("title");

            return new MaterialAlertDialogBuilder(getActivity(), R.style.Theme_DinnerHalp_CustomAlertDialog)
                    .setTitle(title)
                    .setPositiveButton(R.string.alert_dialog_delete_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
//                                    Log.d(TAG, "Delete button clicked!");
                                    ((ViewDinnerActivity)requireActivity()).doPositiveClick();
                                }
                            }
                    )
                    .setNegativeButton(R.string.button_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((ViewDinnerActivity)requireActivity()).doNegativeClick();
                                }
                            }
                    )
                    .create();
        }
    }

    void showDeleteDialog() {
        AppCompatDialogFragment newFragment = DeleteDialogFragment.newInstance(
                R.string.delete_alert_title);
        newFragment.show(getSupportFragmentManager(), "dialog");

    }

    public void doPositiveClick() {

        //deleteDinner() will return true if successful
        mDbHelper.open();
        boolean deleteSuccess = mDbHelper.deleteDinner(mRowId);
        mDbHelper.close();
        if (deleteSuccess) {
            Context context = getApplicationContext();
            CharSequence text = mTitleText.getText() + " deleted";
            int duration = Toast.LENGTH_LONG;
            Toast.makeText(context, text, duration).show();

            Intent intent = new Intent(this, DinnerListActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void doNegativeClick() {
        //Dialog is dismissed automatically
    }

    //Lifecycle onResume() method needed in case SharedPreferences have changed

    @Override
    public void onResume() {
        super.onResume();
//        Log.d(TAG, "onResume!");
        checkSharedPrefs();
        //Re-run populateDinnerText() in case the image size has changed
        populateDinnerText();
    }
}
