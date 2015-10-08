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

/**
 * Klasse für das View-Objekt (= Anzeigefläche) der WatchActivity
 */
public class WatchingView extends View {

    //--- Anfang Attribute ---
    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = 0xFF000000;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private float brushSize;

    //--- Ende Attribute ---

    /**
     * Konstruktor
     * @param context
     * @param attrs
     */
    public WatchingView(Context context, AttributeSet attrs) {
        super(context, attrs);
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

    /**
     * Methode zur Darstellung des Gemalten auf der Fläche
     * @param x
     * @param y
     * @param event
     * @param color
     */
    public void paintPicture(float x, float y, int event, String color) {

        //Setzt die Farbe
        setColor("#" + color);

        //Je nach Event-Wert wird ein Absetzen, ein Bewegen oder ein Abheben des Fingers simuliert
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

        //Aktualisiert die Ansicht
        invalidate();
    }

    /**
     * Setzt die Farbe
     * @param newColor
     */
    public void setColor(String newColor){
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    /**
     * Methode zum Leeren (= Löschen) der Fläche
     */
    public void deletePainting() {
        drawCanvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
        invalidate();
    }
}