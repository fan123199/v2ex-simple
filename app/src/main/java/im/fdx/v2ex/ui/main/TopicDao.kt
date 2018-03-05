package im.fdx.v2ex.ui.main

import android.arch.persistence.room.*


@Dao
interface TopicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(vararg users: Topic)

    @Insert
    fun insertBothTopic(user1: Topic, user2: Topic)

    @Insert
    fun insertTopicAndFriends(user: Topic, friends: List<Topic>)

    @Update
    fun updateTopic(vararg users: Topic)

    @Delete
    fun deleteTopic(vararg users: Topic)


    @Query("SELECT * FROM topic WHERE replies > :replyNum")
    fun loadAllUsersOlderThan(replyNum: Int): List<Topic>
}