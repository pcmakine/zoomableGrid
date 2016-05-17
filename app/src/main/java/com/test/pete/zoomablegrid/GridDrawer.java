package com.test.pete.zoomablegrid;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Pete on 17.5.2016.
 */
public class GridDrawer implements ZoomViewDrawer {
    private int cellsX = 30;
    private int cellsY = 30;
    private Paint paint;
    private Paint linePaint;

    public GridDrawer(){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xff0066ff);
        paint.setStyle(Paint.Style.FILL);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0xff0066ff);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(8);
    }

    @Override
    public void draw(Canvas canvas, ZoomViewConfigs configs) {
        canvas.drawCircle(configs.getWidth()/2, configs.getHeight()/2, 20, paint);
        float cellWidth = configs.getWidth()/cellsX ;
        for(int i = 1; i < cellsX; i++){
            canvas.drawLine(0 + i * cellWidth, 0, 0 + i * cellWidth, configs.getHeight(), linePaint);
        }

        float cellHeight = configs.getHeight()/cellsX ;
        for(int i = 1; i < cellsY; i++) {
            canvas.drawLine(0, 0 + i * cellHeight, configs.getWidth(), 0 + i * cellHeight, linePaint);
        }
    }
}
