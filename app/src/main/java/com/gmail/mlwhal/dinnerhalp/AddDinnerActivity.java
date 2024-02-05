package com.gmail.mlwhal.dinnerhalp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.app.NavUtils;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
//import java.util.Objects;

public class AddDinnerActivity extends AppCompatActivity {

    //TextViews for help button and hint text
    private TextView helpButton;
    private TextView hintText;
    private TextView okButton;

    private DinnersDbAdapter mDbHelper;
    private EditText mEditNameText;
    private Spinner mMethodSpinner;
    private Spinner mTimeSpinner;
    private Spinner mServingsSpinner;
    private ImageButton mSetPicButton;
    private ImageButton mChangePicButton;
    private ImageButton mRemovePicButton;
    private Uri mSelectedImageUri;
    private Bitmap mDinnerBitmap;
    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText mEditRecipe;
    //Tracker variable in case activity is getting content from another app
    private boolean mSharedContent = false;
    private String mNameText;
    private String mRecipeText;
    private Long mRowId;
    //Variables for preferences
    private int mImageScalePref;
    private boolean mImageStorePref;

    //TAG String used for logging
    private static final String TAG = AddDinnerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dinner);

        //Handle cases where this activity is getting content from another app
        Intent shareIntent = getIntent();
        String action = shareIntent.getAction();
        String type = shareIntent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                //go ahead and prepare to create a new dinner using incoming text as recipe
//                Log.d(TAG, "Text sent to app!");
//                Log.d(TAG, "mRowId is " + mRowId);
                mSharedContent = true;
//                Log.d(TAG, "Text sent is " + mRecipeText);
                //Show dialog to let user decide whether incoming text has a title to use as the
                //dinner name
                showShareDialog();
            }
        }

        mDbHelper = new DinnersDbAdapter(this);

        //Initialize hint TextView and buttons that show and dismiss it
        helpButton = findViewById(R.id.button_help);
        hintText = findViewById(R.id.hint_text);
        okButton = findViewById(R.id.button_ok);

        //Hint text and button are never shown onCreate
        hintText.setVisibility(View.GONE);
        okButton.setVisibility(View.GONE);

        //Check SharedPreferences to determine what size images to display and whether pro mode
        //is active
        checkSharedPrefs();

        //EditText for dinner name
        mEditNameText = findViewById(R.id.edittext_name);
        mEditNameText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        //Cooking method spinner
        mMethodSpinner = findViewById(R.id.spinner_method);
        ArrayAdapter<CharSequence> methodAdapter = ArrayAdapter.createFromResource(
                this, R.array.method_array, android.R.layout.simple_spinner_item);
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMethodSpinner.setAdapter(methodAdapter);

        //Cooking time spinner
        mTimeSpinner = findViewById(R.id.spinner_time);
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(
                this, R.array.time_array, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTimeSpinner.setAdapter(timeAdapter);

        //Servings spinner
        mServingsSpinner = findViewById(R.id.spinner_servings);
        ArrayAdapter<CharSequence> servingsAdapter = ArrayAdapter.createFromResource(
                this, R.array.servings_array, android.R.layout.simple_spinner_item);
        servingsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mServingsSpinner.setAdapter(servingsAdapter);

        //Initialize image button
        mSetPicButton = findViewById(R.id.button_add_image);
        mSetPicButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent();
                photoPickerIntent.setType("image/*");
                photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
                photoPickerIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(photoPickerIntent, "Select picture"),
                        PICK_IMAGE_REQUEST);
            }
        });

        //Initialize change image button but hide unless needed
        mChangePicButton = findViewById(R.id.button_change_image);
        mChangePicButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent();
                photoPickerIntent.setType("image/*");
                photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
                photoPickerIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(photoPickerIntent, "Change picture"),
                        PICK_IMAGE_REQUEST);
            }
        });
        mChangePicButton.setVisibility(View.GONE);

        //Initialize remove image button but hide unless needed
        mRemovePicButton = findViewById(R.id.button_remove_image);
        mRemovePicButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Create dialog to confirm the removal of the image
                showRemoveImgDialog();
            }
        });
        mRemovePicButton.setVisibility(View.GONE);

        //EditText for recipe
        mEditRecipe = findViewById(R.id.edittext_recipe);
        mEditRecipe.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        //Check savedInstanceState for a row ID to use, or use null if there is none
        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(DinnersDbContract.DinnerEntry.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(DinnersDbContract.DinnerEntry.KEY_ROWID)
                    : null;
        }

        //If coming from another app sharing content, override any mRowId value to create new dinner
        if (mSharedContent) {
            mRowId = null;
        }

        //Fill in text fields and set spinner values if we are editing a record.
        populateFields();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_dinner, menu);
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

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_cancel:
                finish();
                return true;

            case R.id.action_save:
                //Check whether name field is empty before allowing save
                String nameText = mEditNameText.getText().toString();
                if (nameText.matches("")) {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.toast_no_dinner_name),
                            Toast.LENGTH_LONG).show();
                } else {
                    setResult(RESULT_OK);
                    saveDinner();
                }
                return true;

            case R.id.action_search:
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("FRAGMENT_TRACKER", 0);
                this.startActivity(intent);
                return true;

            case R.id.action_manage:
                Intent intent2 = new Intent(this, MainActivity.class);
                intent2.putExtra("FRAGMENT_TRACKER", 1);
                this.startActivity(intent2);
                return true;

            case R.id.action_settings:
                Intent intent3 = new Intent(this, SettingsActivity.class);
                this.startActivity(intent3);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    //Lifecycle handling methods
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(DinnersDbContract.DinnerEntry.KEY_ROWID, mRowId);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        //This code handles when the user chooses an image to save as part of a dinner
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                mSelectedImageUri = imageReturnedIntent.getData();
//                    Log.d(TAG, "Uri is " + mSelectedImageUri);

                //Take a persistent permission for the image file so the app doesn't lose it later
                int takeFlags = imageReturnedIntent.getFlags();
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                //Check for the freshest data
                getContentResolver().takePersistableUriPermission(mSelectedImageUri, takeFlags);

                //Scale the image to the preferred size for the device
                //First, get the max preferred size for image from arrays.xml
                //This will be saved in the database in case the user ever wants the largest size
                String[] imageScalePrefArray = getResources().getStringArray(R.array.pref_image_size_values);
                int imageScaleMax = Integer.parseInt(imageScalePrefArray[imageScalePrefArray.length - 1]);
