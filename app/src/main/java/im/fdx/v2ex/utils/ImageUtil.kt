package im.fdx.v2ex.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Base64
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import im.fdx.v2ex.BuildConfig
import im.fdx.v2ex.GlideApp
import im.fdx.v2ex.myApp
import org.jetbrains.anko.toast


object ImageUtil {


    fun shareImage(context: Context, url:String) {
        downloadImage(url) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(Intent.EXTRA_STREAM, it)
            intent.type = "image/*"
            context.startActivity(Intent.createChooser(intent, "Share image via"))
        }

    }

    fun downloadImage(context: Context, url: String){
        downloadImage(url) {
            context.toast( "已保存图片在 " + it.toString())
        }
    }


    private fun downloadImage(url: String, ready : (Uri) ->Unit) {

        GlideApp.with(myApp)
                .asBitmap()
                .load(url)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val uri = saveBitmap(url,resource)
                        uri?.let { ready(it) }
                    }
                })
    }

    fun saveBitmap(url :String ,bitmap: Bitmap) : Uri? {
        var outStream: FileOutputStream? = null

        // Write to app data
        try {
            val dataPath = myApp.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val dir = File(dataPath!!.absolutePath + "/image")
            dir.mkdirs()
            val encodeToString = Base64.encodeToString(url.toByteArray(), Base64.DEFAULT)

            val fileName = String.format("%s.jpg", encodeToString)
            val outFile = File(dir, fileName)


            outStream = FileOutputStream(outFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            outStream.flush()
            outStream.close()

            val uri = FileProvider.getUriForFile(
                    myApp,
                    "im.fdx.v2ex${if (BuildConfig.DEBUG) ".debug" else ""}.provider",
                    outFile
            )
            return uri
        } catch (e: FileNotFoundException) {
            print("FNF")
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
        }
        return null
    }
}