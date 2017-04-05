package im.fdx.v2ex.utils;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by fdx on 2017/3/17.
 */
public class SecureUtilsTest {

//    public SecureUtils utils = new SecureUtils();

    //需要mock才能进行，失败
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Test
    public void encrypt() throws Exception {
//        SecureUtils utils = new SecureUtils();
//        String encrypt = utils.encrypt("what the fuck");
//        String decrypt = utils.decrypt(encrypt);
//        assertEquals("what the fuck", decrypt);
    }

    @Test
    public void decrypt() throws Exception {

    }

}