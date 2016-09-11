package im.fdx.v2ex.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
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

import org.xml.sax.XMLReader;

import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.utils.ContentUtils;

import static android.R.attr.bitmap;
import static android.R.attr.width;
import static android.text.Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH;
import static im.fdx.v2ex.R.id.container;


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

        final Spanned spannedText;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            spannedText = Html.fromHtml(formContent, FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH, new Html.ImageGetter() {

                BitmapDrawable drawable;

                @Override
                public Drawable getDrawable(String source) {
                    Log.i(TAG, "before got Image");
                    VolleyHelper.getInstance().getImageLoader().get(source, new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            Bitmap bitmap = response.getBitmap();

                            drawable = new BitmapDrawable(getResources(), bitmap);

                            Log.i(TAG, "got Image");
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }, getMaxWidth(), getMaxHeight(), ImageView.ScaleType.FIT_CENTER);
                    return drawable;
                }
            }, new Html.TagHandler() { //这个是当遇到无法默认处理的tag时，自己去处理，目前我不需要。
                @Override
                public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {

                }
            });
        } else {
            spannedText = Html.fromHtml(formContent, new Html.ImageGetter() {

                final UrlDrawable urlDrawable   = new UrlDrawable();
                        Drawable drawable;
                        public static final int IMAGE_GOT = 1;

                        @Override
                        public Drawable getDrawable(String source) {
                            Log.i(TAG, "before got Image" + source);
                            VolleyHelper.getInstance().getImageLoader().get(source, new ImageLoader.ImageListener() {
                                        @Override
                                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

                                            if (response != null) {

                                                Bitmap bp = response.getBitmap();
                                                if (bp != null) {
                                                    int height = bp.getHeight();
                                                    int width = bp.getWidth();
                                                    Drawable dr = new BitmapDrawable(getResources(), bp);
                                                    //很关键，然而我一无所知
                                                    dr.setBounds(0, 0, width, height);
                                                    urlDrawable.setBounds(0, 0, bp.getWidth(),
                                                            bp.getHeight());
                                                    urlDrawable.drawable = dr;
                                                    invalidate();
                                                    Log.i(TAG, "got Image");

                                                }
                                            }
                                        }

                                        @Override
                                        public void onErrorResponse(VolleyError error) {

                                        }
                                    }, getWidth(), getHeight(), ImageView.ScaleType.FIT_CENTER);

                            Log.i(TAG, "before return");
                            return urlDrawable;
                        }
                    }

                    , new Html.TagHandler()

                    { //这个是当遇到无法默认处理的tag时，自己去处理，目前我不需要。
                        @Override
                        public void handleTag(boolean opening, String tag, Editable output, XMLReader
                                xmlReader) {

                        }
                    }

            );
        }


        SpannableStringBuilder htmlSpannable = new SpannableStringBuilder(spannedText);
//        SpannableStringBuilder htmlSpannable = (SpannableStringBuilder)spannedText;
//
        //这一步没干什么事情？
        ImageSpan[] spans = htmlSpannable.getSpans(0, htmlSpannable.length(), ImageSpan.class);


        final List<String> imageUrls = new ArrayList<>();
        List<String> imagePositions = new ArrayList<>();
        for (ImageSpan span : spans) {
            String imageUrl = span.getSource();
            int start = htmlSpannable.getSpanStart(span);
            int end = htmlSpannable.getSpanEnd(span);

            imagePositions.add(start + "/" + end);
            imageUrls.add(imageUrl);

        }

        for (ImageSpan span : spans) {
            int start = htmlSpannable.getSpanStart(span);
            int end = htmlSpannable.getSpanEnd(span);

//            可点击的图片
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrls.get(0))));
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
        super.setText(spannedText);

        //不设置这一句，点击图片会跑动。
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    public class UrlDrawable extends BitmapDrawable {
        protected Drawable drawable;
        @Override
        public void draw(Canvas canvas) {
            // override the draw to facilitate refresh function later
            //fdx 很关键，不然无法刷新视图，不管是invalidate或postvaludate，或setText。都无法显示图片
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }
    }
}
