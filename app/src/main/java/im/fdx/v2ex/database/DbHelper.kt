package im.fdx.v2ex.database

import android.arch.persistence.room.Room
import im.fdx.v2ex.MyApp


object DbHelper {

    var db = Room.databaseBuilder(MyApp.get(),
            AppDatabase::class.java, "v2ex.db").build()
}