//                    Log.d(TAG, "imageScaleMax is " + imageScalePrefArray[imageScalePrefArray.length - 1]);
                long maxSizePref = ImageHandler.getImageWidthPref(getApplicationContext(),
                        imageScaleMax);
                //Then get the user's preferred size for image
                long imageSizePref = ImageHandler.getImageWidthPref(getApplicationContext(),
                        mImageScalePref);
//                    Log.d(TAG, "Max size is " + maxSizePref + ", pref size is " + imageSizePref);
                try {
                    //Store a bitmap at the largest allowed scale to save in the db
                    mDinnerBitmap = ImageHandler.resizeImage(getApplicationContext(),
                            mSelectedImageUri, maxSizePref);
                    mDinnerBitmap = ImageHandler.rotateImage(getApplicationContext(),
                            mSelectedImageUri, mDinnerBitmap);
//                        Log.d(TAG, "mDinnerBitmap set to large size");
                    //Create bitmap matching current size preference for display
                    Bitmap dinnerBitmap = ImageHandler.resizeImage(getApplicationContext(),
                            mSelectedImageUri, imageSizePref);
                    dinnerBitmap = ImageHandler.rotateImage(getApplicationContext(),
                            mSelectedImageUri, dinnerBitmap);
//                        Log.d(TAG, "Bitmap for viewing has been processed");
                    //Show the image in the ImageView so the user knows the image selection worked
                    mSetPicButton.setImageBitmap(dinnerBitmap);
                } catch (FileNotFoundException e) {
                    Log.d(TAG, Log.getStackTraceString(e));
                    Toast.makeText(getApplicationContext(), R.string.toast_exception,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //Todo: Is this method being called twice when loading an existing dinner?
    //The log is showing "Uri from db is " twice.
    //Method to fill in data if existing record is pulled from database.
    //Todo: Replace startManagingCursor with CursorLoader,
    //that will eliminate the deprecated stuff.
    private void populateFields() {

        if (mRowId != null) {
            mDbHelper.open();
            Cursor dinner = mDbHelper.fetchDinner(mRowId);
            mDbHelper.close();
            startManagingCursor(dinner);

            //Change section label if dinner is being updated rather than created
            TextView sectionLabel = findViewById(R.id.section_label);
            sectionLabel.setText(getResources().getString(R.string.update_dinner_title));
            mEditNameText.setText(dinner.getString(
                    dinner.getColumnIndexOrThrow(DinnersDbContract.DinnerEntry.KEY_NAME)));
            //Set spinners to correct index for existing dinner
            mMethodSpinner.setSelection(getSpinnerIndex(mMethodSpinner,
                    dinner.getString(dinner.getColumnIndexOrThrow(
                            DinnersDbContract.DinnerEntry.KEY_METHOD))));
            mTimeSpinner.setSelection(getSpinnerIndex(mTimeSpinner,
                    dinner.getString(dinner.getColumnIndexOrThrow(
                            DinnersDbContract.DinnerEntry.KEY_TIME))));
            mServingsSpinner.setSelection(getSpinnerIndex(mServingsSpinner,
                    dinner.getString(dinner.getColumnIndexOrThrow(
                            DinnersDbContract.DinnerEntry.KEY_SERVINGS))));

            //Get image data from the picpath and picdata columns
            String imageString = dinner.getString(dinner.getColumnIndexOrThrow(
                    DinnersDbContract.DinnerEntry.KEY_PICPATH));
            if (imageString != null) {
                mSelectedImageUri = Uri.parse(imageString);
            }
            byte[] imageByteArray = dinner.getBlob(dinner.getColumnIndexOrThrow(
                    DinnersDbContract.DinnerEntry.KEY_PICDATA));

            //Priority goes to images stored in database; check there first
            //If image is stored in db, make a bitmap from imageByteArray
            if (imageByteArray != null) {
//                Log.d(TAG, "imageBitmap is not null");
                //Set mDinnerBitmap to retrieved bitmap to ensure that the right image is used
                //for saveDinner() and other methods that might use it later
                mDinnerBitmap = BitmapFactory.decodeByteArray(imageByteArray,
                        0, imageByteArray.length);
//                int bitmapHeight = mDinnerBitmap.getHeight();
//                Log.d(TAG, "mDinnerBitmap has been updated; height is " + bitmapHeight);

                //Todo: Does resizing and rotating need to be inside a try/catch block?
                //This code only runs if data was pulled from the database
                //Scale image for display according to current scale pref
                long imageSizePref = ImageHandler.getImageWidthPref(getApplicationContext(),
                        mImageScalePref);
                Bitmap scaledDinnerImage = ImageHandler.resizeByteArray(getApplicationContext(),
                            imageByteArray, imageSizePref);
                //EXIF data is lost when image is saved in db; no need to rotate the image
                mSetPicButton.setImageBitmap(scaledDinnerImage);

                //Display change and remove image buttons
                mChangePicButton.setVisibility(View.VISIBLE);
                mRemovePicButton.setVisibility(View.VISIBLE);
            } else if (imageString != null && !mImageStorePref) {
                //If there's a picpath in the database AND prefs are set to use it, load image from there

                //Display change and remove image buttons
                mChangePicButton.setVisibility(View.VISIBLE);
                mRemovePicButton.setVisibility(View.VISIBLE);

                //Downsample bitmap, rotate, and display
                Uri imageUri = Uri.parse(imageString);
//                Log.d(TAG, "Uri from db is " + imageUri);
                try {
                    long imageSizePref = ImageHandler.getImageWidthPref(getApplicationContext(),
                            mImageScalePref);
                    Bitmap dinnerBitmap = ImageHandler.resizeImage(getApplicationContext(),
                            imageUri, imageSizePref);
                    dinnerBitmap = ImageHandler.rotateImage(getApplicationContext(), imageUri,
                            dinnerBitmap);
                    mSetPicButton.setImageBitmap(dinnerBitmap);
                } catch (FileNotFoundException | SecurityException e) {
                    Log.d(TAG, Log.getStackTraceString(e));
                    //Show the missing image icon if the picPath is bad
                    mSetPicButton.setImageResource(R.drawable.ic_missing_image);
                }
            } else {
                    //Handle cases where there are no images stored
                    mSetPicButton.setImageResource(R.drawable.ic_new_picture);
                    mChangePicButton.setVisibility(View.GONE);
                    mRemovePicButton.setVisibility(View.GONE);
//                    Log.d(TAG, "imageBitmap and imageString are null");
            }

            mEditRecipe.setText(dinner.getString(
                    dinner.getColumnIndexOrThrow(DinnersDbContract.DinnerEntry.KEY_RECIPE)));

            //Close up cursor
            stopManagingCursor(dinner);
            dinner.close();

        } else {
            //If mRowId is null, hide change/remove image buttons since they're not relevant
            mChangePicButton.setVisibility(View.GONE);
            mRemovePicButton.setVisibility(View.GONE);

            mEditNameText.requestFocus();

            //If another app sent in text via an intent, put that in the name and recipe EditTexts
            mEditNameText.setText(mNameText);
            mEditRecipe.setText(mRecipeText);
        }

     }

    //Method to get index of spinner when value is known (thanks StackOverflow)
    //http://stackoverflow.com/questions/29595478/set-spinner-value-based-on-database-record-in-android
    private int getSpinnerIndex(Spinner spinner, String myString) {

        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                index = i;
                break;
            }
        }
        return index;

    }

    private void saveDinner() {

        //Collect values from input fields
        String name = mEditNameText.getText().toString();
        String method = mMethodSpinner.getSelectedItem().toString();
        String time = mTimeSpinner.getSelectedItem().toString();
        String servings = mServingsSpinner.getSelectedItem().toString();
        String picpath;
        byte[] imageByteArray;

        //Set picpath if there is a known image path and the user preference is to save just paths
        if (mSelectedImageUri != null && !mImageStorePref) {
            picpath = mSelectedImageUri.toString();
//            Log.d(TAG, "saveDinner: Picpath will be " + picpath);
        } else {
            picpath = null;
//            Log.d(TAG, "saveDinner: Picpath is null");
        }

        //Process mDinnerBitmap into byte array to save in database
        //Only do this if mImageStorePref is true (user wants to save images in the db)
        if (mDinnerBitmap != null && mImageStorePref) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            //Compress bitmap to JPEG with maximum quality (100)
            mDinnerBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            imageByteArray = bos.toByteArray();
//            Log.d(TAG, "imageByteArray saved; Length is " + imageByteArray.length);
        } else {
            imageByteArray = null;
//            Log.d(TAG, "saveDinner: imageByteArray is null");
        }

        String recipe = mEditRecipe.getText().toString();

