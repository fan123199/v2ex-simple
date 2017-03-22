package im.fdx.v2ex.view;

import android.annotation.TargetApi;
import android.app.Activity;
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
import android.os.Build;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
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
import com.elvishew.xlog.XLog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.fdx.v2ex.R;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.VolleyHelper;
import im.fdx.v2ex.utils.ContentUtils;


/**
 * Created by fdx on 2016/9/11.
 * fdx will maintain it
 */

public class GoodTextView extends android.support.v7.widget.AppCompatTextView {

    private Context context;
    private static final String TAG = GoodTextView.class.getSimpleName();
    private MyImageGetter imageGetter;


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

    public void recycleImage() {

    }


    public void setGoodText(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

//        XLog.tag(TAG).d( "early in setGoodTest" + getWidth());
        String formContent = ContentUtils.format(text);
        final Spanned spannedText;
        imageGetter = new MyImageGetter();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            spannedText = Html.fromHtml(formContent, Html.FROM_HTML_MODE_LEGACY, imageGetter, null);
        } else {
            spannedText = Html.fromHtml(formContent, imageGetter, null);
        }


        SpannableStringBuilder htmlSpannable = new SpannableStringBuilder(spannedText);
//
        //分离 图片 span
        ImageSpan[] imageSpans = htmlSpannable.getSpans(0, htmlSpannable.length(), ImageSpan.class);


//        final List<String> imageUrls = new ArrayList<>();
//        List<String> imagePositions = new ArrayList<>();
        for (final ImageSpan imageSpan : imageSpans) {

            final String imageUrl = imageSpan.getSource();
            final int start = htmlSpannable.getSpanStart(imageSpan);
            final int end = htmlSpannable.getSpanEnd(imageSpan);

//            XLog.tag("GoodTextView-fdx").d(imageSpan.getSource());
//            XLog.tag("GoodTextView-fdx").d(start + "|" + end);

//            imageUrls.add(imageUrl);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
                    context.startActivity(intent);
                }
            };

            ClickableSpan[] clickableSpans = htmlSpannable.getSpans(start, end, ClickableSpan.class);
            if (clickableSpans != null && clickableSpans.length != 0) {
                for (ClickableSpan span2 :
                        clickableSpans) {
                    htmlSpannable.removeSpan(span2);
                }
            }
            htmlSpannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        public Drawable getDrawable(final String source) {
            //怪不得一样的图片。放在了类里。
            final BitmapHolder bitmapHolder = new BitmapHolder();

            final BitmapDrawable bitmapDrawable = new BitmapDrawable();
            Log.i(TAG, " Image url: " + source);
            ImageLoader.ImageContainer response = VolleyHelper.getInstance().getImageLoader()
                    .get(source, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (response != null) {


                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;


                        Bitmap bp = response.getBitmap();


                        if (bp != null) {

                            XLog.tag(TAG).d("bp.getByteCount() " + bp.getByteCount()
                                    + "\nbp.getAllocationByteCount() = " + bp.getAllocationByteCount()
                                    + "\nbp.getWidth() = " + bp.getWidth()
                                    + "\nbp.getHeight() =" + bp.getHeight()
                                    + "\nbp.getDensity() = " + bp.getDensity()
                                    + "\ngetWidth() = " + GoodTextView.this.getWidth()
                                    + "\ngetHeight() = " + GoodTextView.this.getHeight()
                                    + "\ngetMeasuredWidth()" + getMeasuredWidth()
                                    + "\ngetMeasuredHeight()" + getMeasuredHeight()
                            );

                            Drawable dr = new BitmapDrawable(getResources(), bp);

                            Point point = new Point();
                            int theMaxWitheasy = 500;
                            ((Activity) context).getWindowManager().getDefaultDisplay().getSize(point);
                            int theMaxWith = point.y - 100;

                            int bpheight;
                            int bpwidth;

                            if (bp.getWidth() > theMaxWith) {
                                bpwidth = theMaxWith;
                                bpheight = (int) (bp.getHeight() * ((float) theMaxWith / bp.getWidth()));
                            } else {
                                bpheight = bp.getHeight();
                                bpwidth = bp.getWidth();
                            }

                            dr.setBounds(0, 0, bpwidth, bpheight);
                            bitmapHolder.setDrawable(dr);
                            bitmapHolder.setBounds(0, 0, bpwidth, bpheight);

                            //很关键，然而我一无所知,必须setText
                            setText(getText());
                            Log.i(TAG, "got Image");
                        }
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }, getWidth(), getHeight(), ImageView.ScaleType.FIT_CENTER);

            return bitmapHolder;
        }
    }

    private class NewGetter implements Html.ImageGetter {
        @Override
        public Drawable getDrawable(String source) {
            return null;
        }
    }

    private class task extends AsyncTask<GoodTextView, Void, Bitmap> {

        String source;
        Context context;

        public task(Context context, String source) {
            this.context = context;
            this.source = source;
        }

        @Override
        protected Bitmap doInBackground(GoodTextView... params) {

            try {
                return Picasso.with(this.context).load(source).get();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
        }
    }

}
