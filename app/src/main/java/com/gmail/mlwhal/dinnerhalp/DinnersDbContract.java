package com.gmail.mlwhal.dinnerhalp;

//Created on 22 Aug 2018
//Thanks to https://developer.android.com/training/data-storage/sqlite#DefineContract

import android.provider.BaseColumns;

final class DinnersDbContract {
    // To prevent someone from accidentally instantiating the contract class,
    // the constructor is private

    private DinnersDbContract() {
    }

    /* Inner class that defines the table contents */
    static class DinnerEntry implements BaseColumns {
        static final String DATABASE_TABLE = "dinners";
        static final String KEY_NAME = "name";
        static final String KEY_METHOD = "method";
        static final String KEY_TIME = "time";
        static final String KEY_SERVINGS = "servings";
        static final String KEY_PICPATH = "picpath";
        static final String KEY_PICDATA = "picdata";
        static final String KEY_RECIPE = "recipe";
        static final String KEY_ROWID = "_id";

        private static final String TAG = "DinnersDbContract";
    }
}