//        Log.d(TAG, "mRowId = " + mRowId);

        //Create dinner or update existing record depending on the value of mRowId
        if (mRowId == null) {
            //Todo: Does it ever crash on a new dinner when name is not unique?
            mDbHelper.open();
            long id = mDbHelper.createDinner(name, method, time, servings, picpath,
                    imageByteArray, recipe);
            mDbHelper.close();
//            Log.d(TAG, "id = " + id);
            //If id == -1 the dinner hasn't been saved; toast this and remain
            //in AddDinnerActivity.
            if (id == -1) {
                notUniqueName();
            } else if (id > 0) {
//                Log.d(TAG, "Dinner created");
                mRowId = id;
                saveSuccessToast(name);

                //After dinner is saved, launch DinnerListActivity to display updated dinner list
                Intent intent1 = new Intent(this, DinnerListActivity.class);
                this.startActivity(intent1);
                finish();
            }
        } else {
            //updateDinner() returns a boolean indicating whether rows were updated.
            boolean updateSuccess = false;
            try {
                mDbHelper.open();
                updateSuccess = mDbHelper.updateDinner(
                        mRowId, name, method, time, servings, picpath, imageByteArray, recipe);
                mDbHelper.close();
            } catch (SQLiteConstraintException e) {
                Log.d(TAG, "Exception caught: " + e);
                notUniqueName();
            }
            if (updateSuccess) {
                saveSuccessToast(name);

                //Launch ViewDinnerActivity to see updated dinner
                Intent intent2 = new Intent(this, ViewDinnerActivity.class);
                intent2.putExtra(DinnersDbContract.DinnerEntry.KEY_ROWID, mRowId);
                this.startActivity(intent2);
                finish();
            }

        }

    }

    //Method to handle when user wants first line of shared text to become dinner name
    private void handleShareString(Intent shareIntent) {
        //Todo: shareString might be redundant with mRecipeText; or does scope matter?
        String shareString = shareIntent.getExtras().getString(Intent.EXTRA_TEXT);
        //Starting value for mRecipeText is the entire shareString
        mRecipeText = shareString;

        if (shareString != null) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 50; i++) {
                char c = shareString.charAt(i);
                //Build up a string of chars until a line break is hit
                if (c != '\n') {
                    builder.append(c);
                } else {
                    break;
                }
            }
            mNameText = builder.toString();
