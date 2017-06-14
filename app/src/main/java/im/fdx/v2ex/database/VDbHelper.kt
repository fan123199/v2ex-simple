package im.fdx.v2ex.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by a708 on 15-8-20.
 * 数据库帮助类，手写SQL啊，太复杂，不用，但留着继续学习
 */

//// TODO: 2017/3/10 不要怂，就是干
class VDbHelper(context: Context) : SQLiteOpenHelper(context, VDbHelper.DATABASE_NAME, null, VDbHelper.DATABASE_VERSION) {

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(TOPIC_TABLE_CREATE)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    companion object {

        fun instance(context: Context): VDbHelper = VDbHelper(context)
        val DATABASE_VERSION = 1
        val DATABASE_NAME = "v2ex.db"

        // 话题数据表(话题ID,收藏状态,阅读状态)
        val TOPIC_TABLE_NAME = "topics_table"
        val TOPIC_COLUMN_ID = "_id"
        val TOPIC_COLUMN_TOPICID = "topic_id"
        val TOPIC_COLUMN_FAVOR = "isfavored"
        val TOPIC_COLUMN_READ = "isread"

        private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TOPIC_TABLE_NAME
        val TOPIC_TABLE_CREATE = "CREATE TABLE $TOPIC_TABLE_NAME(" +
                "$TOPIC_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$TOPIC_COLUMN_TOPICID INTEGER UNIQUE NOT NULL, " +
                "$TOPIC_COLUMN_READ INTEGER NOT NULL, " +
                "$TOPIC_COLUMN_FAVOR INTEGER NOT NULL);"
    }

}
