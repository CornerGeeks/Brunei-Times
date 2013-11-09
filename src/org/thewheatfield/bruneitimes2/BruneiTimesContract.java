package org.thewheatfield.bruneitimes2;

import android.provider.BaseColumns;

public final class BruneiTimesContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public BruneiTimesContract() {}

    /* Inner class that defines the table contents */
    public static abstract class BruneiTimesEdition implements BaseColumns {
        public static final String TABLE_NAME = "edition";
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_KEY = "key";
        public static final String COLUMN_NAME_VALUE = "value";
        
        
    }
}	
