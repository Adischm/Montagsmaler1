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

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.ArrayList;

import Data.Data;
import controller.Controller;

/**
 * Klasse für das View-Objekt (= Malfläche) der DrawActivity
 */
public class DrawingView extends View {

    //--- Anfang Attribute ---
    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = 0xFF000000;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private float brushSize;
    private int event_dif = 0;
    private int event_color = 0;
    private int drawID = 0;
    private String colorString = "FF000000";
    private ArrayList<String[]> drawPoints;

    //--- Ende Attribute ---

    /**
     * Konstruktor
     * @param context
     * @param attrs
     */
    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);

        //Instanziert die Liste für Malpunkte
        this.drawPoints = new ArrayList<String[]>();

        //Ruft die Setup-Methode auf
        setupDrawing();
    }

    /**
     * Methode zum initialen Setup der Fläche
     */
    private void setupDrawing(){

        //Die Strichbreite wird anhand der Bildschirmbreite festgelegt
        brushSize = (float) (Controller.getInstance().getUser().getScreenWidth() * 0.01);

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
    /**
     * Listener für die Touch-Eingabe
     */
    public boolean onTouchEvent(MotionEvent event) {

        //Ermittelt die Koordinaten
        float touchX = event.getX();
        float touchY = event.getY();

        //Übergibt die aktuelle Farbe
        event_color = paintColor;

        //Für die 3 Touch-Events (DOWN, MOVE und UP) werden jeweils die "Malfunktionen" ausgeführt
        //Zudem werden für jedes Event die Koordinaten, der Event-Wert und eine ID an die PostXY-Methode übergeben
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

        //Aktualisiert die Ansicht
        invalidate();
        return true;
    }

    /**
     * Methode zum Leeren (= Löschen) der Malfläche
     * Das Leeren wird mit Event-Wert 3 an PostXY übergeben
     */
    public void deletePainting() {
        drawID++;
        postXY(0, 0, 3, drawID);
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    /**
     * Methode, die die Mal-Infos in Arrays für den Get-String umwandelt
     * @param x
     * @param y
     * @param event_dif
     * @param drawID
     */
    public void postXY(float x, float y, int event_dif, int drawID) {

        //Modifiziert die koordinaten mit der Breite der Bildschirms
        x = x/Controller.getInstance().getUser().getScreenWidth();
        y = y/Controller.getInstance().getUser().getScreenWidth();

        //Erzeugt das Array und fügt es einer Liste hinzu
        String[] s = {"[X]=" + String.valueOf(x), "[Y]=" + String.valueOf(y), "[Event]=" + String.valueOf(event_dif), "[Id]=" + String.valueOf(drawID),
                "[LobbyId]=" + Controller.getInstance().getGame().getLobbyId(),"[Color]=" + colorString};

        drawPoints.add(s);

        //Hat die Liste 10 Einträge, oder wurde Event 2 oder 3 ausgelöst (Finger UP, bzw. Löschen)
        //dann wird die Liste weitergegeben und anschließend geleert
        if (drawPoints.size() == 10 || event_dif > 1) {
            createValues(drawPoints);
            drawPoints.clear();
        }
    }

    /**
     * Methode, die den Finalen Get-String zur Übermittlung der Bildpunkte an die DB erzeugt
     * Der String enthält am Ende ein Get-Array mit bis zu 10 Bildpunkten
     * @param al
     */
    public void createValues(ArrayList<String[]> al) {

        String values = "";

        for (int i = 0; i < al.size(); i++) {

            for (int j = 0; j < al.get(i).length; j++) {

                values = values + "Values[" + i + "]" + al.get(i)[j] + "&";
            }
        }

        values = values.substring(0, (values.length()-1));

        //Übergibt den String
        new DrawPointsTask().execute(values);
    }

    /**
     * Setzt die Farbe
     * @param newColor
     */
    public void setColor(String newColor){

        colorString = newColor.substring(1);

        //set color
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    /**
     * Überträgt per HttpGet bis zu 10 Bildpunkte als Array an die DB
     */
    private class DrawPointsTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            HttpClient httpclient = new DefaultHttpClient();

            //Execute-String
            String urlPoints = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json"
                    + "&method=DrawArrayPoints&" + strings[0];

            //Führt die GetFunktion aus
            try {
                httpclient.execute(new HttpGet(urlPoints));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    //Getter & Setter
    public int getDrawID() {
        return drawID;
    }

    public void setDrawID(int drawID) {
        this.drawID = drawID;
    }
}
