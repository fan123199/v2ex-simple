package im.fdx.v2ex.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by a708 on 15-8-20.
 * 数据库帮助类
 */
public class VDbHelper extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "V2ex.db";

    // 话题数据表(话题ID,收藏状态,阅读状态)
    public static final String TOPIC_TABLE_NAME = "topics_table";
    public static final String TOPIC_COLUMN_ID = "_id";
    public static final String TOPIC_COLUMN_TOPICID = "topic_id";
    public static final String TOPIC_COLUMN_FAVOR = "isfavored";
    public static final String TOPIC_COLUMN_READ = "isread";

    private static final String TOPIC_TABLE_CREATE = "CREATE TABLE " + TOPIC_TABLE_NAME
            + "(" + TOPIC_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TOPIC_COLUMN_TOPICID + " INTEGER UNIQUE NOT NULL, "
            + TOPIC_COLUMN_READ + " INTEGER NOT NULL, "
            + TOPIC_COLUMN_FAVOR + " INTEGER NOT NULL);";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TOPIC_TABLE_NAME;


    public VDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TOPIC_TABLE_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    private static volatile VDbHelper mVDBHelper;

    public static synchronized VDbHelper getInstance(Context context) {
        //SO 称之为double check
        if(mVDBHelper ==null) {
            synchronized (VDbHelper.class) {
                if(mVDBHelper ==null) {
                    mVDBHelper = new VDbHelper(context);
                }
            }
        }
        return mVDBHelper;
    }



}
