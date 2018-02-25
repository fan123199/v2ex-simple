package im.fdx.v2ex.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.ui.main.TopicsDao


@Database(entities = [(Topic::class)], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicsDao
}