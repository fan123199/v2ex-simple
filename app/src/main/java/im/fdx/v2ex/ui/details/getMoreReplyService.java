package im.fdx.v2ex.ui.details;

import android.app.IntentService;
import android.content.Intent;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.elvishew.xlog.XLog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

import im.fdx.v2ex.model.ReplyModel;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.JsonManager;
import okhttp3.Request;

/**
 * 为什么不用Thread？感觉Thread不高级
 */
public class getMoreReplyService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public getMoreReplyService(String name) {
        super(name);
    }

    public getMoreReplyService() {
        this("what");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        int page = intent != null ? intent.getIntExtra("page", -1) : -1;
        long topicId = intent != null ? intent.getLongExtra("topic_id", -1) : -1;

        XLog.tag("DetailsActivity").d(page + " | " + topicId);
        if (page == -1 || topicId == -1L) {
            return;
        }

        try {
            for (int i = 2; i <= page; i++) {
                okhttp3.Response response = HttpHelper.OK_CLIENT.newCall(new Request.Builder()
                        .headers(HttpHelper.baseHeaders)
                        .url(JsonManager.HTTPS_V2EX_BASE + "/t/" + topicId + "?p=" + i)
                        .build()).execute();
                Document body = Jsoup.parse(response.body().string());
                ArrayList<ReplyModel> replies = JsonManager.parseResponseToReplay(body);

                XLog.tag("DetailsActivity").d(replies.get(0).getContent());
                Intent it = new Intent();
                it.setAction("im.fdx.v2ex.reply");
                it.putParcelableArrayListExtra("replies", replies);
                LocalBroadcastManager.getInstance(this).sendBroadcast(it);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
