package model;

import java.util.ArrayList;

/**
 *
 */
public class Game {

    //Singleton Pattern: Stellt sicher, dass nur eine Instanz von Game existiert
    private static Game instance = new Game();

    public static Game getInstance() {
        if (instance == null) {
            instance = new Game();
        }

        return instance;
    }

    //--- Start Attribute ---
    private String id;
    private String lobbyId;
    private String activeWord;
    private String painterId;
    private int usersReady;

    private ArrayList<String> userIds;

    //--- Ende Attribute ---

    /**
     * Konstruktor
     */
    public Game() {

        this.id = "";
        this.lobbyId = "";
        this.activeWord = "";
        this.painterId = "";
        this.usersReady = 0;

        this.userIds = new ArrayList<String>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    public String getActiveWord() {
        return activeWord;
    }

    public void setActiveWord(String activeWord) {
        this.activeWord = activeWord;
    }

    public String getPainterId() {
        return painterId;
    }

    public void setPainterId(String painterId) {
        this.painterId = painterId;
    }

    public ArrayList<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(ArrayList<String> userIds) {
        this.userIds = userIds;
    }

    public int getUsersReady() {
        return usersReady;
    }

    public void setUsersReady(int usersReady) {
        this.usersReady = usersReady;
    }
}
