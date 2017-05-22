package im.fdx.v2ex.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.elvishew.xlog.XLog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import java.io.IOException;
import java.util.List;

import im.fdx.v2ex.R;
import im.fdx.v2ex.ui.WebViewActivity;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.utils.ContentUtils;
import im.fdx.v2ex.utils.ViewUtil;


/**
 * Created by fdx on 2016/9/11.
 * fdx will maintain it
 */

public class GoodTextView extends android.support.v7.widget.AppCompatTextView {

    public static final int REQUEST_CODE = 200;
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

    @Override
    protected void onDetachedFromWindow() {


        super.onDetachedFromWindow();
    }


    public void setGoodText(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        setLinkTextColor(ContextCompat.getColor(context, R.color.primary));

        String formContent = ContentUtils.format(text);
        final Spanned spannedText;
        MyImageGetter imageGetter = new MyImageGetter();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            spannedText = Html.fromHtml(formContent, Html.FROM_HTML_MODE_LEGACY, imageGetter, null);
        } else {
            spannedText = Html.fromHtml(formContent, imageGetter, null);
        }


        SpannableStringBuilder htmlSpannable = new SpannableStringBuilder(spannedText);
//
        //分离 图片 span
        ImageSpan[] imageSpans = htmlSpannable.getSpans(0, htmlSpannable.length(), ImageSpan.class);


        URLSpan[] urlSpans = htmlSpannable.getSpans(0, htmlSpannable.length(), URLSpan.class);


        for (URLSpan urlSpan : urlSpans) {

            int spanStart = htmlSpannable.getSpanStart(urlSpan);
            int spanEnd = htmlSpannable.getSpanEnd(urlSpan);
            URLSpan newUrlSpan = new UrlSpanNoUnderline(urlSpan.getURL());

            htmlSpannable.removeSpan(urlSpan);
            htmlSpannable.setSpan(newUrlSpan, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        }


        for (final ImageSpan imageSpan : imageSpans) {

            final String imageUrl = imageSpan.getSource();
            final int start = htmlSpannable.getSpanStart(imageSpan);
            final int end = htmlSpannable.getSpanEnd(imageSpan);

            //生成自定义可点击的span
            ClickableSpan newClickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
                    context.startActivity(intent);
                }
            };

            //替换原来的ImageSpan
            ClickableSpan[] clickableSpans = htmlSpannable.getSpans(start, end, ClickableSpan.class);

            if (clickableSpans != null && clickableSpans.length != 0) {
                for (ClickableSpan span2 :
                        clickableSpans) {
                    htmlSpannable.removeSpan(span2);
                }
            }
            htmlSpannable.setSpan(newClickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        setText(htmlSpannable);
        //不设置这一句，点击图片会跑动。
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    private class BitmapHolder extends BitmapDrawable {

        protected Drawable drawable;


        @Override
        public void draw(Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }


    private class MyImageGetter implements Html.ImageGetter {

        @Override
        public Drawable getDrawable(String source) {


            //怪不得一样的图片。放在了类里。
            final BitmapHolder bitmapHolder = new BitmapHolder();
            Log.i(TAG, " Image url: " + source);

            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                    XLog.tag(TAG).d("bp.getByteCount() " + bitmap.getByteCount()
                            + "\nbp.getAllocationByteCount() = " + bitmap.getAllocationByteCount()
                            + "\nbp.getWidth() = " + bitmap.getWidth()
                            + "\nbp.getHeight() =" + bitmap.getHeight()
                            + "\nbp.getDensity() = " + bitmap.getDensity()
                            + "\ngetWidth() = " + GoodTextView.this.getWidth()
                            + "\ngetHeight() = " + GoodTextView.this.getHeight()
                            + "\ngetMeasuredWidth()" + getMeasuredWidth()
                            + "\ngetMeasuredHeight()" + getMeasuredHeight()
                    );


                    int targetWidth = bitmap.getWidth();
                    ;
                    int targetHeight = bitmap.getHeight();

                    int minWidth = ViewUtil.dp2px(12);
                    if (bitmap.getWidth() > targetWidth) {
                        targetWidth = ViewUtil.getScreenSize()[1] - ViewUtil.dp2px(36);
                        targetHeight = (int) (targetWidth * (double) bitmap.getHeight() / (double) bitmap.getWidth());
                    } else if (bitmap.getWidth() < minWidth) {
                        targetWidth = minWidth;
                        targetHeight = (int) (targetWidth * (double) bitmap.getHeight() / (double) bitmap.getWidth());
                    }

                    Bitmap result = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true); //压缩
                    Drawable drawable = new BitmapDrawable(getContext().getResources(), result);
                    drawable.setBounds(0, 0, result.getWidth(), result.getHeight());
                    bitmapHolder.setBounds(0, 0, result.getWidth(), result.getHeight());

                    bitmapHolder.setDrawable(drawable);
                    setText(getText());
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };

            Picasso.with(getContext()).load(source).into(target);
            return bitmapHolder;
        }
    }


    @SuppressLint("ParcelCreator")
    public class UrlSpanNoUnderline extends URLSpan {
        public UrlSpanNoUnderline(URLSpan src) {
            super(src.getURL());
        }

        public UrlSpanNoUnderline(String url) {
            super(url);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }

        @Override
        public void onClick(View widget) {
            CustomChrome.getInstance(context).load(getURL());
        }

    }


    /**
     * 缩放图片
     */
    private class ImageTransform implements Transformation {

        private String key = "ImageTransform";

        @Override
        public Bitmap transform(Bitmap source) {
            int targetWidth = ViewUtil.getScreenSize()[1] - ViewUtil.dp2px(36);
            Log.i(TAG, targetWidth + "targetWidth");
            if (source.getWidth() == 0) {
                return source;
            }
            //如果图片大于设置的宽度，做处理
            if (source.getWidth() > targetWidth) {
                int targetHeight = (int) (targetWidth * (double) source.getHeight() / (double) source.getWidth());

                if (targetHeight != 0 && targetWidth != 0) {
                    Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true); //true会压缩
                    if (result != source) {
                        // Same bitmap is returned if sizes are the same
                        source.recycle();
                    }
                    return result;
                } else {
                    return source;
                }
            } else {
                return source;
            }
        }

        @Override
        public String key() {
            return key;
        }
    }
}
