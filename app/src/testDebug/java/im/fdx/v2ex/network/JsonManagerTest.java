package im.fdx.v2ex.network;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by fdx on 2016/11/19.
 * fdx will maintain it
 */
public class JsonManagerTest {
    @Test
    public void getOnceCode() throws Exception {

        String tt = "hidden\" value=\"54055\" name=\"once\" /><input type=\"password\" class=\"sl\" name=\"c65809a132a1dc908fcb7c9b2d895af4af0acd530511a140a8c6e550aacd67de\" value=\"\" autocorrect=\"off\" spellcheck=\"false\" autocapitalize=\"off\" /></td>\n" +
                "                                                                    </tr>\n" +
                "                                                                </table>\n" +
                "                                                                <td width=\"auto\" align=\"left\"><input type=\"submit\" class=\"super normal button\" value=\"登录\" style=\"width: 100%; line-height: 20px; box-sizing: border-box;\" /></td>";

        System.out.print(JsonManager.getOnceCode(tt));
    }

}