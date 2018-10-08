package im.fdx.v2ex.ui.node

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NodeDao {


  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertNode(vararg nodes: Node)

  @Query("select * from node ")
  fun getNodes(): List<Node>

}