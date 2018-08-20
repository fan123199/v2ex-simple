package im.fdx.v2ex.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import im.fdx.v2ex.ui.Tab
import org.junit.Test

class GsonTest {


  @Test
  fun testList() {

    println(t(""))
    println(t(null))

    val s = """[{"name":"hehe", "title":"cc"}]"""
    val s2 = """[]"""
    println(t(s))

    println(t(s2))
  }

  fun t(str: String?): List<Tab>? {
    return Gson().fromJson<List<Tab>>(str, object : TypeToken<List<Tab>>() {}.type)
  }


}