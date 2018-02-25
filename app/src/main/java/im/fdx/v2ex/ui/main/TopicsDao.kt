package im.fdx.v2ex.ui.main

import android.arch.persistence.room.*


@Dao
interface TopicsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(vararg users: Topic)

    @Insert
    fun insertBothTopicModels(user1: Topic, user2: Topic)

    @Insert
    fun insertTopicModelsAndFriends(user: Topic, friends: List<Topic>)

    @Update
    fun updateTopicModels(vararg users: Topic)

    @Delete
    fun deleteTopicModels(vararg users: Topic)


    @Query("SELECT * FROM topic WHERE replies > :replyNum")
    fun loadAllUsersOlderThan(replyNum: Int): List<Topic>
}