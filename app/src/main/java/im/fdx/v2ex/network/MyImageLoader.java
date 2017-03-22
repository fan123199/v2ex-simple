package im.fdx.v2ex.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import static com.squareup.picasso.Picasso.with;

/**
 * Created by fdx on 2017/3/15.
 * fdx will maintain it
 */

public class MyImageLoader {

    public static void load(Context context, String url, ImageView imageView) {
        Picasso.with(context).load("http://i.imgur.com/DvpvklR.png").into(imageView);
    }

    public static Bitmap load(Context context, String url) {
        Bitmap bitmap = null;
        try {
            bitmap = Picasso.with(context).load(url).resize(300, 300).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;

    }

}
