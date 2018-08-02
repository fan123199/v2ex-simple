package im.fdx.v2ex.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.ui.main.TopicDao
import im.fdx.v2ex.ui.node.Node
import im.fdx.v2ex.ui.node.NodeDao


@Database(entities = [Topic::class, Node::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun nodeDao(): NodeDao
}