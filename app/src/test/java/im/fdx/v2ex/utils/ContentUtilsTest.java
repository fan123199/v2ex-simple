package im.fdx.v2ex.utils;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.view.View.Z;
import static org.junit.Assert.*;

/**
 * Created by fdx on 2016/9/11.
 * fdx will maintain it
 */
public class ContentUtilsTest {
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Test
    public void formatContent() throws Exception {
        String s  = "href=\"/go/";
        String out = ContentUtils.formatContent(s);


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ", Locale.CHINA);
        Date out2 = simpleDateFormat.parse("2017-03-07 01:45:30 ");
        String out3 = out2.toString();
        long sss = out2.getTime();
        assertEquals(out,"href=\"https://www.v2ex.com/go/");
    }

}
