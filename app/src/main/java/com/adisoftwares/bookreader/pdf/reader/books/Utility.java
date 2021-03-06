package com.adisoftwares.bookreader.pdf.reader.books;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

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

    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if(grantResults.length < 1){
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

    public static long getCurrentTime(){
        return System.currentTimeMillis();
    }

    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return toolbarHeight;
    }

    public static int getScreenWidth(Context mContext){
        int width=0;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if(Build.VERSION.SDK_INT>12){
            Point size = new Point();
            display.getSize(size);
            width = size.x;
        }
        else{
            width = display.getWidth();  // Deprecated
        }
        return width;
    }

    public static int getScreenHeight(Context mContext){
        int height=0;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if(Build.VERSION.SDK_INT>12){
            Point size = new Point();
            display.getSize(size);
            height = size.y;
        }
        else{
            height = display.getHeight();  // Deprecated
        }
        return height;
    }

    public static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    public static Bitmap getBitmapFromVector(VectorDrawableCompat vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }
}
