package im.fdx.v2ex.network;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by fdx on 2016/11/20.
 * fdx will maintain it
 */

public class OkHttpHelper {

    public final OkHttpClient okHttpClient  = new OkHttpClient();


    public void doSomething() throws IOException {


            Request request = new Request.Builder()
                    .url(JsonManager.SIGN_IN_URL)
                    .build();

        Response response = okHttpClient.newCall(request).execute();
        String c = JsonManager.getOnceCode(response.body().string());
        System.out.print( c);
    }
}
