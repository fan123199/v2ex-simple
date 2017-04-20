package im.fdx.v2ex.utils;

import android.view.View;

/**
 * Created by fdx on 15-8-28.
 * 点击事件接口
 */
public interface EasyClickListener {
    void onClick(View view, int position);

    void onLongClick(View view, int position);

}
