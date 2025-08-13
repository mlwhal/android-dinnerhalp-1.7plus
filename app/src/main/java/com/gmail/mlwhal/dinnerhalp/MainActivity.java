package com.gmail.mlwhal.dinnerhalp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
//import java.util.Objects;
//import java.util.Locale;

//import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
//import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentPagerAdapter;
import android.os.Bundle;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.gmail.mlwhal.dinnerhalp.ui.main.SectionsPagerAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;


public class MainActivity extends AppCompatActivity {

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    //Track which tab to show when navigating from other activities
    public int mFragmentTracker = 0;

    //Two shared preferences need tracking: mBackupNum and darkModePref
    int mBackupNum;
    String mDarkModePref;

    //TAG String used for logging
    private static final String TAG = MainActivity.class.getSimpleName();
    static final int PICK_IMPORT_FILE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get info from extras to determine which tab to show; mFragmentTracker is then used by
        //mViewPager to select the initial tab
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                mFragmentTracker = 0;
                Log.d(TAG, "onCreate: Extras are null");
            } else {
                mFragmentTracker = extras.getInt("FRAGMENT_TRACKER");
                Log.d(TAG, "onCreate: Extras not null, mFragmentTracker = " + mFragmentTracker);
            }
        } else {
            mFragmentTracker = savedInstanceState.getInt("FRAGMENT_TRACKER");
            Log.d(TAG, "onCreate: savedInstanceState, mFragmentTracker = " + mFragmentTracker);
        }

        // Set up the tab layout.
       TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this,
                getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);

        Log.d(TAG, "onCreate: mViewPager set, mFragmentTracker = " + mFragmentTracker);
        //Choose the correct tab based on info from saved state or extra, stored in mFragmentTracker
        mViewPager.setCurrentItem(mFragmentTracker);

        //Set up the floating action button click listener
        ExtendedFloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.d(TAG, "onCreate: FAB has been clicked");
                Intent intent = new Intent(MainActivity.this, AddDinnerActivity.class);
                startActivity(intent);
            }
        });

        //Show FAB only on the SearchFragment (case 0)
        //Todo: Forget this, show FAB on both fragments
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
//                switch (position) {
//                    case 0:
//                        fab.show();
//                        break;
//                    case 1:
//                        fab.hide();
//                        break;
//                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //Initialize PreferenceManager with default values (recommended by
        //https://developer.android.com/guide/topics/ui/settings.html)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        //Get current shared prefs for number of backups and dark mode status
        checkSharedPrefs();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
                //intent.putExtra("FRAGMENT_TRACKER", 1);
                //Log.d(TAG, "fragTracker is " + mFragmentTracker);
