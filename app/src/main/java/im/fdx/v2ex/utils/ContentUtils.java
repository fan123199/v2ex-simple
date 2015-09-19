package im.fdx.v2ex.utils;

/**
 * Created by a708 on 15-8-18.
 * 来自yaoyumeng的开源
 */

public class ContentUtils {

    public static String formatContent(String content){
        return content.replace("href=\"/member/", "href=\"http://www.v2ex.com/member/")
                .replace("href=\"/i/", "href=\"https://i.v2ex.co/")
                .replace("href=\"/t/", "href=\"http://www.v2ex.com/t/")
                .replace("href=\"/go/", "href=\"http://www.v2ex.com/go/");
    }
}
