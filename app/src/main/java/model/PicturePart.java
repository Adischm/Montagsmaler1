package model;

import controller.Controller;

public class PicturePart {

    private float x;
    private float y;
    private int event;

    public PicturePart(float x, float y, int event) {

        this.x = x;
        this.y = y;
        this.event = event;

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
}
