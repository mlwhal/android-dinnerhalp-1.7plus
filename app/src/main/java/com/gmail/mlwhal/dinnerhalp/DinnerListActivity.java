package com.gmail.mlwhal.dinnerhalp;

import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.DialogFragment;
import androidx.appcompat.app.AppCompatDialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;


public class DinnerListActivity extends AppCompatActivity {

    private DinnersDbAdapter mDbHelper;
    //Array of long values to track ids of dinners that are selected for deletion;
    //this needs to be out here because it's used by both the ActionMode and the
    //DialogFragment.
    ArrayList<Long> mIdList = new ArrayList<>();

    //Data members for handling info for savedInstanceState and Intent extras
    static final String KEYWORD_SEARCH = "KEYWORD_SEARCH";
    static final String SEARCH_COLUMN = "SEARCH_COLUMN";
    static final String SEARCH_STRING = "SEARCH_STRING";
    private Boolean mKeywordSearch;
    private String mSearchColumn;
    private String mSearchString;

    //TextViews for help button and hint text
    private TextView helpButton;
    private TextView hintText;
    private TextView okButton;

    //TAG String used for logging
    private static final String TAG = DinnerListActivity.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dinner_list);

        //Initialize hint TextView and buttons that show and dismiss it
        helpButton = findViewById(R.id.button_help);
        hintText = findViewById(R.id.hint_text);
        okButton = findViewById(R.id.button_ok);

        //Hint text and button are never shown onCreate
        hintText.setVisibility(View.GONE);
        okButton.setVisibility(View.GONE);

        //Check SharedPreferences for pro mode in order to show helpButton only if false
        checkSharedPrefs();

        mDbHelper = new DinnersDbAdapter(this);

        /* Get extras:
         * Was there a keyword search? (boolean)
         * Is there a where clause from the SearchFragment?
         * Is there a where argument from the SearchFragment?
         */
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                mKeywordSearch = false;
                mSearchColumn = null;
                mSearchString = null;
            } else {
                mKeywordSearch = extras.getBoolean(KEYWORD_SEARCH);
                mSearchColumn = extras.getString(SEARCH_COLUMN);
                mSearchString = extras.getString(SEARCH_STRING);
            }
        } else {
            mKeywordSearch = (Boolean) savedInstanceState.getSerializable(KEYWORD_SEARCH);
            mSearchColumn = (String) savedInstanceState.getSerializable(SEARCH_COLUMN);
            mSearchString = (String) savedInstanceState.getSerializable(SEARCH_STRING);
        }
