package com.test.pete.zoomablegrid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * Created by Pete on 15.5.2016.
 * http://vivin.net/2011/12/04/implementing-pinch-zoom-and-pandrag-in-an-android-view-on-the-canvas/2/
 */
public class ZoomableView extends View {
    public static final String TAG = ZoomableView.class.getSimpleName();
    public static float MAX_SCALE = 5f;
    public static float MIN_SCALE = 0.8f;

    private Paint paint;
    private Paint linePaint;
    private Matrix matrix;
    private float scale = 1;
    private ScaleGestureDetector gestureDetector;
    private int cellsX = 10;
    private int cellsY = 10;
    private float xpad;
    private float ypad;
    private Mode mode;
    private float startX = 0f;
    private float startY = 0f;
    private float translateX = 0f;
    private float translateY = 0f;
    private float previousTranslateX = 0f;
    private float previousTranslateY = 0f;
    private ZoomViewDrawer drawer;

    public ZoomableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xff0066ff);
        paint.setStyle(Paint.Style.FILL);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0xff0066ff);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(8);

        matrix = new Matrix();
        gestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        drawer = new GridDrawer();
    }

    public void setScale(float scale){
        scale = Math.max(MIN_SCALE, Math.min(scale, MAX_SCALE));
        matrix.setScale(scale, scale, getWidth()/2, getHeight()/2);
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvas.save();
        canvas.scale(scale, scale); //, getWidth()/2, getHeight()/2);
        Log.d(TAG, "translatex: " + translateX);
        //If translateX times -1 is lesser than zero, let's set it to zero. This takes care of the left bound
        if((translateX * -1) < 0) {
            translateX = 0;
        }

        //This is where we take care of the right bound. We compare translateX times -1 to (scaleFactor - 1) * displayWidth.
        //If translateX is greater than that value, then we know that we've gone over the bound. So we set the value of
        //translateX to (1 - scaleFactor) times the display width. Notice that the terms are interchanged; it's the same
        //as doing -1 * (scaleFactor - 1) * displayWidth
        else if((translateX * -1) > (scale - 1) * getWidth()) {
            translateX = (1 - scale) * getWidth();
        }

        if(translateY * -1 < 0) {
            translateY = 0;
        }

        //We do the exact same thing for the bottom bound, except in this case we use the height of the display
        else if((translateY * -1) > (scale - 1) * getHeight()) {
            translateY = (1 - scale) * getHeight();
        }
        if(scale > 1){
            canvas.translate(translateX/scale, translateY/scale);
        }
        ZoomViewConfigs configs = new ZoomViewConfigs();
        configs.setHeight(this.getHeight());
        configs.setWidth(this.getWidth());
        drawer.draw(canvas, configs);


        /*
        matrix.postTranslate(translateX/scale, translateY/scale);
        canvas.concat(matrix);
        canvas.drawCircle(this.getWidth()/2, this.getHeight()/2, 20, paint);
        float cellWidth = getWidth()/cellsX ;
        for(int i = 1; i < cellsX; i++){
            canvas.drawLine(0 + i * cellWidth, 0, 0 + i * cellWidth, getHeight(), linePaint);
        }

        float cellHeight = getHeight()/cellsX ;
        for(int i = 1; i < cellsY; i++){
            canvas.drawLine(0, 0 + i * cellHeight, getWidth(), 0 + i * cellHeight, linePaint);
        }*/
        canvas.restore();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //This is the basic skeleton for our code. We examine each of the possible motion-events that can happen
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                //This event happens when the first finger is pressed onto the screen
                this.mode = Mode.DRAG;
                startX = event.getX() - previousTranslateX;
                startY = event.getY() - previousTranslateY;
                Log.d(TAG, "ONTOUCH, startx: " + startX);
                Log.d(TAG, "ONTOUCH, eventx: " + event.getX());
                break;

            case MotionEvent.ACTION_MOVE:
                //This event fires when the finger moves across the screen, although in practice I've noticed that
                //this fires even when you're simply holding the finger on the screen.
                //We don't need to set the mode at this point because the mode is already set to DRAG
                translateX = event.getX() - startX;
                translateY = event.getY() - startY;
              /*  if(translateX < 0){
                    translateX = 0;
                }
                if(translateY < 0){
                    translateY = 0;
                }*/
                Log.d(TAG, "MOVE");
                Log.d(TAG, "ONTOUCH, startx: " + startX);
                Log.d(TAG, "ONTOUCH, eventx: " + event.getX());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //This event fires when a second finger is pressed onto the screen
                this.mode = Mode.ZOOM;
                break;

            case MotionEvent.ACTION_UP:
                //This event fires when all fingers are off the screen
                this.mode = Mode.NONE;
                previousTranslateX = translateX;
                previousTranslateY = translateY;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                //This event fires when the second finger is off the screen, but the first finger is still on the
                //screen
                this.mode = Mode.DRAG;
                previousTranslateX = translateX;
                previousTranslateY = translateY;
                break;
        }

        gestureDetector.onTouchEvent(event);
        //The only time we want to re-draw the canvas is if we are panning (which happens when the mode is
        //DRAG and the zoom factor is not equal to 1) or if we're zooming
        if ((mode == Mode.DRAG /*&& scale != 1f */ ) ||mode == Mode.ZOOM) {
            invalidate();
        }
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
            ZoomableView.this.setScale(scale);

          /*    matrix.setScale(scale, scale, ZoomableGrid.this.getWidth()/2, ZoomableGrid.this.getHeight()/2);
            RectF drawableRect = new RectF(0, 0, ZoomableGrid.this.getWidth()*scale, ZoomableGrid.this.getWidth()*scale);
            RectF viewRect = new RectF(0, 0,  ZoomableGrid.this.getWidth(), ZoomableGrid.this.getWidth());
            matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);*/
            return true;
        }
    }
}
