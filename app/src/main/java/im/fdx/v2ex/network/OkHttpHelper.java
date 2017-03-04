package im.fdx.v2ex.network;

import java.io.IOException;

import im.fdx.v2ex.utils.HintUI;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static im.fdx.v2ex.network.JsonManager.SIGN_IN_URL;

/**
 * Created by fdx on 2016/11/20.
 * fdx will maintain it
 */

public class OkHttpHelper {

    public final OkHttpClient okHttpClient  = new OkHttpClient();


    public int doSomething() throws IOException {


        Request request = new Request.Builder()
                .url(SIGN_IN_URL)
                .build();

        Response response = okHttpClient.newCall(request).execute();

        Request requestPush = new Request.Builder()
                .addHeader("Origin", JsonManager.HTTPS_V2EX_BASE)
                .addHeader("Referer", SIGN_IN_URL)
                .addHeader("Content-Type", "application/x-www-form-urlencoded").build();

        return 0;
    }
}
