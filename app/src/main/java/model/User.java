package model;

/**
 * Klasse für das User-Objekt
 * Jedes Device hat nur 1 User, den Spieler
 */
public class User {

    //Singleton Pattern: Stellt sicher, dass nur eine Instanz von User existiert
    private static User instance = new User();

    public static User getInstance() {
        if (instance == null) {
            instance = new User();
        }

        return instance;
    }

    //--- Start Attribute ---
    private String id;
    private String name;
    private String currentLobbyId;
    private int isLobbyOwner;
    //--- Ende Attribute ---

    //Konstruktor
    public User () {}

    //Methode, die Attribute des Users setzt
    public void createUser (String id, String name, String currentLobbyId, int isLobbyOwner) {

        this.id = id;
        this.name = name;
        this.currentLobbyId = currentLobbyId;
        this.isLobbyOwner = isLobbyOwner;
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

    public String getCurrentLobbyId() {
        return currentLobbyId;
    }

    public void setCurrentLobbyId(String currentLobbyId) {
        this.currentLobbyId = currentLobbyId;
    }

    public int getIsLobbyOwner() {
        return isLobbyOwner;
    }

    public void setIsLobbyOwner(int isLobbyOwner) {
        this.isLobbyOwner = isLobbyOwner;
    }
}