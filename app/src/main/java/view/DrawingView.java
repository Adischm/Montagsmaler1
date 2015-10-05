package view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

import Data.Data;
import controller.Controller;


public class DrawingView extends View {

    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    private float brushSize;

    private int event_dif = 0;
    private int drawID = 0;

    private HttpClient httpclient;

    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        httpclient = new DefaultHttpClient();
        setupDrawing();
    }

    private void setupDrawing(){

        brushSize = (float) (Controller.getInstance().getUser().getScreenWidth() * 0.01);

        //get drawing area setup for interaction
        drawPath = new Path();
        drawPaint = new Paint();
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //detect user touch
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                event_dif = 0;
                drawID++;

                postXY(touchX, touchY, event_dif, drawID);

                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                event_dif = 1;
                drawID++;

                postXY(touchX, touchY, event_dif, drawID);

                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();

                event_dif = 2;
                drawID++;

                postXY(0, 0, event_dif, drawID);

                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    public void deletePainting() {
        drawID++;
        postXY(0, 0, 3, drawID);
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    public void postXY(float x, float y, int event_dif, int drawID) {

        x = x/Controller.getInstance().getUser().getScreenWidth();
        y = y/Controller.getInstance().getUser().getScreenWidth();

        //Execute-String
        String urlDrawPoints = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method="
                                + "setDrawPoint&x=" + x + "&y=" + y + "&event=" + event_dif + "&id=" + drawID
                                + "&lobbyId=" + Controller.getInstance().getGame().getLobbyId();

        new DrawPointsTask().execute(urlDrawPoints);
    }

    /**
     *
     */
    private class DrawPointsTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            //Führt die GetFunktion aus
            try {
                httpclient.execute(new HttpGet(strings[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
