package im.fdx.v2ex.utils;

import android.view.View;

/**
 * Created by a708 on 15-8-28.
 * 点击事件接口
 */
public interface myClickListener {
    void onClick(View view, int position);

    void onLongClick(View view, int position);

}
