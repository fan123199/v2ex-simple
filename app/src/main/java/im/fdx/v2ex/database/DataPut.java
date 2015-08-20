package im.fdx.v2ex.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by a708 on 15-8-20.
 */
public class DataPut {

    SQLiteDatabase db;
    public DataPut(){
    }

    public DataPut(VDbHelper vDbHelper) {
        db = vDbHelper.getWritableDatabase();
    }
}
