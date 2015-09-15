package model;


import java.util.ArrayList;

public class Lobby {

    private int id;
    private String name;
    private ArrayList<Integer> users;

    public Lobby(int id, String name, ArrayList<Integer> users) {
        this.id = id;
        this.name = name;
        this.users = users;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Integer> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<Integer> users) {
        this.users = users;
    }
}
