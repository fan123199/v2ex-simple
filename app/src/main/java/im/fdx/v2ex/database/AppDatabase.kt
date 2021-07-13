package im.fdx.v2ex.database

import androidx.room.Database
import androidx.room.RoomDatabase
import im.fdx.v2ex.ui.topic.MyReply
import im.fdx.v2ex.ui.topic.MyReplyDao
import im.fdx.v2ex.ui.main.Topic
import im.fdx.v2ex.ui.main.TopicDao
import im.fdx.v2ex.ui.node.Node
import im.fdx.v2ex.ui.node.NodeDao


@Database(entities = [Topic::class, Node::class, MyReply::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun nodeDao(): NodeDao
    abstract fun myReplyDao(): MyReplyDao
}