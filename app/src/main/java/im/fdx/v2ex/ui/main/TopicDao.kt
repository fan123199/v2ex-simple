package im.fdx.v2ex.ui.main

import androidx.room.*


@Dao
interface TopicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTopic(vararg topics: Topic)

    @Insert
    fun insertBothTopic(topic1: Topic, topic2: Topic)

    @Update
    fun updateTopic(vararg topic: Topic)

    @Delete
    fun deleteTopic(vararg topic: Topic)


    @Query("SELECT * FROM topic WHERE replies > :replyNum")
    fun loadAllUsersOlderThan(replyNum: Int): List<Topic>
}