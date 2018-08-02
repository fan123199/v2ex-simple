package im.fdx.v2ex.ui.node

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface NodeDao {


  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertNode(vararg nodes: Node)

  @Query("select * from node ")
  fun getNodes(): List<Node>

}