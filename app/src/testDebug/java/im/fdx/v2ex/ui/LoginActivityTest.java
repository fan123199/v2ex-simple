package im.fdx.v2ex.ui;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.mockito.internal.exceptions.ExceptionIncludingMockitoWarnings;

import static org.junit.Assert.*;

/**
 * Created by fdx on 2017/3/1.
 * fdx will maintain it
 */
public class LoginActivityTest {


    @Test
    public void testOutput() throws Exception {

        String body = "<div class=\"box\">\n" +
                "                                                       <div class=\"header\"><a href=\"/\">V2EX</a> <span class=\"chevron\">&nbsp;›&nbsp;</span> 登录 &nbsp;<li class=\"fa fa-lock\"></li></div>\n" +
                "                                                       \n" +
                "                                                       <div class=\"message\" onclick=\"$(this).slideUp('fast');\">woqunide</div></div>";
        Element element = Jsoup.parse(body).body();
        Elements message = element.getElementsByClass("message");
        String errorCode = message.text();


        assertEquals("hehe", errorCode, "woqunide");

    }

}