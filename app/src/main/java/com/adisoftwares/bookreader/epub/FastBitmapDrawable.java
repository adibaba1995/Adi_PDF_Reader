package com.adisoftwares.bookreader.epub;

import android.graphics.*;
import android.graphics.drawable.Drawable;

public class FastBitmapDrawable extends Drawable {

    private Bitmap mBitmap;

    private int width;
    private int height;

    public FastBitmapDrawable(Bitmap b) {
        mBitmap = b;

        if ( b != null ) {
            this.width = b.getWidth();
            this.height = b.getHeight();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if ( mBitmap != null ) {
            canvas.drawBitmap(mBitmap, 0.0f, 0.0f, null);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getIntrinsicWidth() {
        return width;
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }

    @Override
    public int getMinimumWidth() {
        return width;
    }

    @Override
    public int getMinimumHeight() {
        return height;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void destroy() {
        if ( this.mBitmap != null ) {
            this.mBitmap.recycle();
        }

        this.mBitmap = null;
        this.setCallback(null);
    }
}
