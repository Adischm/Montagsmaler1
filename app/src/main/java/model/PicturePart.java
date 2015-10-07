package model;

import controller.Controller;

/**
 * Klasse für Bildpunkte, die in der Watch-Aktivity das gemalte Bild replizieren
 */
public class PicturePart {

    //--- Anfang Attribute ---
    private float x;
    private float y;
    private int event;
    private int id;
    private String color;

    //--- Ende Attribute ---

    /**
     * Konstruktor
     * @param x
     * @param y
     * @param event
     * @param id
     * @param color
     */
    public PicturePart(float x, float y, int event, int id, String color) {

        this.x = x;
        this.y = y;
        this.event = event;
        this.id = id;
        this.color = color;

        //Fügt den neu erzeugten Bildpunkt der Liste des Controllers hinzu
        Controller.getInstance().getpParts().add(this);
    }

    //Getter & Setter
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}