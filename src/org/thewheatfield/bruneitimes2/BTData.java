package org.thewheatfield.bruneitimes2;

import java.io.File;

import org.thewheatfield.bruneitimes2.BruneiTimesContract.BruneiTimesEdition;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class BTData {
	public class BruneiTimesDbHelper extends SQLiteOpenHelper {
	    private static final String DATABASE_NAME = "BruneiTimesData.dbBruneiTimesData.dbBruneiTimesData.dbBruneiTimesData.db";
	    private static final int DATABASE_VERSION = 1;

        private static final String TEXT_TYPE = " TEXT";
        private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + BruneiTimesEdition.TABLE_NAME + " (" +
            		BruneiTimesEdition._ID + " INTEGER PRIMARY KEY" +
            		", " + BruneiTimesEdition.COLUMN_NAME_KEY + TEXT_TYPE + 
            		", " + BruneiTimesEdition.COLUMN_NAME_VALUE + TEXT_TYPE + 
            " )";

        private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + BruneiTimesEdition.TABLE_NAME;

	    
	    public BruneiTimesDbHelper(Context context) {
	        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    }
	    public void onCreate(SQLiteDatabase db) {
	        db.execSQL(SQL_CREATE_ENTRIES);
	    }
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	        onCreate(db);
	    }
	    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	        onUpgrade(db, oldVersion, newVersion);
	    }
	}	
	
	private BruneiTimesDbHelper mDbHelper;
	private Context mContext;
	public BTData(Context context){
		mDbHelper = new BruneiTimesDbHelper(context);
		mContext = context;
	}
	public boolean deletePages(String date){
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		String selection = BruneiTimesEdition.COLUMN_NAME_KEY + " = ?";
		String[] selectionArgs = { String.valueOf(date) };
		db.delete(BruneiTimesEdition.TABLE_NAME, selection, selectionArgs);
		return true;
	}
	public boolean savePages(String date, int pages){
		deletePages(date);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(BruneiTimesEdition.COLUMN_NAME_KEY, date);
		values.put(BruneiTimesEdition.COLUMN_NAME_VALUE, pages+"");
		// Insert the new row, returning the primary key value of the new row
		long newRowId = 0;
		newRowId = db.insert(
		         BruneiTimesEdition.TABLE_NAME,
		         null,
		         values);
		db.close();
		return newRowId > 0;
	}
	public int getPages(String date){
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		// Define a projection that specifies which columns from the database
		// you will actually use after this query.
		String[] projection = {
		    BruneiTimesEdition.COLUMN_NAME_VALUE
		    };
		String sortOrder = BruneiTimesEdition._ID + " DESC";

		String selection = BruneiTimesEdition.COLUMN_NAME_KEY + " = ?";
		// Specify arguments in placeholder order.
		String[] selectionArgs = { String.valueOf(date) };
		
		Cursor cursor = db.query(
		    BruneiTimesEdition.TABLE_NAME,  // The table to query
		    projection,                               // The columns to return
		    selection,                                // The columns for the WHERE clause
		    selectionArgs,                            // The values for the WHERE clause
		    null,                                     // don't group the rows
		    null,                                     // don't filter by row groups
		    sortOrder                                 // The sort order
		    );
		
		try{
			cursor.moveToFirst();
			return cursor.getInt(0);
		}
		catch(Exception e){
			return 0;	
		}
		finally{
			db.close();
		}
	}	
	public File getCacheDir(){
		if (android.os.Build.VERSION.SDK_INT >= 8)
			return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), BT.FOLDER_NAME);
		else 
			return new File(new File(Environment.getExternalStorageDirectory(), "Pictures"), BT.FOLDER_NAME);
//		File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
//		File appCacheDir = new File(new File(dataDir, mContext.getPackageName()), "cache");
//		return appCacheDir;
	}
	public void deleteCache(String date){
		File cacheDir = getCacheDir();
		String folder = BTFileGenerator.getFolderName(date);
		if(folder != ""){
			File dir = new File(cacheDir,folder);
			if(dir.exists()){
				deleteDirectory(dir);
			}
		}
	}
	public static void deleteDirectory( File dir )
	{
	    if ( dir.isDirectory() )
	    {
	        String [] children = dir.list();
	        for ( int i = 0 ; i < children.length ; i ++ )
	        {
	         File child =    new File( dir , children[i] );
	         if(child.isDirectory()){
	             deleteDirectory( child );
	             child.delete();
	         }else{
	             child.delete();

	         }
	        }
	        dir.delete();
	    }
	}
}
