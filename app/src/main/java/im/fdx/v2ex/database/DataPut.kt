package im.fdx.v2ex.database

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by a708 on 15-8-20.
 * 数据库
 */
class DataPut(vDbHelper: SQLiteOpenHelper) {
    internal var db: SQLiteDatabase = vDbHelper.writableDatabase
}