//            Log.d(TAG, "Name text is " + mNameText);

            //Remove mNameText and line break from front of shareString to update mRecipeText.
            //This is inside the if statement so that replaceFirst() isn't attempted if
            //shareString is null.
            mRecipeText = mRecipeText.replaceFirst(mNameText + "\n", "");
//            Log.d(TAG, "Truncated recipe is " + mRecipeText);
        }

    }

    public void saveSuccessToast(CharSequence name) {

        Context context = getApplicationContext();
        CharSequence text = name + getResources().getString(R.string.toast_dinner_saved);
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, text, duration).show();

    }

    public void notUniqueName() {

        //Toast that name must be unique
        Context context = getApplicationContext();
        CharSequence text = getResources().getString(R.string.toast_not_unique);
        int duration = Toast.LENGTH_LONG;
        Toast.makeText(context, text, duration).show();

        //Put user back into editText to rename dinner
        mEditNameText.setSelectAllOnFocus(true);
        mEditNameText.requestFocus();

    }

    //Method to check SharedPreferences to handle image size preference and pro mode
    private void checkSharedPrefs() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

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
                .getString(R.string.pref_image_size_key), "40");
        mImageScalePref = Integer.parseInt(imageScalePrefString);

        //Check preference to see where images are stored
        //False: stored in picpath; true: stored in picdata
        mImageStorePref = sharedPref.getBoolean(getResources()
                .getString(R.string.pref_switch_image_storage_key), true);
    }

    //Class and methods for an alert dialog to let user decide whether shared text has a title
    public static class ShareDialogFragment extends AppCompatDialogFragment {
        static ShareDialogFragment newInstance(int title) {
            ShareDialogFragment frag = new ShareDialogFragment();
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

            return new MaterialAlertDialogBuilder(getActivity(), R.style.Theme_DinnerHalp_NightAlertDialog)
                    .setTitle(title)
                    .setPositiveButton(R.string.alert_dialog_share_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((AddDinnerActivity) requireActivity()).doPositiveShareClick();
                                }
                            }
                    )
                    .setNegativeButton(R.string.alert_dialog_share_no,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((AddDinnerActivity) requireActivity()).doNegativeShareClick();
                                }
                            }
                    )
                    .create();
        }
    }

    void showShareDialog() {
        AppCompatDialogFragment newFragment = ShareDialogFragment.newInstance(R.string.share_alert_title);
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    public void doPositiveShareClick() {
        Intent shareIntent = getIntent();
        //User wants the first line of the shared text to go into the dinner name field
        handleShareString(shareIntent);
        //Refresh the fields to show updated name and recipe
        populateFields();
    }

    public void doNegativeShareClick() {
        //Grab shared text and put all of it into recipe field
        Intent shareIntent = getIntent();
        mRecipeText = shareIntent.getExtras().getString(Intent.EXTRA_TEXT);
        //Refresh the fields to show updated recipe
        populateFields();
    }

    //Class and methods for an alert dialog to let user remove an image from a dinner
    public static class RemoveImgDialogFragment extends AppCompatDialogFragment {
        static RemoveImgDialogFragment newInstance(int title, int message) {
            RemoveImgDialogFragment frag = new RemoveImgDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            args.putInt("message", message);
            frag.setArguments(args);
            return frag;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            assert getArguments() != null;
            int title = getArguments().getInt("title");
            int message = getArguments().getInt("message");

            return new MaterialAlertDialogBuilder(getActivity(), R.style.Theme_DinnerHalp_NightAlertDialog)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(R.string.alert_dialog_remove_img_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((AddDinnerActivity) requireActivity()).doPositiveImgClick();
                                }
                            }
                    )
                    .setNegativeButton(R.string.button_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((AddDinnerActivity) requireActivity()).doNegativeImgClick();
                                }
                            }
                    )
                    .create();
        }
    }

    void showRemoveImgDialog() {
        AppCompatDialogFragment newFragment = RemoveImgDialogFragment.newInstance(R.string.image_remove_alert_title,
                R.string.image_remove_alert_message);
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    //Remove path to image, clear mDinnerBitmap, and reset image buttons
    void doPositiveImgClick() {
        mSelectedImageUri = null;
//        Log.d(TAG, "mSelectedImageUri now null");
        mDinnerBitmap = null;
//        Log.d(TAG, "mDinnerBitmap now null");
        Toast.makeText(getApplicationContext(), R.string.toast_image_removed, Toast.LENGTH_SHORT).show();
        mSetPicButton.setImageResource(R.drawable.ic_new_picture);
        mSetPicButton.setVisibility(View.VISIBLE);
        mChangePicButton.setVisibility(View.GONE);
        mRemovePicButton.setVisibility(View.GONE);
    }

    void doNegativeImgClick() {
        //Dialog dismisses and nothing is changed
    }

    //Lifecycle onResume() method needed in case SharedPreferences have changed
    @Override
    public void onResume() {
        super.onResume();
        checkSharedPrefs();
    }
}
