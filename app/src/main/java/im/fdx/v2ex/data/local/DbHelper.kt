package im.fdx.v2ex.data.local

import androidx.room.Room
import im.fdx.v2ex.MyApp


object DbHelper {

  var db = Room.databaseBuilder(MyApp.get(), AppDatabase::class.java, "v2ex.db")
      .fallbackToDestructiveMigration()
      .build()
}

