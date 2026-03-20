package com.gmail.mlwhal.dinnerhalp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;


/**
 * Created by marika on 7/25/15.
 * Based on the Notepad Tutorial from developer.android.com.
 */


class DinnersDbAdapter {

    private static final String TAG = "DinnersDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     * Note that the dinner name must be unique for each record.
     */
    private static final String DATABASE_CREATE =
            "create table dinners (_id integer primary key autoincrement, "
                    + "name text unique not null, method text not null, time text not null, "
                    + "servings text not null, datemade text, picpath text, recipe text, picdata blob);";

    private static final String DATABASE_NAME = "dinnerData.db";
    private static final String DATABASE_TABLE = DinnersDbContract.DinnerEntry.DATABASE_TABLE;
    private static final int DATABASE_VERSION = 3;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //If database is being upgraded, add relevant new column
            Log.d(TAG, "onUpgrade is running");
            if (newVersion > oldVersion) {
                if (newVersion == 2) {
                String alterString = "ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN " +
                        DinnersDbContract.DinnerEntry.KEY_PICDATA + " BLOB";
                db.execSQL(alterString);
//                Log.d(TAG, "Database has been upgraded from version 1 to 2");
                }

                if (newVersion == 3) {
                    String alterString2 = "ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN " +
                            DinnersDbContract.DinnerEntry.KEY_DATEMADE + " TEXT";
                    db.execSQL(alterString2);
                    Log.d(TAG, "Database has been upgraded from version 2 to 3");
                }
            }
        }

    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    DinnersDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the dinners database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */

    DinnersDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    void close() {
        mDbHelper.close();
    }

    /**
     * Create a new dinner using the data provided. If the dinner is
     * successfully created return the new rowId for that dinner, otherwise return
     * a -1 to indicate failure.
     *
     * @param name the name of the dinner
     * @param method the cooking method of the dinner (stovetop, oven, slow cooker)
     * @param time the cook time
     * @param servings the number of servings
     * @param datemade the date dinner was made
     * @param picpath file path for photo
     * @param picdata byte array for saving photo inside database
     * @param recipe text of recipe
     * @return rowId or -1 if failed
     */
    long createDinner(String name, String method, String time, String servings,
                      String datemade, String picpath, byte[] picdata, String recipe) {
        //If the user tries to create a dinner with no name, this is handled
        //in AddDinnerActivity
        ContentValues initialValues = new ContentValues();
        initialValues.put(DinnersDbContract.DinnerEntry.KEY_NAME, name);
        initialValues.put(DinnersDbContract.DinnerEntry.KEY_METHOD, method);
        initialValues.put(DinnersDbContract.DinnerEntry.KEY_TIME, time);
        initialValues.put(DinnersDbContract.DinnerEntry.KEY_SERVINGS, servings);
        initialValues.put(DinnersDbContract.DinnerEntry.KEY_DATEMADE, datemade);
        initialValues.put(DinnersDbContract.DinnerEntry.KEY_PICPATH, picpath);
        initialValues.put(DinnersDbContract.DinnerEntry.KEY_PICDATA, picdata);
        initialValues.put(DinnersDbContract.DinnerEntry.KEY_RECIPE, recipe);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the dinner with the given rowId
     *
     * @param rowId id of dinner to delete
     * @return true if deleted, false otherwise
     */
    boolean deleteDinner(long rowId) {

        return mDb.delete(DATABASE_TABLE,
                DinnersDbContract.DinnerEntry.KEY_ROWID + "=" + rowId,
                null) > 0;
    }

    /**
     * Return a Cursor over the list of all dinners in the database
     *
     * @return Cursor over all dinners
     * The parameters for query() are: table name, column name(s) to return, whereClause,
     * whereArgs, groupBy, having, orderBy
     */

    //fetchAllDinners only fetches rowID, name, and datemade columns, since that's what is needed in
    //DinnerListActivity.
    //It's not recommended to return all columns because that loads more data than needed.

    Cursor fetchAllDinners(boolean sortByName, boolean sortAsc) {

        //Create string array to hold names of columns to be fetched
        String[] tableColumns = new String[] {
                DinnersDbContract.DinnerEntry.KEY_ROWID,
                DinnersDbContract.DinnerEntry.KEY_NAME,
                DinnersDbContract.DinnerEntry.KEY_DATEMADE
        };

        String orderByString = getOrderByString(sortByName, sortAsc);

        return mDb.query(DATABASE_TABLE, tableColumns, null, null,
                null, null, orderByString);
    }

    /**
     * Return a Cursor with all dinners that match a query on a particular column
     */
    //fetchDinnerSearch returns only rowID, name, and datemade columns, since that's what is needed in
    //DinnerListActivity.
    //It's not recommended to return all columns because that loads more data than needed.
    //Todo: Shouldn't this throw SQLException?

    Cursor fetchDinnerSearch(boolean keywordSearch, String whereClause, String searchString,
                             boolean sortByName, boolean sortAsc) {

        //Create string array to hold names of columns to be fetched
        String[] tableColumns = new String[] {
                DinnersDbContract.DinnerEntry.KEY_ROWID,
                DinnersDbContract.DinnerEntry.KEY_NAME,
                DinnersDbContract.DinnerEntry.KEY_DATEMADE
        };

        String[] whereArgs;

        //Number of whereArgs is 1 for all searches except keyword, which needs 2
        if (keywordSearch) {
            whereArgs = new String[] {
                    searchString,
                    searchString
            };

        } else {
            whereArgs = new String[]{
                    searchString
            };
        }

        String orderByString = getOrderByString(sortByName, sortAsc);

        return mDb.query(DATABASE_TABLE, tableColumns, whereClause, whereArgs, null,
                null, orderByString);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     *
     * @param rowId id of dinner to retrieve
     * @return Cursor positioned to matching dinner, if found
     * @throws SQLException if dinner could not be found/retrieved
     */

    //fetchDinner returns all columns (third parameter of query) because all are needed for
    //ViewDinnerActivity.

    Cursor fetchDinner(long rowId) throws SQLException {

        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, null,
                        DinnersDbContract.DinnerEntry.KEY_ROWID + "=" + rowId,
                        null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    /**
     * Update the dinner using the details provided. The dinner to be updated is
     * specified using the rowId, and it is altered to use the
     * values passed in
     *
     * @param rowId id of dinner to update
     * @param name value to set dinner title to
     * @param method value to set cooking method to
     *               etc. etc.
     * @return true if the dinner was successfully updated, false otherwise
     * mDb.update() returns the number of rows updated in an int. Anything > 0 returns true.
     */
    boolean updateDinner(long rowId, String name, String method, String time, String servings,
                         String datemade, String picpath, byte[] picdata, String recipe) {
        ContentValues args = new ContentValues();
        args.put(DinnersDbContract.DinnerEntry.KEY_NAME, name);
        args.put(DinnersDbContract.DinnerEntry.KEY_METHOD, method);
        args.put(DinnersDbContract.DinnerEntry.KEY_TIME, time);
        args.put(DinnersDbContract.DinnerEntry.KEY_SERVINGS, servings);
        args.put(DinnersDbContract.DinnerEntry.KEY_DATEMADE, datemade);
        args.put(DinnersDbContract.DinnerEntry.KEY_PICPATH, picpath);
        args.put(DinnersDbContract.DinnerEntry.KEY_PICDATA, picdata);
        args.put(DinnersDbContract.DinnerEntry.KEY_RECIPE, recipe);

        return mDb.update(DATABASE_TABLE, args,
                DinnersDbContract.DinnerEntry.KEY_ROWID + "=" + rowId,
                null) > 0;
    }

    //Method that uses sorting parameters to determine how the data should be ordered
    //This is needed in two places, hence the separate method.
    //If sorting by date, a secondary sort statement ensures remaining dinners are A-Z
    String getOrderByString(boolean sortByName, boolean sortAsc) {
        String orderByString = null;
        if (sortByName && sortAsc) {
            orderByString = DinnersDbContract.DinnerEntry.KEY_NAME + " ASC";
        }
        if (sortByName && !sortAsc) {
            orderByString = DinnersDbContract.DinnerEntry.KEY_NAME + " DESC";
        }
        if (!sortByName && !sortAsc) {
            orderByString = DinnersDbContract.DinnerEntry.KEY_DATEMADE + " DESC, " +
                    DinnersDbContract.DinnerEntry.KEY_NAME + " ASC";
        }
        if (!sortByName && sortAsc) {
            orderByString = DinnersDbContract.DinnerEntry.KEY_DATEMADE + " ASC, " +
                    DinnersDbContract.DinnerEntry.KEY_NAME + " ASC";
        }
        Log.d(TAG, "In getOrderByString: orderByString is " + orderByString);
        return orderByString;

    }

//    //Method for clearing just the date made from a db row
//    //Currently unused because all columns are updated in AddDinnerActivity at once.
//    //Use this if you want to update just the date made in ViewDinnerActivity
//    //Although this doesn't update, just clears, but it's a start!
//    int clearDateMade(long rowId) {
//        ContentValues values = new ContentValues();
//        values.putNull(DinnersDbContract.DinnerEntry.KEY_DATEMADE);
//        String whereClause = DinnersDbContract.DinnerEntry.KEY_ROWID + " = ?";
//        String[] whereArgs = { String.valueOf(rowId) };
//
//        return mDb.update(DATABASE_TABLE, values, whereClause, whereArgs);
//    }

    //Method for deleting all rows in the database table
    int clearAllDinners() {

        //Passing 1 as the whereClause returns the number of rows deleted
        return mDb.delete(DATABASE_TABLE, "1", null);
    }

    ArrayList<String> fetchAllDinnersTest() {
        //Create string array to hold names of columns to be fetched
        String[] tableColumns = new String[] {
                DinnersDbContract.DinnerEntry.KEY_ROWID,
                DinnersDbContract.DinnerEntry.KEY_NAME
        };

        ArrayList<String> dinnerList = new ArrayList<>();
        Cursor dinnerCursor;

        dinnerCursor = mDb.query(DATABASE_TABLE, tableColumns, null, null,
                null, null,
                DinnersDbContract.DinnerEntry.KEY_NAME + " ASC");

        //Iterate through cursor to populate dinnerList
        if (dinnerCursor != null) {
            while (dinnerCursor.moveToNext()) {
                String dinnerName = dinnerCursor.getString(dinnerCursor
                        .getColumnIndexOrThrow(DinnersDbContract.DinnerEntry.KEY_ROWID));
                dinnerList.add(dinnerName);
            }
            dinnerCursor.close();
        }
        return dinnerList;
    }

}
