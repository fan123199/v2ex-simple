package im.fdx.v2ex.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.utils.ContentUtils;


/**
 * Created by fdx on 2016/9/11.
 * fdx will maintain it
 */

public class GoodTextView extends TextView {

    private Context context;
    private static final String TAG = GoodTextView.class.getSimpleName();


    public GoodTextView(Context context) {
        super(context);
        this.context = context;
    }

    public GoodTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public GoodTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public GoodTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }


    public void setGoodText(String text) {

        String formContent = ContentUtils.formatContent(text);
        final Spanned spannedText = Html.fromHtml(formContent, new MyImageGetter(), null);


        SpannableStringBuilder htmlSpannable = new SpannableStringBuilder(spannedText);
//        SpannableStringBuilder htmlSpannable = (SpannableStringBuilder)spannedText;

//
        //这一步没干什么事情？
        ImageSpan[] spans = htmlSpannable.getSpans(0, htmlSpannable.length(), ImageSpan.class);


        final List<String> imageUrls = new ArrayList<>();
        List<String> imagePositions = new ArrayList<>();
        for (final ImageSpan span : spans) {
            String imageUrl = span.getSource();
            int start = htmlSpannable.getSpanStart(span);
            int end = htmlSpannable.getSpanEnd(span);

            imagePositions.add(start + "/" + end);
            imageUrls.add(imageUrl);


            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(span.getSource())));
                }
            };

            ClickableSpan[] clickableSpans = htmlSpannable.getSpans(start, end, ClickableSpan.class);
            if (clickableSpans != null && clickableSpans.length != 0) {
                for (ClickableSpan span2 :
                        clickableSpans) {
                    htmlSpannable.removeSpan(span2);
                }
                htmlSpannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

        }
        setText(spannedText);


        //不设置这一句，点击图片会跑动。
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    private static class BitmapHolder extends BitmapDrawable {

        protected Drawable drawable;


        @Override
        public void draw(final Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

    }


    private class MyImageGetter implements Html.ImageGetter {

        final BitmapHolder bitmapHolder = new BitmapHolder();

        @Override
        public Drawable getDrawable(String source) {
            Log.i(TAG, "before got Image" + source);
            ImageLoader.ImageContainer response = VolleyHelper.getInstance().getImageLoader().get(source, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (response != null) {

                        Bitmap bp = response.getBitmap();
                        if (bp != null) {
                            int height = bp.getHeight();
                            int width = bp.getWidth();
                            Drawable dr = new BitmapDrawable(getResources(), bp);
                            dr.setBounds(0, 0, width, height);
                            bitmapHolder.setDrawable(dr);
                            bitmapHolder.setBounds(0, 0, width, height);

                            //很关键，然而我一无所知,必须先invalidate，然后setText
                            invalidate();
                            setText(getText());
//                                        postInvalidate();
//                                    drawable.invalidateSelf();

                            Log.i(TAG, "got Image");
                        }
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }, getWidth(), getHeight(), ImageView.ScaleType.FIT_CENTER);


            Log.i(TAG, "before return");
            return bitmapHolder;
        }
    }
}
