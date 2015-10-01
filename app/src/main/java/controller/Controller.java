package controller;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import Data.Data;
import model.Game;
import model.Lobby;
import model.PicturePart;
import model.User;

/**
 * Kontroll-Klasse
 */
public class Controller {

    /**
     * Singleton Pattern: Stellt sicher, dass nur eine Instanz von MainController existiert
     */
    private static Controller instance = new Controller();

    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }

        return instance;
    }

    //--- Start Attribute ---
    private ArrayList<PicturePart> pParts;
    private ArrayList<Lobby> lobbyList;
    private int lastPP = 0;
    private String activeLobby = "";
    private User user;
    private Game game;
    private int wait = 0;
    private int pictureWait = 0;
    private HttpClient httpclient;

    private int refreshUser = 0;
    //--- Ende Attribute ---

    /**
     * Konstruktor
     */
    public Controller(){

        this.pParts = new ArrayList<PicturePart>();
        this.user = new User();
        this.httpclient = new DefaultHttpClient();
        this.lobbyList = new ArrayList<Lobby>();

        //Instanziert einen Timer
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                new AutoRefreshDataTask().execute();
            }

            //Intervall (initiale Pause, Pause zwishcne den Durchläufen
        }, 2000, 2000);
    }

    /**
     * Holt Lobbys per AsyncTask aus der DB
     */
    public void getLobbys() {

        new GetLobbysTask().execute();
    }

    /**
     * Erzeugt das User-Objekt per AsyncTask
     * @param u
     */
    public void setUser(String u) {

        new SetUserTask().execute(u);
    }

    /**
     * Holt per AsyncTask Bild-Koordinaten aus der Datenbank
     */
    public void getPictureParts() {

        new GetPicturePartsTask().execute();
    }

    /**
     *
     */
    public void createGame(String lobbyId) {

        game = new Game();
        game.setLobbyId(lobbyId);

        new CreateGameTask().execute(lobbyId);

        //Game in DB erzeugen und gameID zurück bekommen

        //Dann mit LobbyId die User abfragen und einen davon als Painter festlegen

        //Begriff holen --> geht das alles mit einem Get??

        //View öffnen, abhängig davon, ob Painter oder nicht


        //Per flag in die Auto-Schleife aufnehmen?

    }

    /**
     * Holt per HttpGet die Daten aller Lobbys inkl. der zugeordneten User aus der DB
     * Erstellt daraus Lobby-Objekte und fügt diese der LobbyListe hinzu
     */
    private class GetLobbysTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            //Die Liste wird zunächst geleert
            lobbyList.clear();

            //HttpResponse
            HttpResponse response = null;

            //Execute-String
            String urlGetAllLobby = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=getAllLobbyFromDb";

            //Führt die GetFunktion aus
            try {
                response = httpclient.execute(new HttpGet(urlGetAllLobby));
            } catch (IOException e) {
                e.printStackTrace();
            }

            StatusLine statusLine = response.getStatusLine();

            if(statusLine.getStatusCode() == HttpStatus.SC_OK) {

                //Schreibt die Antwort in einen Output Stream und erzeugt daraus einen String
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                try {
                    response.getEntity().writeTo(out);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String responseString = out.toString();

                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Erzeugt aus dem Antwort-String ein JSON-Objekt
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray ja = jsonObject.getJSONArray("data");

                    //Aus dem Data-Array werden die LobbyIDs extrahiert und damit die zugehörigen User aus der DB geholt
                    for (int i = 0; i < ja.length(); i++) {
                        JSONArray jaa = ja.getJSONArray(i);

                        //ArrayList für User
                        ArrayList<String> lobbyUsers = new ArrayList<String>();

                        //HttpResponse
                        HttpResponse userResponse = null;

                        //Execute-String
                        String urlGetAllUserForLobby = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=getAllUserForLobbyId&LobbyId=" + jaa.get(0);

                        //Führt die GetFunktion aus
                        try {
                            userResponse = httpclient.execute(new HttpGet(urlGetAllUserForLobby));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        StatusLine userStatusLine = userResponse.getStatusLine();

                        if(userStatusLine.getStatusCode() == HttpStatus.SC_OK) {

                            //Schreibt die Antwort in einen Output Stream und erzeugt daraus einen String
                            ByteArrayOutputStream userOut = new ByteArrayOutputStream();

                            try {
                                userResponse.getEntity().writeTo(userOut);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            String userResponseString = userOut.toString();

                            try {
                                userOut.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            //Erzeugt aus dem Antwort-String ein JSON-Objekt
                            try {
                                JSONObject userJsonObject = new JSONObject(userResponseString);
                                JSONArray uja = userJsonObject.getJSONArray("data");

                                for (int j = 0; j < uja.length(); j++) {
                                    JSONArray ujaa = uja.getJSONArray(j);

                                    //Aus dem Data-Array werden die User extrahiert und der User-Liste zugefügt
                                    //Lobby-Owner werden markiert
                                    if ((Integer)ujaa.get(2) == 1) {
                                        lobbyUsers.add((String) ujaa.get(1) + " (Owner)");
                                    } else {
                                        lobbyUsers.add((String) ujaa.get(1));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        //Mit den Daten aus beiden Get-Aufrufen wird eine Lobby erstellt und der Lobby-Liste zugefügt
                        Lobby lobby = new Lobby((String)jaa.get(0), (String)jaa.get(1), lobbyUsers);
                        lobbyList.add(lobby);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //Setzt den Wait-Wert auf 0 -> Damit kann die wartende Activity weiter machen
            wait = 0;
            return null;
        }
    }

    /**
     * Holt per HttpGet die Daten des eingeloggten Users und übergibt diese an das User-Objekt
     */
    private class SetUserTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            //Username
            String userName = strings[0];

            //HttpResponse
            HttpResponse response = null;

            //Execute String
            String urlGetUserID = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=getUserId&UserName=" + userName;

            //Führt die GetFunktion aus
            try {
                response = httpclient.execute(new HttpGet(urlGetUserID));
            } catch (IOException e) {
                e.printStackTrace();
            }

            StatusLine statusLine = response.getStatusLine();

            if(statusLine.getStatusCode() == HttpStatus.SC_OK) {

                //Schreibt die Antwort in einen Output Stream und erzeugt daraus einen String
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                try {
                    response.getEntity().writeTo(out);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String responseString = out.toString();

                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Erzeugt aus dem Antwort-String ein JSON-Objekt
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray uja = jsonObject.getJSONArray("data");

                    //Übergibt die Daten aus dem Data-Array an das User-Objekt
                    user.createUser((String)uja.get(0), userName, (String)uja.get(1), (Integer)uja.get(2));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //Aktiviert die Refresh-Schleife für den User
            refreshUser = 1;

            //Setzt den Wait-Wert auf 0 -> Damit kann die wartende Activity weiter machen
            wait = 0;

            return null;
        }
    }

    /**
     * Holt per HttpGet die Koordinaten des gemalten Bildes aus der DB
     */
    private class GetPicturePartsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            //Löscht zunächst die Liste mit Koordinaten
            pParts.clear();

            //HttpResponse
            HttpResponse response = null;

            //Execute-String
            String urlgetDrawPoints = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=getDrawPoints&minId=" + lastPP;

            //Führt die GetFunktion aus
            try {
                response = httpclient.execute(new HttpGet(urlgetDrawPoints));
            } catch (IOException e) {
                e.printStackTrace();
            }

            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

                //Schreibt die Antwort in einen Output Stream und erzeugt daraus einen String
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                try {
                    response.getEntity().writeTo(out);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String responseString = out.toString();

                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Erzeugt aus dem Antwort-String ein JSON-Objekt
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray ja = jsonObject.getJSONArray("data");

                    //Aus dem Data-Array werden PicturePart-Objekte erzeugt. Diese fügen sich selbst der Koordinaten-Liste hinzu
                    for (int i = 0; i < ja.length(); i++) {
                        JSONArray jaa = ja.getJSONArray(i);

                        PicturePart pP = new PicturePart(Float.parseFloat(jaa.getString(0)), Float.parseFloat(jaa.getString(1)),
                                Integer.parseInt(jaa.getString(2)), Integer.parseInt(jaa.getString(3)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            pictureWait = 0;
            return null;
        }
    }

    /**
     *
     */
    private class CreateGameTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            String lobbyId = strings[0];

            //HttpResponse
            HttpResponse createGameResponse = null;

            //Execute-String
            String urlCreateGame = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=NewGameObject&LobbyId=" + lobbyId;

            //Führt die GetFunktion aus
            try {
                createGameResponse = httpclient.execute(new HttpGet(urlCreateGame));
            } catch (IOException e) {
                e.printStackTrace();
            }

            StatusLine statusLine = createGameResponse.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

                //Schreibt die Antwort in einen Output Stream und erzeugt daraus einen String
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                try {
                    createGameResponse.getEntity().writeTo(out);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String responseString = out.toString();

                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //data:
                //["560cec0a7a1388.96822279","CompuGlobalHyperMegaNet",["55f9456eb029c0.75414538","83c8782a-56e0-11e5-9300-080027003c36"]]

                //Erzeugt aus dem Antwort-String ein JSON-Objekt
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray ja = jsonObject.getJSONArray("data");

                    //Extrahiert GameId und ActiveWord aus data
                    game.setId((String) ja.get(0));
                    game.setActiveWord((String) ja.get(1));

                    //Bildet ein inneres Array aus dem data-Array mit UserIds
                    JSONArray jaa = ja.getJSONArray(ja.getInt(2));

                    //Übergibt die UserIds an das Game-Objekt
                    for (int i = 0; i < jaa.length(); i++) {

                        game.getUserIds().add((String) jaa.get(i));
                    }

                    //Erzeugt eine Zufallszahl mit max = Anzahl User
                    //Mit dieser Zahl wird der erste Maler festgelegt
                    int randomInt = (int)(Math.random() * game.getUserIds().size());

                    //Übergibt den GameActive Status und isPainter an die DB
                    for (int i = 0; i < game.getUserIds().size(); i++) {

                        //Bei match mit der Zufallszahl: isPainter = 1 ...
                        if (i == randomInt) {

                            //gameActive = 2, is Painter = 1

                            //Execute-String
                            String urlSetUserData = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=NewGameObject&LobbyId=" + lobbyId;

                            //Führt die GetFunktion aus
                            try {
                                createGameResponse = httpclient.execute(new HttpGet(urlSetUserData));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        //Andernfalls: isPainter = 0
                        } else {

                            ////gameActive = 2, is Painter = 0
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    /**
     *
     */
    private class AutoRefreshDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            //Autoschleife mit allen Update-Abfragen (User, Game, Lobby...?)

            if (refreshUser == 1) {

                //HttpResponse
                HttpResponse refreshUserResponse = null;

                //Execute-String
                String urlRefreshUser = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=GetUserInformation&UserId=" + user.getId();

                //Führt die GetFunktion aus
                try {
                    refreshUserResponse = httpclient.execute(new HttpGet(urlRefreshUser));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                StatusLine statusLine = refreshUserResponse.getStatusLine();

                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

                    //Schreibt die Antwort in einen Output Stream und erzeugt daraus einen String
                    ByteArrayOutputStream out = new ByteArrayOutputStream();

                    try {
                        refreshUserResponse.getEntity().writeTo(out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String responseString = out.toString();

                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //Erzeugt aus dem Antwort-String ein JSON-Objekt
                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        JSONArray ja = jsonObject.getJSONArray("data");

                        user.setCurrentLobbyId((String) ja.get(0));
                        user.setIsLobbyOwner((Integer) ja.get(1));
                        user.setIsPainter((Integer) ja.get(2));
                        user.setGameActive((Integer) ja.get(3));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }
    }

    //Getter & Setter
    public ArrayList<PicturePart> getpParts() {
        return pParts;
    }

    public void setpParts(ArrayList<PicturePart> pParts) {
        this.pParts = pParts;
    }

    public int getLastPP() {
        return lastPP;
    }

    public void setLastPP(int lastPP) {
        this.lastPP = lastPP;
    }

    public String getActiveLobby() {
        return activeLobby;
    }

    public void setActiveLobby(String activeLobby) {
        this.activeLobby = activeLobby;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getWait() {
        return wait;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }

    public ArrayList<Lobby> getLobbyList() {
        return lobbyList;
    }

    public void setLobbyList(ArrayList<Lobby> lobbyList) {
        this.lobbyList = lobbyList;
    }

    public int getPictureWait() {
        return pictureWait;
    }

    public void setPictureWait(int pictureWait) {
        this.pictureWait = pictureWait;
    }
}