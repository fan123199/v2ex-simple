package im.fdx.v2ex.utils

import android.os.Build
import android.support.annotation.RequiresApi
import org.junit.Test

/**
 * Created by fdx on 2017/3/17.
 */
class SecureUtilsTest {

    //    public SecureUtils utils = new SecureUtils();

    //需要mock才能进行，失败
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Test
    @Throws(Exception::class)
    fun encrypt() {
        //        SecureUtils utils = new SecureUtils();
        //        String encrypt = utils.encrypt("what the fuck");
        //        String decrypt = utils.decrypt(encrypt);
        //        assertEquals("what the fuck", decrypt);
    }

    @Test
    @Throws(Exception::class)
    fun decrypt() {

    }

}