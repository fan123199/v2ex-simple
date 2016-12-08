package im.fdx.v2ex.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by fdx on 2016/9/11.
 * fdx will maintain it
 */
public class ContentUtilsTest {
    @Test
    public void formatContent() throws Exception {
        String s  = "href=\"/go/";
        String out = ContentUtils.formatContent(s);
        assertEquals(out,"href=\"https://www.v2ex.com/go/");
    }

}
