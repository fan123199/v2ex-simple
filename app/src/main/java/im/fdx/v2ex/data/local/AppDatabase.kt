package im.fdx.v2ex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import im.fdx.v2ex.data.model.MyReply
import im.fdx.v2ex.data.local.MyReplyDao
import im.fdx.v2ex.data.model.Topic
import im.fdx.v2ex.data.local.TopicDao
import im.fdx.v2ex.data.model.Node
import im.fdx.v2ex.data.local.NodeDao


@Database(entities = [Topic::class, Node::class, MyReply::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun nodeDao(): NodeDao
    abstract fun myReplyDao(): MyReplyDao
}


