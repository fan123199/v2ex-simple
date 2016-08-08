package im.fdx.v2ex.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by a708 on 15-8-28.
 * RecyclerView 的 按钮监听类，不用OnclickListener，而是用OnItemTouchListener
 */
public class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

    private GestureDetector mGestureDetector;
    private EasyClickListener mListener;

    public RecyclerTouchListener(Context context, final RecyclerView recyclerView, EasyClickListener myClickListener) {

        mListener = myClickListener;

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
//                L.m("single tap up");
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {

                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null && mListener != null) {
                    mListener.onLongClick(childView, recyclerView.getChildAdapterPosition(childView));
//                    L.m("on Long Press");
                }
            }
        });
    }

    //onInterceptTouchEvent默认值是false这样才能把事件传给View里的onTouchEvent.
    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View childView = rv.findChildViewUnder(e.getX(), e.getY());

        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onClick(childView, rv.getChildAdapterPosition(childView));
            L.m("why do onIntercept");
        }
        return false; //默认返回true
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {


    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
