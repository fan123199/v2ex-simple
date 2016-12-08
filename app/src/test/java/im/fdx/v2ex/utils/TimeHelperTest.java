package im.fdx.v2ex.utils;

import android.util.Log;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by fdx on 2016/9/11.
 * fdx will maintain it
 */
public class TimeHelperTest {
    @Test
    public void getAbsoluteTime() throws Exception {
       String s =  TimeHelper.getAbsoluteTime(1341262360);
//        assertEquals("succeed", "2012-7-3 4:52:40",s);
    }

}