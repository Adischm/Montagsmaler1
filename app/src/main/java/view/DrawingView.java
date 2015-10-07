package view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import controller.Controller;
import Data.Data;


public class DrawingView extends View {

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

    private int event_dif = 0;
    private int event_color = 0;
    private int drawID = 0;

    private String colorString = "FF000000";

    private ArrayList<String[]> drawPoints;

    //private HttpClient httpclient;

    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        //httpclient = new DefaultHttpClient();

        this.drawPoints = new ArrayList<String[]>();

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //detect user touch
        float touchX = event.getX();
        float touchY = event.getY();

        event_color = paintColor;

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

    /*public void postXY(float x, float y, int event_dif, int drawID) {

        x = x/Controller.getInstance().getUser().getScreenWidth();
        y = y/Controller.getInstance().getUser().getScreenWidth();

        //Execute-String
        String urlDrawPoints = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method="
                                + "setDrawPoint&x=" + x + "&y=" + y + "&event=" + event_dif + "&id=" + drawID
                                + "&lobbyId=" + Controller.getInstance().getGame().getLobbyId()
                                + "&color=" + colorString;

        new DrawPointsTask().execute(urlDrawPoints);
    }*/

    public void postXY(float x, float y, int event_dif, int drawID) {

        x = x/Controller.getInstance().getUser().getScreenWidth();
        y = y/Controller.getInstance().getUser().getScreenWidth();

//        String s = "(" + String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(event_dif) + "," + String.valueOf(drawID) + ",'"
//                + Controller.getInstance().getGame().getLobbyId() + "','" + colorString + "')";

        String[] s = {"[X]=" + String.valueOf(x), "[Y]=" + String.valueOf(y), "[Event]=" + String.valueOf(event_dif), "[Id]=" + String.valueOf(drawID),
                "[LobbyId]=" + Controller.getInstance().getGame().getLobbyId(),"[Color]=" + colorString};

        drawPoints.add(s);

        if (drawPoints.size() == 10 || event_dif > 1) {

            createValues(drawPoints);
            drawPoints.clear();
        }
    }

    public void createValues(ArrayList<String[]> al) {

        String values = "";

        for (int i = 0; i < al.size(); i++) {

            for (int j = 0; j < al.get(i).length; j++) {

                values = values + "Values[" + i + "]" + al.get(i)[j] + "&";
            }
        }

        values = values.substring(0, (values.length()-1));

        Log.i("FU", "Statement: " + values);

        new DrawPointsTask().execute(values);


    }

    public void setColor(String newColor){

        colorString = newColor.substring(1);

        //set color
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    /**
     *
     *//*
    private class DrawPointsTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            HttpClient httpclient = new DefaultHttpClient();

            //Führt die GetFunktion aus
            try {
                httpclient.execute(new HttpGet(strings[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }*/

    /**
     *
     */
    private class DrawPointsTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            HttpClient httpclient = new DefaultHttpClient();

            //Execute-String
            String urlPoints = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json"
                    + "&method=TestPoints&" + strings[0];

            Log.i("FU", "URL: " + urlPoints);

            //Führt die GetFunktion aus
            try {
                httpclient.execute(new HttpGet(urlPoints));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //httpclient.getConnectionManager().shutdown();

            return null;
        }
    }

    public int getDrawID() {
        return drawID;
    }

    public void setDrawID(int drawID) {
        this.drawID = drawID;
    }
}
