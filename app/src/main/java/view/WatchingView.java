package view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;

import controller.Controller;
import schule.adrian.montagsmaler.R;

/**
 * Created by Adrian on 24.09.15.
 */
public class WatchingView extends View {

    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF000000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    private float brushSize;

    public WatchingView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing(){

        brushSize = (float) (Controller.getInstance().getUser().getScreenWidth() * 0.01);

        //get drawing area setup for interaction
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //view given size
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //draw view
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    public void paintPicture(float x, float y, int event, String color) {

        setColor("#" + color);

        switch (event) {
            case 0:
                drawPath.moveTo(x, y);
                break;

            case 1:
                drawPath.lineTo(x, y);
                break;

            case 2:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;

            case 3:
                deletePainting();
                break;

            default:
        }

        invalidate();
    }

    public void setColor(String newColor){
        //set color
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    public void deletePainting() {
        drawCanvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
        invalidate();
    }
}