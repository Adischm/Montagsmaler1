package model;

import java.util.ArrayList;

/**
 * Klasse für Lobby-Objekte
 */
public class Lobby {

    private String id;
    private String name;
    private ArrayList<String> users;

    /**
     * Konstruktor
     * @param id
     * @param name
     * @param users
     */
    public Lobby(String id, String name, ArrayList<String> users) {
        this.id = id;
        this.name = name;
        this.users = users;
    }

    //Getter & Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }
}