//                Log.d(TAG, "getSelNavIndx is " + getSupportActionBar().getSelectedNavigationIndex());
                this.startActivity(intent);
                return true;

            case R.id.action_settings:
                Intent intent2 = new Intent(this, SettingsActivity.class);
                this.startActivity(intent2);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    //Add an onSaveInstanceState() method to save currently selected tab
    @Override
    public void onSaveInstanceState(@NonNull Bundle outstate) {
        super.onSaveInstanceState(outstate);

        //Set mFragmentTracker to current tab in order to save info to outstate
        mFragmentTracker = mViewPager.getCurrentItem();
        outstate.putInt("FRAGMENT_TRACKER", mFragmentTracker);
        Log.d(TAG, "Save state! mFragmentTracker = " + mFragmentTracker);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mFragmentTracker = savedInstanceState.getInt("FRAGMENT_TRACKER");
        Log.d(TAG, "Restore state! mFragmentTracker = " + mFragmentTracker);
    }

    //Method to track which tab to load when navigating from other activities
    @Override
    public void onResume() {
        super.onResume();

        mViewPager.setCurrentItem(mFragmentTracker);
        Log.d(TAG, "onResume; mFragmentTracker is " + mFragmentTracker);
    }


    /**
     * A fragment containing the search screen view.
     */
    public static class SearchFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        //Custom click listener for ListView items
        static class SearchOnItemClickListener implements AdapterView.OnItemClickListener {

            SearchOnItemClickListener(MainActivity activity, SearchFragment frag) {
                theActivity = activity;
                theFragment = frag;
            }

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
//                Log.d(TAG, "Position clicked " + position);
                switch (position) {
                    case 0:
                        theActivity.confirmKeyword();
                        break;

                    case 1:
                        theActivity.confirmMethod();
                        break;

                    case 2:
                        theActivity.confirmTime();
                        break;

                    case 3:
                        theActivity.confirmServings();
                        break;

                    case 4:
                        Intent intent2 = new Intent(theActivity, DinnerListActivity.class);
                        theFragment.startActivity(intent2);
                        break;
                }

            }

            private MainActivity theActivity;
            private SearchFragment theFragment;
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static SearchFragment newInstance(int sectionNumber) {
            SearchFragment fragment = new SearchFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public SearchFragment() {
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_search, container, false);

            //Create ListView and add ArrayAdapter to display search options
            final ListView listView = rootView.findViewById(R.id.list);

            //Get list item names and image ids from array resources
            Resources res = getResources();
            String[] itemName = res.getStringArray(R.array.search_array);
            TypedArray imageResArray = res.obtainTypedArray(R.array.search_icon_array);
            int lgth = imageResArray.length();
            Integer[] imageId = new Integer[lgth];
            for (int i = 0; i < lgth; i++) {
                imageId[i] = imageResArray.getResourceId(i, 0);
            }
            imageResArray.recycle();

            //Create custom adapter and pass in context, image, and name info
            CustomListAdapter adapter = new CustomListAdapter(this.getActivity(), itemName,
                    imageId);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(
                    new SearchOnItemClickListener((MainActivity) getActivity(), this));
            return rootView;
        }

    }

    public static class KeywordDialogFragment extends AppCompatDialogFragment {
        static KeywordDialogFragment newInstance(int title) {
            KeywordDialogFragment frag = new KeywordDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            assert getArguments() != null;
            int title = getArguments().getInt("title");

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity(),
                    R.style.Theme_DinnerHalp_CustomAlertDialog);
            //Get the layout inflater
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            //Cast the Dialog as a View so I can get text out of the EditText
            //http://stackoverflow.com/questions/12799751/android-how-do-i-retrieve-edittext-gettext-in-custom-alertdialog
            //Inflate and set the layout
            //Pass null as the parent view because it's going in the dialog layout
            final View dialogView = inflater.inflate(R.layout.dialog_keywordsearch, null);
            builder.setTitle(title)
                    .setView(dialogView)
                    //Add action buttons
                    .setPositiveButton(R.string.button_search,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    //Pull user input from EditText to construct db query
                                    EditText keywordEditText = dialogView.findViewById(
                                            R.id.dialog_edittext_keyword);
                                    String keywordInput = keywordEditText.getText().toString();
                                    String whereClause = "name LIKE ? OR recipe LIKE ?";
//                                    Log.d(TAG, "Search column is " + whereClause);
//                                    Log.d(TAG, "Search string is " + "%" + keywordInput + "%");
                                    Intent intent = new Intent(getActivity(), DinnerListActivity.class);
                                    /*Flag this as a keyword search so that two WhereArgs are used
                                    * when the database is searched
                                    * */
                                    intent.putExtra("KEYWORD_SEARCH", true);
                                    intent.putExtra("SEARCH_COLUMN", whereClause);
                                    intent.putExtra("SEARCH_STRING", "%" + keywordInput + "%");
                                    startActivity(intent);

                                }

                            })
                    .setNegativeButton(R.string.button_cancel,
                            (dialog, id) -> KeywordDialogFragment.this.getDialog().cancel());
            return builder.create();
        }
    }

    //Method to invoke keyword search dialog fragment; called in click listener
    void confirmKeyword() {
        AppCompatDialogFragment newFragment = KeywordDialogFragment.newInstance(R.string.keyword_alert_title);
        newFragment.show(getSupportFragmentManager(), "keywordConfirm");
    }

    //Custom class for cooking method Alert Dialog
    public static class CookMethodDialogFragment extends AppCompatDialogFragment {
        static CookMethodDialogFragment newInstance(int title) {
            CookMethodDialogFragment frag = new CookMethodDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            assert getArguments() != null;
            int title = getArguments().getInt("title");
            //Create a string array to hold method values; these will be the db query terms.
            final String[] methods = getResources().getStringArray(R.array.method_array);

//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity(),
                    R.style.Theme_DinnerHalp_CustomAlertDialog);
            builder.setTitle(title)
                    .setItems(R.array.method_array, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int position) {
//                            Log.d(TAG, "Position clicked " + position);
                            //Query DB for selected method and display a list of dinners
                            Intent intent = new Intent(getActivity(), DinnerListActivity.class);
                            intent.putExtra("SEARCH_COLUMN", "method LIKE ?");
                            intent.putExtra("SEARCH_STRING", methods[position]);
                            startActivity(intent);
                        }

                    });
            return builder.create();
        }
    }

    //Method to invoke cooking method dialog fragment; called in click listener
    void confirmMethod() {
        AppCompatDialogFragment newFragment = CookMethodDialogFragment.newInstance(R.string.method_alert_title);
        newFragment.show(getSupportFragmentManager(), "methodConfirm");
    }

    //Custom class for cooking time Alert Dialog
    public static class CookTimeDialogFragment extends AppCompatDialogFragment {
        static CookTimeDialogFragment newInstance(int title) {
            CookTimeDialogFragment frag = new CookTimeDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            assert getArguments() != null;
            int title = getArguments().getInt("title");
            //Create a string array to hold time values; these will be the db query terms.
            final String[] times = getResources().getStringArray(R.array.time_array);

//            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity(),
                    R.style.Theme_DinnerHalp_CustomAlertDialog);
            builder.setTitle(title)
                    .setItems(R.array.time_array, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int position) {
//                            Log.d(TAG, "Position clicked " + position);
//                            Log.d(TAG, "Item clicked " + times[position]);
                            //Query DB for selected cook time and display a list of dinners
                            //Also pass in "time" as column to search
                            Intent intent = new Intent(getActivity(), DinnerListActivity.class);
                            intent.putExtra("SEARCH_COLUMN", "time LIKE ?");
                            intent.putExtra("SEARCH_STRING", times[position]);
                            startActivity(intent);
                        }

                    });
            return builder.create();
        }
    }

    //Method to invoke cooking time dialog fragment; called in click listener
    void confirmTime() {
        AppCompatDialogFragment newFragment = CookTimeDialogFragment.newInstance(R.string.time_alert_title);
        newFragment.show(getSupportFragmentManager(), "timeConfirm");
    }

    //Custom class for servings Alert Dialog
    public static class ServingsDialogFragment extends AppCompatDialogFragment {
        static ServingsDialogFragment newInstance(int title) {
            ServingsDialogFragment frag = new ServingsDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            assert getArguments() != null;
            int title = getArguments().getInt("title");
            //Create a string array to hold serving values; these will be the db query terms.
            final String[] servings = getResources().getStringArray(R.array.servings_array);

//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity(),
                    R.style.Theme_DinnerHalp_CustomAlertDialog);
            builder.setTitle(title)
                    .setItems(R.array.servings_array, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
//                            Log.d(TAG, "Position clicked " + position);
                            //Query DB for selected no. of servings and display a list of dinners
                            Intent intent = new Intent(getActivity(), DinnerListActivity.class);
                            intent.putExtra("SEARCH_COLUMN", "servings LIKE ?");
                            intent.putExtra("SEARCH_STRING", servings[position]);
                            startActivity(intent);
                        }
                    });
            return builder.create();
        }

        private static final String TAG = "ServingsDialogFrag";
    }

    //Method to invoke servings dialog fragment; called in click listener
    void confirmServings() {
        AppCompatDialogFragment newFragment = ServingsDialogFragment.newInstance(R.string.servings_alert_title);
        newFragment.show(getSupportFragmentManager(), "servingsConfirm");
//        Log.d(TAG, "Servings clicked!");
    }

    /**
     * A fragment containing the manage DB screen view.
     */
    public static class ManageFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        static class ManageOnItemClickListener implements AdapterView.OnItemClickListener {

            ManageOnItemClickListener(MainActivity activity,
                                      ManageFragment frag) {
                theActivity = activity;
                theFragment = frag;
            }

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
//                Log.d(TAG, "Position clicked " + position);

                switch (position) {
                    //Get dinner list in order to edit or delete
                    case 0:
                        Intent intent2 = new Intent(theActivity, DinnerListActivity.class);
                        theFragment.startActivity(intent2);
                        break;
                    //Delete all records in database
                    case 1:
                        theActivity.showDeleteDBDialog();
                        break;
                    //Copy database file to storage
                    case 2:
                        theActivity.copyDBtoStorage(theActivity, false);
                        break;
                    //Restore database file from storage
                    case 3:
                        //Check whether there are backup files before launching dialog
                        if (theActivity.checkFilesDir() == null) {
                            Log.d(TAG, "checkFilesDir() fired; file array = null");
                            Toast.makeText(theActivity.getApplicationContext(),
                                            R.string.restore_nobackup_alert, Toast.LENGTH_LONG)
                                    .show();
                            break;
                        } else {
                            if (theActivity.checkFilesDir().length == 0) {
                                Log.d(TAG, "checkFilesDir() fired; array length = 0");
                                Toast.makeText(theActivity.getApplicationContext(),
                                                R.string.restore_nobackup_alert, Toast.LENGTH_LONG)
                                        .show();
                                break;
                            } else {
                                Log.d(TAG, "checkFilesDir fired; checkFilesDir.length = " +
                                        theActivity.checkFilesDir().length);
                                theActivity.showRestoreDBDialog(theActivity.checkFilesDir().length);
                                break;
                            }
                        }
                    //Share/email item
                    case 4:
                        theActivity.shareDB(theActivity);
                        break;
                    //Launch import alert dialog
                    case 5:
                        theActivity.showImportDBDialog();
                        break;
                }

            }

            private MainActivity theActivity;
            private ManageFragment theFragment;

        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ManageFragment newInstance(int sectionNumber) {
            ManageFragment fragment = new ManageFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public ManageFragment() {
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_manage, container, false);

            //Create ListView and add CustomListAdapter to display manage dinner and DB options
            ListView listView1 = rootView.findViewById(R.id.list);

            Resources res = getResources();
            String[] itemName = res.getStringArray(R.array.manage_dinners_array);
            TypedArray imageResArray = res.obtainTypedArray(R.array.manage_dinner_icon_array);
            int lgth = imageResArray.length();
            Integer[] imageId = new Integer[lgth];
            for (int i = 0; i < lgth; i++) {
                imageId[i] = imageResArray.getResourceId(i, 0);
            }
            imageResArray.recycle();

            //Create custom adapter and pass in context, image, and name info
            CustomListAdapter adapter1 = new CustomListAdapter(this.getActivity(), itemName,
                    imageId);

            listView1.setAdapter(adapter1);
            listView1.setOnItemClickListener(
                    new ManageOnItemClickListener((MainActivity) getActivity(),
                            this));

            return rootView;
        }

    }

    //Dialog fragment to handle confirming that the user wants to delete the DB file
    public static class DeleteDBDialogFragment extends AppCompatDialogFragment {

        static DeleteDBDialogFragment newInstance(int title) {
            DeleteDBDialogFragment frag = new DeleteDBDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            assert getArguments() != null;
            int title = getArguments().getInt("title");

            return new MaterialAlertDialogBuilder(getActivity(), R.style.Theme_DinnerHalp_CustomAlertDialog)
                    .setTitle(title)
                    .setPositiveButton(R.string.alert_dialog_delete_ok,
                            (dialog, whichButton) -> ((MainActivity) requireActivity()).delAllDinners()
                    )
                    .setNegativeButton(R.string.button_cancel,
                            (dialog, whichButton) -> ((MainActivity) requireActivity())
                                    .doNegativeClick(0)
                    )
                    .create();
        }
    }

    void showDeleteDBDialog() {
        AppCompatDialogFragment newFragment = DeleteDBDialogFragment.newInstance(
                R.string.delete_db_alert_title);
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    public void delAllDinners() {
        //Open a database object and delete all rows
//        Log.d(TAG, "Positive button clicked");
        DinnersDbAdapter mDbHelper;
        mDbHelper = new DinnersDbAdapter(this);
        mDbHelper.open();

        //Method clearAllDinners() returns an int giving the number of rows deleted
        int rowsDeleted = mDbHelper.clearAllDinners();
        if (rowsDeleted == 0) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.delete_db_cancel_toast),
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "All " + rowsDeleted + " dinners deleted",
                    Toast.LENGTH_LONG).show();
        }
        mDbHelper.close();

    }

    public void doNegativeClick(int position) {
        //Go back to manage fragment
//        Log.d(TAG, "Cancel button clicked");
        //Three alert dialogs use cancel: 0-delete database; 1-import database file, 2-restore db file
        switch (position) {
            case 0:
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.delete_db_cancel_toast),
                        Toast.LENGTH_LONG).show();
                break;
            case 1:
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.import_cancel_alert),
                        Toast.LENGTH_LONG).show();
                break;
            case 2:
                Toast.makeText(getApplicationContext(),
                        R.string.restore_cancel_alert,
                        Toast.LENGTH_LONG).show();
        }
    }

    //Method to retrieve any backup files in app storage; will return null if no
    //files directory exists (empty directory returns an array of 0 length)
    public File[] checkFilesDir() {
        Context context = getApplicationContext();
//        File file = new File(context.getFilesDir() + "/");
        File file = new File(context.getExternalFilesDir(null), "/");
        return file.listFiles();
    }

    //Custom class for restore db dialog fragment
    public static class RestoreDBDialogFragment extends AppCompatDialogFragment {
        static RestoreDBDialogFragment newInstance (int title, int fileCount) {
            RestoreDBDialogFragment frag = new RestoreDBDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            args.putInt("filecount", fileCount);
            frag.setArguments(args);
            return frag;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            assert getArguments() != null;
            int title = getArguments().getInt("title");
            int fileCount = getArguments().getInt("filecount");

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(),
                    R.style.Theme_DinnerHalp_CustomAlertDialog).setMessage(title);
            builder.setPositiveButton(R.string.restore_positive_button,
                    (dialog, whichButton) -> ((MainActivity) requireActivity())
                            .restoreLastBackup(false, -1));
            builder.setNegativeButton(R.string.button_cancel,
                    (dialog, whichButton) -> ((MainActivity) requireActivity())
                            .doNegativeClick(2));
            //Neutral button to choose backup file is used only if there is more than 1 backup file
            if (fileCount > 1) {
                builder.setNeutralButton(R.string.restore_neutral_button,
                        (dialog, whichButton) -> ((MainActivity) requireActivity())
                                .chooseBackupFile());
            } else {
                Log.d(TAG, "File count is " + fileCount + " so no neutral button needed");
            }
            return builder.create();

//            return new MaterialAlertDialogBuilder(getActivity(), R.style.Theme_DinnerHalp_CustomAlertDialog)
//                    .setTitle(title)
//                    .setPositiveButton(R.string.restore_positive_button,
//                            (dialog, whichButton) -> ((MainActivity) requireActivity())
//                    .restoreLastBackup()
//                    )
//                    .setNeutralButton(R.string.restore_neutral_button,
//                            (dialog, whichButton) -> ((MainActivity) requireActivity())
//                    .chooseBackupFile()
//                    )
//                    .setNegativeButton(R.string.button_cancel,
//                            (dialog, whichButton) -> ((MainActivity) requireActivity())
//                                    .doNegativeClick(2)
//                    ).create();
        }
    }

    public void showRestoreDBDialog(int fileCount) {
        AppCompatDialogFragment newFragment = RestoreDBDialogFragment.newInstance(
                R.string.restore_alert_title, fileCount);
        newFragment.show(getSupportFragmentManager(), "dialog");

    }

    //Method to restore most recent backup file
    public void restoreLastBackup(boolean chooseFiles, int pos) {
        File[] files = checkFilesDir();
        Log.d(TAG, "restoreBackupDB fired, checkFilesDir.length = " + checkFilesDir().length);
        //Double check that file list is not empty; this should never fire but who knows?
        if (files == null) {
            Toast.makeText(getApplicationContext(), R.string.restore_nobackup_alert,
                    Toast.LENGTH_LONG).show();
            //Todo: somehow kill the dialog? Test this use case
        } else {
            //Handle backup differently from RestoreDBDialog and ChooseFileDialog
            if (!chooseFiles) {
                //Get last backup file; the latest is always at the end of the array
                Log.d(TAG, "restoreBackupDB: files in file array are: " +
                        Arrays.toString(checkFilesDir()));
                Uri fileUri = null;   //Needs to be outside try block
                try {
                    //Todo: Create custom FileProvider as recommended here:
                    //https://developer.android.com/reference/androidx/core/content/FileProvider
                    fileUri = FileProvider.getUriForFile(MainActivity.this,
                            "com.gmail.mlwhal.dinnerhalp.fileprovider", files[files.length - 1]);
                    Log.d(TAG, "restoreBackupDB: fileUri is " + fileUri);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getApplicationContext(), R.string.restore_cancel_alert, Toast.LENGTH_LONG)
                            .show();
                    e.printStackTrace();
                }
                if (fileUri != null) {
                    boolean restoreFile = true;
                    Log.d(TAG, "restoreBackupDB: Launching overwriteDB, restoreFile = " + restoreFile);
                    overwriteDB(fileUri, restoreFile);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.restore_cancel_alert,
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Log.d(TAG, "restoreBackupDB: Coming from ChooseFileDialog");
                Uri fileUri = null;   //Needs to be outside try block
                try {
                    //Todo: Create custom FileProvider as recommended here:
                    //https://developer.android.com/reference/androidx/core/content/FileProvider
                    fileUri = FileProvider.getUriForFile(MainActivity.this,
                            "com.gmail.mlwhal.dinnerhalp.fileprovider", files[pos]);
                    Log.d(TAG, "restoreBackupDB: fileUri is " + fileUri);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getApplicationContext(), R.string.restore_cancel_alert, Toast.LENGTH_LONG)
                            .show();
                    e.printStackTrace();
                }
                if (fileUri != null) {
                    boolean restoreFile = true;
                    Log.d(TAG, "restoreBackupDB: Launching overwriteDB, restoreFile = " + restoreFile);
                    overwriteDB(fileUri, restoreFile);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.restore_cancel_alert,
                            Toast.LENGTH_LONG).show();
                }
            }
