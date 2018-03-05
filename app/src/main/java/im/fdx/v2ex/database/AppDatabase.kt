package im.fdx.v2ex.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.ui.main.TopicDao


@Database(entities = [Topic::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
}