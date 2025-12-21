package im.fdx.v2ex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import im.fdx.v2ex.data.model.MyReply

@Dao
interface MyReplyDao {

    @Query("SELECT * FROM my_reply WHERE topic_id = (:topicId)")
    suspend fun getMyReplyById(topicId: String) : MyReply?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(myReply: MyReply): Long
}

