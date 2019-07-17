package im.fdx.v2ex.ui.details

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MyReplyDao {

    @Query("SELECT * FROM my_reply WHERE topic_id = (:topicId)")
    fun getMyReplyById(topicId: String) : MyReply?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(myReply: MyReply)
}