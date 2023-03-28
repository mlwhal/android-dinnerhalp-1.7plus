package com.gmail.mlwhal.dinnerhalp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;


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
                    + "servings text not null, picpath text, recipe text, picdata blob);";

    private static final String DATABASE_NAME = "dinnerData.db";
    private static final String DATABASE_TABLE = DinnersDbContract.DinnerEntry.DATABASE_TABLE;
    private static final int DATABASE_VERSION = 2;

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
            //If database is being upgraded, add the picdata column
//            Log.d(TAG, "onUpgrade is running");
            if (newVersion > oldVersion) {
                String alterString = "ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN " +
                        DinnersDbContract.DinnerEntry.KEY_PICDATA + " BLOB";
                db.execSQL(alterString);
//                Log.d(TAG, "Database has been upgraded");
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
     * @param picpath file path for photo
     * @param picdata byte array for saving photo inside database
     * @param recipe text of recipe
     * @return rowId or -1 if failed
     */
    long createDinner(String name, String method, String time, String servings,
                      String picpath, byte[] picdata, String recipe) {
        //If the user tries to create a dinner with no name, this is handled
        //in AddDinnerActivity
        ContentValues initialValues = new ContentValues();
        initialValues.put(DinnersDbContract.DinnerEntry.KEY_NAME, name);
        initialValues.put(DinnersDbContract.DinnerEntry.KEY_METHOD, method);
        initialValues.put(DinnersDbContract.DinnerEntry.KEY_TIME, time);
        initialValues.put(DinnersDbContract.DinnerEntry.KEY_SERVINGS, servings);
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

    //fetchAllDinners only fetches rowID and name columns, since that's what is needed in
    //DinnerListActivity.
    //It's not recommended to return all columns because that loads more data than needed.

    Cursor fetchAllDinners() {

        //Create string array to hold names of columns to be fetched
        String[] tableColumns = new String[] {
                DinnersDbContract.DinnerEntry.KEY_ROWID,
                DinnersDbContract.DinnerEntry.KEY_NAME
        };

        return mDb.query(DATABASE_TABLE, tableColumns, null, null,
                null, null,  DinnersDbContract.DinnerEntry.KEY_NAME + " ASC");
    }

    /**
     * Return a Cursor with all dinners that match a query on a particular column
     */
    //fetchDinnerSearch returns only rowID and name columns, since that's what is needed in
    //DinnerListActivity.
    //It's not recommended to return all columns because that loads more data than needed.
    //Todo: Also, shouldn't this throw SQLException?

    Cursor fetchDinnerSearch(boolean keywordSearch, String whereClause, String searchString) {

        //Create string array to hold names of columns to be fetched
        String[] tableColumns = new String[] {
                DinnersDbContract.DinnerEntry.KEY_ROWID,
                DinnersDbContract.DinnerEntry.KEY_NAME
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

        return mDb.query(DATABASE_TABLE, tableColumns, whereClause, whereArgs, null, null,
                 DinnersDbContract.DinnerEntry.KEY_NAME + " ASC");
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
    boolean updateDinner(long rowId, String name, String method, String time,
                                String servings, String picpath, byte[] picdata, String recipe) {
        ContentValues args = new ContentValues();
        args.put(DinnersDbContract.DinnerEntry.KEY_NAME, name);
        args.put(DinnersDbContract.DinnerEntry.KEY_METHOD, method);
        args.put(DinnersDbContract.DinnerEntry.KEY_TIME, time);
        args.put(DinnersDbContract.DinnerEntry.KEY_SERVINGS, servings);
        args.put(DinnersDbContract.DinnerEntry.KEY_PICPATH, picpath);
        args.put(DinnersDbContract.DinnerEntry.KEY_PICDATA, picdata);
        args.put(DinnersDbContract.DinnerEntry.KEY_RECIPE, recipe);

        return mDb.update(DATABASE_TABLE, args,
                DinnersDbContract.DinnerEntry.KEY_ROWID + "=" + rowId,
                null) > 0;
    }

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
