package com.adisoftwares.bookreader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by adityathanekar on 14/01/16.
 */
public class Utility {
    public static Bitmap drawCircleAroundIcon(Context context, int resourceid, int circleColor, int paddingInDp, int strokewidth) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceid);

        int padding = (int)convertDpToPixel((float)paddingInDp, context);
        // Since the Paint is going to draw a noticeably thick line, the thickness must be included in the calculations
        int strokeWidth = (int)convertDpToPixel((float)strokewidth, context);
        /*
         * Calculating single dimension since the bitmap must have a square shape for the circle to fit.
         * Also account for the padding and the stroke width;
         */
        int bitmapSize = Math.max(bitmap.getWidth(), bitmap.getHeight()) + padding + strokeWidth;
        Bitmap workingBitmap = Bitmap.createBitmap(bitmapSize, bitmapSize,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(workingBitmap);
        // ^^^^ Added ^^^^
        //
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(circleColor);
        paint.setStyle(Paint.Style.STROKE);
        //
        // paint.setStrokeWidth(6);
        paint.setStrokeWidth(strokeWidth);
        //
        // canvas.drawCircle(centerCoordinate, centerCoordinate,
        //         centerCoordinate+15, paint);
        /*
         * Calculate exact top left position in the result Bitmap to draw the original Bitmap
         */
        canvas.drawBitmap(bitmap, (bitmapSize - bitmap.getWidth()) / 2.0f,
                (bitmapSize - bitmap.getHeight()) / 2.0f, paint);
        //
        // int centerCoordinate = mutableBitmap.getWidth()/2;
        int centerCoordinate = bitmapSize / 2;
        //
        //canvas.drawCircle(centerCoordinate, centerCoordinate,
        // centerCoordinate+15, paint);
        /*
         * Draw the circle but account for the stroke width of the paint or else the circle will flatten on the edges of the Bitmap.
         */
        canvas.drawCircle(centerCoordinate, centerCoordinate,
                centerCoordinate - (strokeWidth/2.0f), paint);
        // equivalent to imageView.setImageBitmap
        // views.setImageViewBitmap(R.id.icon, mutableBitmap);

        return workingBitmap;

    }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    public static Bitmap createBitmapFromView(View view) {
        //Pre-measure the view so that height and width don't remain null.
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        //Assign a size and position to the view and all of its descendants
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        //Create the bitmap
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        //Create a canvas with the specified bitmap to draw into
        Canvas c = new Canvas(bitmap);

        //Render this view (and all of its children) to the given Canvas
        view.draw(c);
        return bitmap;
    }

    public static String getFileNameFromUrl(String urlString) {
        return urlString.substring(urlString.lastIndexOf('/') + 1).split("\\?")[0].split("#")[0];
    }

    public static Drawable drawTextOnCanvas(Context context, int width, int height, String text, Typeface typeface, int color) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        // maybe color the bacground..

        // Setup a textview like you normally would with your activity context
        TextView tv = new TextView(context);
        tv.setPadding((int)Utility.convertDpToPixel(8.0f, context), 0, (int)Utility.convertDpToPixel(8.0f, context), 0);
        tv.setWidth(width);
        tv.setHeight(height);
        tv.setTypeface(typeface);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        tv.setGravity(Gravity.CENTER);
        // setup text
        tv.setText(text);

        // maybe set textcolor
        tv.setTextColor(context.getResources().getColor(color));
        //tv.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));

        // you have to enable setDrawingCacheEnabled, or the getDrawingCache will return null
        tv.setDrawingCacheEnabled(true);

        // we need to setup how big the view should be..which is exactly as big as the canvas
        tv.measure(View.MeasureSpec.makeMeasureSpec(canvas.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(canvas.getHeight(), View.MeasureSpec.EXACTLY));

        // assign the layout values to the textview
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());

        // draw the bitmap from the drawingcache to the canvas
        paint.setColor(context.getResources().getColor(android.R.color.black));
        canvas.drawRect(0, 0, width, height, paint);
        canvas.drawBitmap(tv.getDrawingCache(), 0, 0, paint);

        // disable drawing cache
        tv.setDrawingCacheEnabled(false);

        return new BitmapDrawable(context.getResources(), bitmap);
    }
}
