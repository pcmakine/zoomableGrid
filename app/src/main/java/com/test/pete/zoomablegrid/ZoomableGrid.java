package com.test.pete.zoomablegrid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Pete on 15.5.2016.
 */
public class ZoomableGrid extends View {
    public static final String TAG = ZoomableGrid.class.getSimpleName();
    public static float MAX_SCALE = 5f;
    public static float MIN_SCALE = 0.1f;

    private Paint paint;
    private Paint linePaint;
    private Matrix matrix;
    private float scale = MIN_SCALE;
    private ScaleGestureDetector gestureDetector;
    private int cellsX = 5;
    private int cellsY = 5;
    private float xpad;
    private float ypad;


    public ZoomableGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xff0066ff);
        paint.setStyle(Paint.Style.FILL);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0xff0066ff);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(10);


        matrix = new Matrix();
        gestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void setScale(float scale){
        scale = Math.max(MIN_SCALE, Math.min(scale, MAX_SCALE));
        matrix.setScale(scale, scale, getWidth()/2, getHeight()/2);
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        Log.d(TAG, "scale: " + scale);
        canvas.concat(matrix);
       // canvas.translate(getWidth()/2*scale, getHeight()/2*scale);

        canvas.drawCircle(this.getWidth()/2, this.getHeight()/2, 20, paint);
        float cellWidth = getWidth()/cellsX ;
        for(int i = 1; i < cellsX; i++){
            canvas.drawLine(0 + i * cellWidth, 0, 0 + i * cellWidth, getHeight(), linePaint);
        }

        float cellHeight = getHeight()/cellsX ;
        for(int i = 1; i < cellsY; i++){
            canvas.drawLine(0, 0 + i * cellHeight, getWidth(), 0 + i * cellHeight, linePaint);
        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        this.invalidate();
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        xpad = (float)(getPaddingLeft() + getPaddingRight());
        ypad = (float)(getPaddingTop() + getPaddingBottom());
        setScale(scale);
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Log.d(TAG, "on scale listener");
            scale *= detector.getScaleFactor();
            scale = Math.max(MIN_SCALE, Math.min(scale, MAX_SCALE));
            ZoomableGrid.this.setScale(scale);

          /*    matrix.setScale(scale, scale, ZoomableGrid.this.getWidth()/2, ZoomableGrid.this.getHeight()/2);
            RectF drawableRect = new RectF(0, 0, ZoomableGrid.this.getWidth()*scale, ZoomableGrid.this.getWidth()*scale);
            RectF viewRect = new RectF(0, 0,  ZoomableGrid.this.getWidth(), ZoomableGrid.this.getWidth());
            matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);*/
            return true;
        }
    }
}