//                try {
//                    InputStream restoreDBStream = getContentResolver().openInputStream(fileUri);
//                    File currentDB = getApplicationContext()
//                            .getDatabasePath(getString(R.string.filename_full_sharedb));
//                    OutputStream os = new FileOutputStream(currentDB);
//                    byte[] buffer = new byte[1024];
//                    int bytesRead;
//                    if (restoreDBStream != null) {
//                        while ((bytesRead = restoreDBStream.read(buffer)) != -1) {
//                            os.write(buffer, 0, bytesRead);
//                        }
//                        restoreDBStream.close();
//                    }
//                    os.flush();
//                    os.close();
//                    Toast.makeText(getApplicationContext(), R.string.restore_success_alert,
//                            Toast.LENGTH_LONG).show();
//                } catch (IOException e) {
//                    Toast.makeText(getApplicationContext(), R.string.restore_cancel_alert,
//                            Toast.LENGTH_LONG).show();
//
//                }
//
//            } else {
//                Toast.makeText(getApplicationContext(), R.string.restore_cancel_alert,
//                        Toast.LENGTH_LONG).show();
//            }
        }
    }

    public void chooseBackupFile() {
        Log.d(TAG, "chooseBackupFile fired");
        File[] files = checkFilesDir();
        String[] backupFileNames = new String[files.length];
        for (int j = 0; j < files.length; j++) {
            String backupFilePath = String.valueOf(files[j]);
            String backupFileName = backupFilePath.substring(67);
            backupFileNames[j] = backupFileName;
        }
        Log.d(TAG, "getBackupList: backupFileNames are " + Arrays.toString(backupFileNames));
        //Todo: Replace this builder stuff with custom dialog class
        showChooseFileDialog(backupFileNames);
//        MaterialAlertDialogBuilder chooseFileDialogBuilder = new MaterialAlertDialogBuilder(MainActivity.this,
//                R.style.Theme_DinnerHalp_CustomAlertDialog);
//        chooseFileDialogBuilder.setTitle(R.string.choose_backup_alert_title);
//        chooseFileDialogBuilder.setItems(backupFileNames, new DialogInterface.OnClickListener() {
//            @Override
//                    public void onClick(DialogInterface dialog, int pos) {
//                Uri fileUri = Uri.fromFile(files[pos]);
//                Log.d(TAG, "chooseBackupFile: selected fileUri is " + fileUri);
//                boolean restoredFile = true;
//                overwriteDB(fileUri, restoredFile);
//            }
//        });
//        chooseFileDialogBuilder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                doNegativeClick(2);
//                            }
//                        });
//        androidx.appcompat.app.AlertDialog dialog = chooseFileDialogBuilder.create();
//        dialog.show();
    }

    //Custom class for choose file dialog fragment
    public static class ChooseFileDialogFragment extends AppCompatDialogFragment {
        static ChooseFileDialogFragment newInstance (int title, String[] filePaths) {
            ChooseFileDialogFragment frag = new ChooseFileDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            args.putStringArray("filepaths", filePaths);
            frag.setArguments(args);
            return frag;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            assert getArguments() != null;
            int title = getArguments().getInt("title");
            String[] filePaths = getArguments().getStringArray("filepaths");

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(),
                    R.style.Theme_DinnerHalp_CustomAlertDialog).setTitle(title);
            builder.setItems(filePaths, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int pos) {
                    Log.d(TAG, "ChooseFileDialog: List item clicked at position " + pos);
                    ((MainActivity) requireActivity()).restoreLastBackup(true, pos);
                }
            });
            builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((MainActivity) requireActivity()).doNegativeClick(2);
                }
            });
            return builder.create();
        }
    }

    public void showChooseFileDialog(String[] filePaths) {
        AppCompatDialogFragment newFragment = ChooseFileDialogFragment.newInstance(
                R.string.choose_backup_alert_title, filePaths);
        newFragment.show(getSupportFragmentManager(), "dialog");

    }

    //Method to overwrite existing DB with restored backup or imported file;
    //the boolean controls toast messaging
    public void overwriteDB(Uri fileUri, boolean restoredFile) {
        if (fileUri != null) {
            try {
                InputStream inputDBStream = getContentResolver().openInputStream(fileUri);
                File currentDB = getApplicationContext()
                        .getDatabasePath(getString(R.string.filename_full_sharedb));
                OutputStream outputDBStream = new FileOutputStream(currentDB);
                byte[] buffer = new byte[1024];
                int bytesRead;
                if (inputDBStream != null) {
                    while ((bytesRead = inputDBStream.read(buffer)) != -1) {
                        outputDBStream.write(buffer, 0, bytesRead);
                    }
                    inputDBStream.close();
                }
                outputDBStream.flush();
                outputDBStream.close();
                if (restoredFile) {
                    Toast.makeText(getApplicationContext(), R.string.restore_success_alert,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.import_success_alert,
                            Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                if (restoredFile) {
                    Toast.makeText(getApplicationContext(), R.string.restore_cancel_alert,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.import_cancel_alert,
                            Toast.LENGTH_LONG).show();
                }
            }
        } else {
            if (restoredFile) {
                Toast.makeText(getApplicationContext(), R.string.restore_cancel_alert,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.import_cancel_alert,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

//    //Custom class for dialog fragment to choose which backup to restore
//    public static class ChooseBackupDialogFragment extends AppCompatDialogFragment {
//        static ChooseBackupDialogFragment newInstance(int title) {
//            ChooseBackupDialogFragment frag = new ChooseBackupDialogFragment();
//            Bundle args = new Bundle();
//            args.putInt("title", title);
//            frag.setArguments(args);
//            return frag;
//        }
//
//        @NonNull
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            assert getArguments() != null;
//            int title = getArguments().getInt("title");
//            //Create a string array to list backup file names
//            File[] files = checkFilesDir();
//        }
//    }

    //Custom class for import dialog fragment
    public static class ImportDBDialogFragment extends AppCompatDialogFragment {
        static ImportDBDialogFragment newInstance(int title) {
            ImportDBDialogFragment frag = new ImportDBDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            assert getArguments() != null;
            int title = getArguments().getInt("title");

            return new MaterialAlertDialogBuilder(getActivity(), R.style.Theme_DinnerHalp_CustomAlertDialog)
                    .setMessage(title)
                    .setPositiveButton(R.string.import_positive_button,
                            (dialog, whichButton) -> ((MainActivity) requireActivity())
                                    .importDBFileChooser()
                    )
                    .setNegativeButton(R.string.button_cancel,
                            (dialog, whichButton) -> ((MainActivity) requireActivity())
                                    .doNegativeClick(1)
                    ).create();
        }

    }

    void showImportDBDialog() {
        AppCompatDialogFragment newFragment = ImportDBDialogFragment.newInstance(
                R.string.import_alert_title);
        newFragment.show(getSupportFragmentManager(), "dialog");

    }

    //Method to write current DB file to storage and delete old backups if needed
    //This is also called when user wants to share the DB so the file can be sent to other apps
    public String copyDBtoStorage(Context ctx, Boolean shareStatus) {
        File backupDB;
        String filenameFull = null;    //Declared outside of try block so the value can be returned
        try {
            File storageDir;
            //Choose cache_dir if shareStatus == true; file can be temporary
            if (shareStatus) {
                storageDir = getApplicationContext().getExternalCacheDir();
            } else {
                storageDir = getApplicationContext().getExternalFilesDir(null);
            }
            Log.d(TAG, "FilesDir is " + storageDir);
            //Add datestamp to backup file name
            SimpleDateFormat formatter = new SimpleDateFormat(getString(R.string.date_format));
            Date now = new Date();
            filenameFull = getString(R.string.filename_sharedb) + formatter.format(now) + ".db";

            if (storageDir.canWrite()) {
                //Get current database file and write a backup file
                File currentDB = ctx.getDatabasePath(getString(R.string.filename_full_sharedb));
                Log.d(TAG, "currentDB = " + currentDB);
                backupDB = new File(storageDir, filenameFull);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    //Toast the backup only when the user has clicked backup, not share
                    if (!shareStatus) {
                        Log.d(TAG, "File copied to storage");
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.backup_db_success), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "No file copied; currentDB does not exist");
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.backup_db_fail), Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(TAG, "Can't write to storageDir; storageDir is " + storageDir);
                Toast.makeText(getApplicationContext(),
                        getString(R.string.backup_db_fail), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getString(R.string.backup_db_fail),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        //Check shared preferences and delete any extra stored backup files
        //This only happens when the user wants to backup, not share, the db
        if (!shareStatus) {
//            int fileNumberPref = checkSharedPrefs();
//            Log.d(TAG, "copyDBtoStorage: fileNumberPref = " + fileNumberPref);

            try {
                File storageDir = getApplicationContext().getExternalFilesDir(null);
                if (storageDir.canRead()) {
                    //Get current list of files in directory
                    File[] files = storageDir.listFiles();
                    int lngth = files.length;
//                    Log.d(TAG, "copyDBtoStorage: Number of files is " + lngth);
                    //Sort the backup files by name
                    Arrays.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File file1, File file2) {
                            return file1.getName().compareTo(file2.getName());
                        }
                    });
                    //Calculate how many files beyond the preferred number there are (if any)
                    int extraFiles = lngth - mBackupNum;
                    boolean filesDeleted;
                    //Track how many files get deleted successfully
                    int filesDeletedCount = 0;
                    if (extraFiles > 0) {
                        for (int i = 0; i < extraFiles; i++) {
                            filesDeleted = files[i].delete();
                            if (filesDeleted) {
                                filesDeletedCount++;
                            }
                        }
                        //Tell the user that files were deleted
                        if (filesDeletedCount == 1) {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.backup_db_file_deleted),
                                    Toast.LENGTH_LONG).show();
                        } else if (filesDeletedCount > 1) {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.backup_db_files_deleted),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.backup_db_fail),
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

        return filenameFull; //Return name of backup file in case it's needed by shareDB()

    }

    //Method for importing database file from outside app
    public void importDBFileChooser() {
//        Log.d(TAG, "Import will happen now");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.import_chooser_title)),
                PICK_IMPORT_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Check which request is being handled
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMPORT_FILE_REQUEST) {
            //Check for positive result from activity
            if (resultCode == RESULT_OK) {
                boolean restoredFile = false;
                Log.d(TAG, "Importing DB; restoredFile boolean is " + restoredFile);
                //Todo: Need to test this on my phone, not emulator
                overwriteDB(data.getData(), restoredFile);
//                Uri uri = data.getData();
//                try {
//                    InputStream importDBStream = getContentResolver().openInputStream(uri);
//                    File currentDB = getApplicationContext()
//                            .getDatabasePath(getString(R.string.filename_full_sharedb));
//                    OutputStream os = new FileOutputStream(currentDB);
//                    byte[] buffer = new byte[1024];
//                    int bytesRead;
//                    if (importDBStream != null) {
//                        while ((bytesRead = importDBStream.read(buffer)) != -1) {
//                            os.write(buffer, 0, bytesRead);
//                        }
//                        importDBStream.close();
//                    }
//                    os.flush();
//                    os.close();
//                    Toast.makeText(getApplicationContext(), R.string.import_success_alert,
//                            Toast.LENGTH_LONG).show();
//
//                } catch (IOException e) {
//                    Toast.makeText(getApplicationContext(), R.string.import_cancel_alert,
//                            Toast.LENGTH_LONG).show();
//                }
//            } else {
//                //Handle negative result from activity
//                Toast.makeText(getApplicationContext(), R.string.import_cancel_alert,
//                        Toast.LENGTH_LONG).show();
            }
        }

    }

    //Method to share/email database file; makes a backup file to cache first to ensure that the
    //current version of the file is sent
    public void shareDB(Context ctx) {
        try {
            //Get path for readable database file
            //Cache directory is used because this is not trying to save a backup
            File storageDir = ctx.getExternalCacheDir();
            Log.d(TAG, "shareDB: storageDir is " + storageDir);

            //Make a new copy of the database and set it as the file to be sent
            String filename = copyDBtoStorage(ctx, true);
//            Log.d(TAG, "filename is " + filename);
//            File backupDB = new File(storageDir + "/" + getString(R.string.app_name), filename);
            File backupDB = new File(storageDir, filename);
//            Log.d(TAG, "shareDB: backupDB is " + backupDB);

            //Get the Uri for the file to prepare for the Intent
            Uri fileUri = null;
            try {
                //Todo: Change to custom FileProvider
                fileUri = FileProvider.getUriForFile(MainActivity.this,
                        "com.gmail.mlwhal.dinnerhalp.fileprovider", backupDB);
                Log.d(TAG, "fileUri has been created: " + fileUri);
            } catch (IllegalArgumentException e) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.sharedb_no_backup), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

            //Create and launch share intent
            if (fileUri != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("*/*");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.intent_sharedb_subject));
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.intent_sharedb_message));
//            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(backupDB));
                //Grant permission to read fileUri
                shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setClipData(ClipData.newRawUri("", fileUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                ctx.startActivity(Intent.createChooser(shareIntent, getString(R.string.sharedb_title)));
            } else {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.sharedb_no_backup), Toast.LENGTH_LONG).show();
            }
            //Delete cached file once shared
            //Todo: This gets called too soon; need another strategy
//            backupDB.delete();

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.sharedb_no_backup), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    //Check shared preferences to find out how many backup files to keep; returns the int
    public void checkSharedPrefs() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //Get current number of backups to keep (used in copyDBtoStorage()
        mBackupNum = Integer.parseInt(sharedPref.getString(getResources()
                        .getString(R.string.pref_backup_number_key),
                getResources().getString(R.string.pref_backup_number_default)));
        Log.d(TAG, "checkSharedPrefs: Number of backups is set to " + mBackupNum);

        //Set theme to match current shared preference; this is needed when theme doesn't match system
        mDarkModePref = sharedPref.getString(getResources()
                        .getString(R.string.pref_theme_key),
                getResources().getString(R.string.pref_theme_default));
        switch (mDarkModePref) {
            case "MODE_NIGHT_FOLLOW_SYSTEM":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "MODE_NIGHT_NO":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "MODE_NIGHT_YES":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
        Log.d(TAG, "checkSharedPrefs: Night mode set to " + mDarkModePref);
    }
}
