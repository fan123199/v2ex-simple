package im.fdx.v2ex.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Test;

/**
 * Created by fdx on 2017/3/7.
 * fdx will maintain it
 */

public class SomeSmallTest {

    @Test
    public void parse() throws Exception {

        String input = "<div class=\"topic_content\">之前服务器端程序员兼职用 flask+bootstrap 撸了一个后台，游戏上线后开发任务加重，所以我们计划寻找一个 web 前端加入我们这个小团队来专职做这事。有过 bootstrap 项目经验的是最受欢迎的，如果 html5 有点造诣就更好了，以后我们有可能会捣腾点。\n" +
                "<br>有兴趣了解我们或者愿意我们了解您的朋友，请发简历到 <a target=\"_blank\" href=\"mailto:chegululu@gmail.com\">chegululu@gmail.com</a> ，或者 qq ： 48160780</div>";

        String head = "<head><meta property=\"article:published_time\" content=\"2017-03-07T01:45:30Z\"> </head>";

        Element body = Jsoup.parse(head);
        String content = body.getElementsByClass("topic_content").text();
        String content2 = body.getElementsByClass("topic_content").html();

        String content3 = body.getElementsByClass("topic_content").outerHtml();

        String headder = body.getElementsByAttributeValue("property", "article:published_time").attr("content");
    }
}
