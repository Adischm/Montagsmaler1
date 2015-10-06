package model;

import controller.Controller;

public class PicturePart {

    private float x;
    private float y;
    private int event;
    private int id;
    private String color;

    public PicturePart(float x, float y, int event, int id, String color) {

        this.x = x;
        this.y = y;
        this.event = event;
        this.id = id;
        this.color = color;

        Controller.getInstance().getpParts().add(this);
    }

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