//        Log.d(TAG, "Keyword search is " + mKeywordSearch);
//        Log.d(TAG, "Where clause is " + mSearchColumn);
//        Log.d(TAG, "String to search is " + mSearchString);
        fillData(mKeywordSearch, mSearchColumn, mSearchString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dinner_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Todo: Fix home button so that it goes back to correct tab in MainActivity
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

    //Method to populate ListView with data from database
    private void fillData(Boolean keywordSearch, String searchColumn, String searchString) {
        mDbHelper.open();
        Cursor dinnerCursor;
        //Boolean to track whether the DinnerListActivity came from a search or from "show all"
        final Boolean queryDinnerList;

        //An if statement to distinguish between retrieving all records
        //and retrieving just the results of a search (column name, search term)

        if (searchColumn != null) {
            queryDinnerList = true;
//            Log.d(TAG, "SearchColumn not null, = " + searchColumn);
            dinnerCursor = mDbHelper.fetchDinnerSearch(keywordSearch, searchColumn, searchString);
            startManagingCursor(dinnerCursor);
        } else {
            queryDinnerList = false;
            // Get all of the rows from the database and create the item list
            dinnerCursor = mDbHelper.fetchAllDinners();
            startManagingCursor(dinnerCursor);
        }

        // Create an array to specify the fields we want to display in the list (only NAME)
        String[] from = new String[] {DinnersDbContract.DinnerEntry.KEY_NAME};

        // and an array of the fields we want to bind those fields to (in this case dinner_row)
        //(Note I have no idea why to bind to a TextView here!)
        int[] to = new int[] {R.id.dinner1};

        // Now create a simple cursor adapter and set it to display
        final SimpleCursorAdapter dinners = new SimpleCursorAdapter(
                this, R.layout.dinner_row, dinnerCursor, from, to
        );

        final ListView lv = findViewById(R.id.listview_dinners);
        lv.setAdapter(dinners);

        //Housekeeping: Close down dinnerCursor and mDbHelper
        stopManagingCursor(dinnerCursor);
        mDbHelper.close();

        //Handle what to show when the database is empty or there are no search results
        View empty = findViewById(R.id.listview_empty);
        final ImageButton addDinnerImgButton = empty.findViewById(R.id.button_add_dinner);
        final TextView addDinnerTextView = empty.findViewById(R.id.textview_empty);
        lv.setEmptyView(empty);

        addDinnerImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(addDinnerImgButton.getContext(), AddDinnerActivity.class);
                startActivity(intent);
                finish();
            }
        });

        addDinnerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(addDinnerTextView.getContext(), AddDinnerActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Click listeners for listview items
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //launch viewDinner activity
//                Log.d(TAG, "Position clicked " + position +
//                        " and id " + id);
                Intent i = new Intent(view.getContext(), ViewDinnerActivity.class);
                i.putExtra(DinnersDbContract.DinnerEntry.KEY_ROWID, id);
                i.putExtra("QUERY_DINNERS", queryDinnerList);
                //Todo: Might not need queryDinnerList now that onSaveInstanceState is working.
                //Right now the ViewDinnerActivity back button always goes back to fetchAllDinners().
                //Or that might be the home button causing that trouble.
                startActivity(i);
            }
        });

        //Long click listener to fire contextual action bar
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            //These track how many items are selected and the ids of the items
            private int nr = 0;
            private long itemId;

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Here you can do something when items are selected/de-selected,
                // such as update the title in the CAB
                if (checked) {
                    itemId = id;
                    mIdList.add(id);
//                    Log.d(TAG, "Id selected = " + id);
//                    Log.d(TAG, "Current list = " + mIdList);
                    nr++;
                } else {
                    mIdList.remove(id);
//                    Log.d(TAG, "Current list = " + mIdList);
                    nr--;
                }
                mode.setTitle(nr + " selected");

                //Using invalidate forces CAB to be redone with updated nr value
                //allowing the edit button to be hidden or shown as required.
                mode.invalidate();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu_1, menu);

                return true;

            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request

                //Hide the edit button if more than one item is selected
//                Log.d(TAG, "nr = " + nr);
                if (nr == 1) {
                    MenuItem item = menu.findItem(R.id.cab_edit);
                    item.setVisible(true);
                    return true;
                } else {
                    MenuItem item = menu.findItem(R.id.cab_edit);
                    item.setVisible(false);
                    return true;
                }
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.cab_edit:
                        //Launch AddDinnerActivity with id passed in as extra
                        Intent i = new Intent(getApplicationContext(), AddDinnerActivity.class);
                        i.putExtra(DinnersDbContract.DinnerEntry.KEY_ROWID, itemId);
                        i.putExtra("QUERY_DINNERS", queryDinnerList);
                        nr = 0;
                        startActivity(i);
                        mode.finish();
                        finish();
                        return true;

                    case R.id.cab_delete:
                        showDeleteDialog();
                        return true;

                    case R.id.cab_share:
                        shareDinners();
                        return true;

                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
                nr = 0;
                mIdList.clear();
            }

        });

    }

    //Method to find out whether Pro User Mode is on and handle help text accordingly
    private void checkSharedPrefs() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
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

    }

    private void shareDinners() {
        //Get cursor to database to pull out names and recipes and set up data members
        Cursor dinnerCursor;
        String dinnerTitle;
        String dinnerRecipe;

        //Set up shareIntent
        Intent shareIntent = new Intent();
        shareIntent.setType("text/plain");
        shareIntent.setAction(Intent.ACTION_SEND);

        //Get title and recipe for dinner from dinnerCursor
        //Behavior will differ depending on whether 1 or more dinners is selected
        mDbHelper.open();
        int size = mIdList.size();
        if (size == 1) {
            dinnerCursor = mDbHelper.fetchDinner(mIdList.get(0));
            startManagingCursor(dinnerCursor);
            dinnerTitle = dinnerCursor.getString(dinnerCursor.getColumnIndexOrThrow(
                    DinnersDbContract.DinnerEntry.KEY_NAME));
            dinnerRecipe = dinnerCursor.getString(dinnerCursor.getColumnIndexOrThrow(
                    DinnersDbContract.DinnerEntry.KEY_RECIPE));
            stopManagingCursor(dinnerCursor);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, dinnerTitle);
            shareIntent.putExtra(Intent.EXTRA_TEXT, dinnerRecipe);
//            Log.d(TAG, "Title to share is " + dinnerTitle);
//            Log.d(TAG, "Recipe to share is " + dinnerRecipe);

        } else {
            //Get titles/recipes from all dinners in mIdList
            //and build a big string for all of it
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < size; i++) {
                dinnerCursor = mDbHelper.fetchDinner(mIdList.get(i));
                startManagingCursor(dinnerCursor);
                dinnerTitle = dinnerCursor.getString(dinnerCursor.getColumnIndexOrThrow(
                        DinnersDbContract.DinnerEntry.KEY_NAME));
                dinnerRecipe = dinnerCursor.getString(dinnerCursor.getColumnIndexOrThrow(
                        DinnersDbContract.DinnerEntry.KEY_RECIPE));
                stopManagingCursor(dinnerCursor);
                builder.append("-----\n")
                        .append(dinnerTitle)
                        .append("\n")
                        .append(dinnerRecipe)
                        .append("\n\n");

            }

            CharSequence multiDinnerSubject = getResources().getText(R.string.intent_share_subject);
            String dinnerTextString = builder.toString();

            shareIntent.putExtra(Intent.EXTRA_SUBJECT, multiDinnerSubject);
            shareIntent.putExtra(Intent.EXTRA_TEXT, dinnerTextString);
