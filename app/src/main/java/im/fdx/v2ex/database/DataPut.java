package im.fdx.v2ex.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by a708 on 15-8-20.
 * 数据库
 */
public class DataPut {

    SQLiteDatabase db;
    public DataPut(){
    }

    public DataPut(SQLiteOpenHelper vDbHelper) {
        db = vDbHelper.getWritableDatabase();
    }
}