//            Log.d(TAG, "Title to share is " + multiDinnerSubject);
//            Log.d(TAG, "Text to share is " + dinnerTextString);

        }

        mDbHelper.close();

        //Create chooser and give it the shareIntent, if there's an app to handle it
        Intent chooser = Intent.createChooser(shareIntent,
                getResources().getText(R.string.intent_share_dialog_title));
        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            Toast.makeText(getApplicationContext(), "Please download an app before trying to share",
                    Toast.LENGTH_SHORT).show();
        }

    }

    //Lifecycle methods to recreate list after share dialog is used or other interruptions happen

    //Save search parameters that led to current list of dinners so back navigation remembers
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEYWORD_SEARCH, mKeywordSearch);
        savedInstanceState.putString(SEARCH_COLUMN, mSearchColumn);
        savedInstanceState.putString(SEARCH_STRING, mSearchString);

//        Log.d(TAG, "SaveInstanceState! KeywordSearch is " + mKeywordSearch);
//        Log.d(TAG, "SaveInstanceState! mSearchString is " + mSearchString);

        super.onSaveInstanceState(savedInstanceState);

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {

        mKeywordSearch = savedInstanceState.getBoolean(KEYWORD_SEARCH);
        mSearchColumn = savedInstanceState.getString(SEARCH_COLUMN);
        mSearchString = savedInstanceState.getString(SEARCH_STRING);
//        Log.d(TAG, "RestoreInstanceState! KeywordSearch is " + mKeywordSearch);
//        Log.d(TAG, "RestoreInstanceState! mSearchString is " + mSearchString);
        fillData(mKeywordSearch, mSearchColumn, mSearchString);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        Log.d(TAG, "onResume! KeywordSearch is " + mKeywordSearch);
//        Log.d(TAG, "onResume! mSearchString is " + mSearchString);
        fillData(mKeywordSearch, mSearchColumn, mSearchString);

        //Recheck SharedPreferences for pro mode value; update helpButton visibility
        checkSharedPrefs();
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
            int title = getArguments().getInt("title");

            return new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialogTheme)
                    .setTitle(title)
                    .setPositiveButton(R.string.alert_dialog_delete_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
//                                    Log.d(TAG, "Delete button clicked!");
                                    ((DinnerListActivity) requireActivity()).doPositiveClick();
                                }
                            }
                    )
                    .setNegativeButton(R.string.button_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((DinnerListActivity) requireActivity()).doNegativeClick();
                                }
                            }
                    )
                    .create();
        }
    }

    void showDeleteDialog() {
        AppCompatDialogFragment newFragment = DeleteDialogFragment.newInstance(
                R.string.batch_delete_alert_title);
        newFragment.show(getSupportFragmentManager(), "dialog");

    }

    public void doPositiveClick() {

        for (int i = 0; i < mIdList.size(); i++) {
//            Log.d(TAG, "Id to delete is " + mIdList.get(i));
            mDbHelper.open();
            mDbHelper.deleteDinner(mIdList.get(i));
            mDbHelper.close();
        }
        //Happy deletion announcement
        Toast.makeText(getApplicationContext(), "Dinners deleted",
                    Toast.LENGTH_LONG).show();

        //Todo: Do I need to clear mIdList manually? It gets cleared programmatically, I think.
//        mIdList.clear();

        //Relaunch DinnerListActivity to show updated list of dinners
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void doNegativeClick() {
        //Action is cancelled
    }

}
