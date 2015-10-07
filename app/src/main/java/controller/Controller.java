package controller;

import android.os.AsyncTask;
import android.os.Handler;

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

import Data.Data;
import model.Game;
import model.Lobby;
import model.PicturePart;
import model.User;

/**
 * Controller-Klasse
 * Der Controller instanziert User und Game, führt einen Großteil der Get-Funktionen aus
 * und holt per Refresh-Task kontinuierlich aktuelle Daten aus der DB
 *
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
    private int wordWait = 0;
    private Handler handler;

    private int refreshUser = 0;
    private int refreshGame = 0;
    //--- Ende Attribute ---

    /**
     * Konstruktor
     */
    public Controller(){

        this.pParts = new ArrayList<PicturePart>();

        this.user = User.getInstance();
        this.game = Game.getInstance();
        this.lobbyList = new ArrayList<Lobby>();

        //Handler, der die Refresh-Runnable aufruft
        handler = new Handler();
        handler.postDelayed(refreshRunnable, 500);
    }

    /**
     * RunnableObjekt, das kontinuierlich Refresh-Tasks in einem eigenen Thread startet
     */
    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshThread.run();
            handler.postDelayed(this, 500);
        }
    };

    /**
     * Thread, der den RefreshTask aufruft
     */
    Thread refreshThread = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                new AutoRefreshDataTask().execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

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
     * Erzeugt das Game-Objekt per AsyncTask
     * @param lobbyId
     */
    public void createGame(String lobbyId) {

        game.setLobbyId(lobbyId);

        new CreateGameTask().execute(lobbyId);
    }

    /**
     * Setzt den GameActive-Status für alle User einer Lobby per AsyncTask
     * @param array
     * -> LobbyId, Status
     */
    public void setGameActive(String[] array) {

        new SetGameActiveTask().execute(array);
    }

    /**
     * Resettet ein Game für eine neue Spielrunde per AsyncTask
     * Setzt: User-ReadyStates, MalerId, NextMalerId, Resolved, isPainter
     * @param state
     */
    public void resetGame(String state) {

        String[] array = {game.getLobbyId(), state};

        new ResetGameTask().execute(array);
    }

    /**
     * Setzt das Game auf "gelöst" und übergibt den nächsten Maler per AsyncTask
     * @param nextPainter
     * @param state
     */
    public void setResolved(String nextPainter, String state) {

        new SetResolvedTask().execute(nextPainter, state);
    }

    /**
     * Setzt den ReadyState eines Users per AsyncTask
     */
    public void setUserReady() {

        new SetUserReadyTask().execute();
    }

    /**
     * Löscht die Bild-Koordinaten einer Lobby per AsyncTask
     */
    public void truncateCoordinates() {

        new TruncateCoordinatesTask().execute();
    }

    /**
     * Legt einen neuen, zufälligen Malbegriff per AsyncTask fest
     */
    public void updateWord() {

        new UpdateWordTask().execute();
    }

    //--- Anfang Tasks ---

    /* Holt per HttpGet die Daten aller Lobbys inkl. der zugeordneten User aus der DB
    * Erstellt daraus Lobby-Objekte und fügt diese der LobbyListe hinzu
    */
    private class GetLobbysTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            HttpClient httpclient = new DefaultHttpClient();

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
                    response.getEntity().consumeContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //httpclient.getConnectionManager().shutdown();

                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ArrayList<Lobby> tempLobbyList = new ArrayList<Lobby>();

                //Erzeugt aus dem Antwort-String ein JSON-Objekt
                try {
                    JSONObject jsonObject = new JSONObject(responseString);

                    //Äusseres Array mit allen Lobbys
                    JSONArray allLobbysArray = jsonObject.getJSONArray("data");

                    //Aus dem Data-Array werden die LobbyIDs extrahiert und damit die zugehörigen User aus der DB geholt
                    for (int i = 0; i < allLobbysArray.length(); i++) {

                        //Mittleres Array mit Daten einer Lobby
                        JSONArray oneLobbyArray = allLobbysArray.getJSONArray(i);

                        //Inneres Array mit den Usern einer Lobby
                        JSONArray lobbyUsersArray = oneLobbyArray.getJSONArray(4);

                        //ArrayList für User
                        ArrayList<String> lobbyUsers = new ArrayList<String>();

                        for (int k = 0; k < lobbyUsersArray.length(); k++) {

                            JSONArray oneUserArray = lobbyUsersArray.getJSONArray(k);

                            //Die User einer Lobby werden der User-Liste zugefügt
                            //Lobby-Owner werden markiert
                            if ((Integer) oneUserArray.get(2) == 1) {
                                lobbyUsers.add((String) oneUserArray.get(1) + " (Owner)");
                            } else {
                                lobbyUsers.add((String) oneUserArray.get(1));
                            }
                        }

                        //Mit den Daten aus beiden Get-Aufrufen wird eine Lobby erstellt und der Lobby-Liste zugefügt
                        Lobby lobby = new Lobby((String) oneLobbyArray.get(0), (String) oneLobbyArray.get(1), lobbyUsers);
                        tempLobbyList.add(lobby);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Die Liste wird zunächst geleert
                lobbyList.clear();
                lobbyList = tempLobbyList;
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

            HttpClient httpclient = new DefaultHttpClient();

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
                    response.getEntity().consumeContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }

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

            //Nach dem Einloggen und dem Erstellen des User-Objekts werden in der DB die spielrelevanten Daten resettet
            //-> UserCurrentLobbyId=0, isLobbyOwner=0, isPainter=0, UserIsReady=0, GameAcive=0

            //Execute String
            String urlResetUser = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=ResetPlayerAfterLogin&UserId=" + user.getId();

            //Führt die GetFunktion aus
            try {
                httpclient.execute(new HttpGet(urlResetUser));
            } catch (IOException e) {
                e.printStackTrace();
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

            HttpClient httpclient = new DefaultHttpClient();

            //Löscht zunächst die Liste mit Koordinaten
            pParts.clear();

            //HttpResponse
            HttpResponse response = null;

            //Execute-String
            String urlgetDrawPoints = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=getDrawPoints&minId=" + lastPP + "&LobbyId=" + game.getLobbyId();

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
                    response.getEntity().consumeContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }

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
                                Integer.parseInt(jaa.getString(2)), Integer.parseInt(jaa.getString(3)), jaa.getString(4));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //Setzt den Wait-Wert auf 0 -> Damit kann die wartende Activity weiter machen
            pictureWait = 0;

            return null;
        }
    }

    /**
     * Erzeugt per HttpGet ein Gameobject in der DB.
     * Liefert die ID des neuen Games, den ersten Malbegriff sowie die zugehörigen User zurück
     */
    private class CreateGameTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            HttpClient httpclient = new DefaultHttpClient();

            String lobbyId = strings[0];

            //HttpResponse
            HttpResponse createGameResponse = null;

            //Execute-String
            String urlCreateGame = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=SetGame&State=2&LobbyId=" + lobbyId;

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
                    createGameResponse.getEntity().consumeContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Erzeugt aus dem Antwort-String ein JSON-Objekt
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray ja = jsonObject.getJSONArray("data");

                    //Extrahiert GameId und ActiveWord aus data
                    game.setId((String) ja.get(0));
                    game.setActiveWord((String) ja.get(1));

                    //Bildet ein inneres Array aus dem data-Array mit UserIds
                    JSONArray jaa = ja.getJSONArray(2);

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

                            //Prüft, ob der User der ausgewählte Maler ist
                            if (game.getUserIds().get(i).equals(user.getId())) {

                                //Setzt das isPainter flag des User-Objekts
                                user.setIsPainter(1);
                            }

                            //Übergibt die MalerId an die Datenbank (gameobjects + user)
                            //Execute-String
                            String urlSetPainter = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=SetPainter"
                                    + "&LobbyId=" + lobbyId + "&UserId=" + game.getUserIds().get(i) + "&GameId=" + game.getId();

                            //Führt die GetFunktion aus
                            try {
                                httpclient.execute(new HttpGet(urlSetPainter));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    //Aktiviert die Refresh-Schleife für das Game
                    refreshGame = 1;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    /**
     * Setzt per HttpGet die GameActive States aller Spieler einer Lobby
     */
    private class SetGameActiveTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            HttpClient httpclient = new DefaultHttpClient();

            String lobbyId = strings[0];
            String state = strings[1];

            //Execute-String
            String urlSetGameActive = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=SetGameActive&State=" + state + "&LobbyId=" + lobbyId;

            //Führt die GetFunktion aus
            try {
                httpclient.execute(new HttpGet(urlSetGameActive));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    /**
     * Resettet per HttpGet ein Game für eine neue Spielrunde
     * Setzt: User-ReadyStates, MalerId, NextMalerId, Resolved, isPainter
     */
    private class ResetGameTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            HttpClient httpclient = new DefaultHttpClient();

            String lobbyId = strings[0];
            String state = strings[1];
            String painter = "";

            //Gibt es noch keinen NextPainter (= 1. Runde), dann wird der ausführende Spieler (= der erste Maler) zum Painter im Gameobjekt
            if (game.getNextPainterId().equals("")) {
                painter = user.getId();

            //Anderfalls wird die NextPainterId als Maler hinterlegt
            } else {
                painter = game.getNextPainterId();
            }

            //Execute-String
            String urlSetAllIsReady = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=ResetGameForNewRound"
                    + "&State=" + state + "&LobbyId=" + lobbyId + "&NextMaler=" + painter;

            //Führt die GetFunktion aus
            try {
                httpclient.execute(new HttpGet(urlSetAllIsReady));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    /**
     * Setzt per HttpGet den ResolvedStatus eines Games und übergibt die ID des Lösers (-> er wird nächster Maler)
     */
    private class SetResolvedTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            HttpClient httpclient = new DefaultHttpClient();

            String nextPainter = strings[0];
            String state = strings[1];

            //Execute-String
            String urlSetResolved = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=SetResolved&NextPainter=" + nextPainter
                                + "&LobbyId=" + game.getLobbyId() + "&State=" + state;

            //Führt die GetFunktion aus
            try {
                httpclient.execute(new HttpGet(urlSetResolved));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    /**
     * Setzt für einen User per HttpGet das Ready-Flag
     */
    private class SetUserReadyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            HttpClient httpclient = new DefaultHttpClient();

            //Execute-String
            String urlSetReady = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json"
                    + "&method=UpdateIsUserReady&State=1&UserId=" + user.getId();

            //Führt die GetFunktion aus
            try {
                httpclient.execute(new HttpGet(urlSetReady));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    /**
     * Löscht die Bild-Koordinaten einer Lobby in der DB
     */
    private class TruncateCoordinatesTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            HttpClient httpclient = new DefaultHttpClient();

            //Execute-String
            String urlTruncateCoordinates = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json"
                    + "&method=truncateCoordinatesForLobbyId&LobbyId=" + game.getLobbyId();

            //Führt die GetFunktion aus
            try {
                httpclient.execute(new HttpGet(urlTruncateCoordinates));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    /**
     * Ersetzt den Malbegriff per HttpGet durch einen zufälligen neuen und gibt das neue Wort zurück
     */
    private class UpdateWordTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            HttpClient httpclient = new DefaultHttpClient();

            //HttpResponse
            HttpResponse updateWordResponse = null;

            //Execute-String
            String urlupdateWord = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=UpdateActiveWord&GameId=" + game.getId();

            //Führt die GetFunktion aus
            try {
                updateWordResponse = httpclient.execute(new HttpGet(urlupdateWord));
            } catch (IOException e) {
                e.printStackTrace();
            }

            StatusLine statusLine = updateWordResponse.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

                //Schreibt die Antwort in einen Output Stream und erzeugt daraus einen String
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                try {
                    updateWordResponse.getEntity().writeTo(out);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String responseString = out.toString();

                try {
                    updateWordResponse.getEntity().consumeContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Erzeugt aus dem Antwort-String ein JSON-Objekt
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray ja = jsonObject.getJSONArray("data");

                    //Extrahiert ActiveWord aus data
                    game.setActiveWord((String) ja.get(0));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //Setzt den Wait-Wert auf 0 -> Damit kann die wartende Activity weiter machen
            wordWait = 0;

            return null;
        }
    }

    /**
     *
     *//*
    private class AutoRefreshDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            //Autoschleife mit allen Update-Abfragen (User, Game, Lobby...?)

            //Aktualisiert die Lobbys
            getLobbys();

            //Aktualisiert den User
            if (refreshUser == 1) {

                HttpClient userhttpclient = new DefaultHttpClient();

                //HttpResponse
                HttpResponse refreshUserResponse = null;

                //Execute-String
                String urlRefreshUser = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=GetUserInformation&UserId=" + user.getId();

                //Führt die GetFunktion aus
                try {
                    refreshUserResponse = userhttpclient.execute(new HttpGet(urlRefreshUser));
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
                        refreshUserResponse.getEntity().consumeContent();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //httpclient.getConnectionManager().shutdown();

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

            //Aktualisiert das Game
            if (refreshGame == 1) {

                HttpClient gamehttpclient = new DefaultHttpClient();

                //HttpResponse
                HttpResponse refreshGameResponse = null;

                //Execute-String
                String urlRefreshGame = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=GetGameInformation&LobbyId=" + game.getLobbyId();

                //Führt die GetFunktion aus
                try {
                    refreshGameResponse = gamehttpclient.execute(new HttpGet(urlRefreshGame));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                StatusLine statusLine = refreshGameResponse.getStatusLine();

                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

                    //Schreibt die Antwort in einen Output Stream und erzeugt daraus einen String
                    ByteArrayOutputStream out = new ByteArrayOutputStream();

                    try {
                        refreshGameResponse.getEntity().writeTo(out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String responseString = out.toString();

                    try {
                        refreshGameResponse.getEntity().consumeContent();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //httpclient.getConnectionManager().shutdown();

                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //Erzeugt aus dem Antwort-String ein JSON-Objekt
                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        JSONArray ja = jsonObject.getJSONArray("data");

                        game.setId((String)ja.get(0));
                        game.setPainterId((String) ja.get(1));
                        game.setNextPainterId((String) ja.get(2));
                        game.setIsSolved((Integer) ja.get(3));
                        game.setActiveWord((String) ja.get(4));
                        game.setUsersReady((Integer) ja.get(5));

                        //Bildet ein inneres Array aus dem data-Array mit UserIds
                        JSONArray jaa = ja.getJSONArray(6);

                        game.getUserIds().clear();

                        //Übergibt die UserIds an das Game-Objekt
                        for (int i = 0; i < jaa.length(); i++) {

                            game.getUserIds().add((String) jaa.get(i));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }
    }*/


    /**
     * Der Autorefresh-Task läuft kontinuierlich im Hintegrund und aktualisiert ständig die Lobby-, User- und Game-Daten
     */
    private class AutoRefreshDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            String lobbyId;
            String userId;
            ArrayList<Lobby> tempLobbyList = new ArrayList<Lobby>();

            //Erzeugt einen Dummy-String, für den Fall, dass noch kein User existiert
            if (refreshUser == 1) {
                userId = user.getId();
            } else {
                userId = "a";
            }

            //Erzeugt einen Dummy-String, für den Fall, dass noch kein Game existiert
            if (refreshGame == 1) {
                lobbyId = game.getLobbyId();
            } else {
                lobbyId = "a";
            }

            HttpClient refreshHttpClient = new DefaultHttpClient();

            //HttpResponse
            HttpResponse refreshResponse = null;

            //Execute-String
            String urlRefreshData = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=RefreshData&LobbyId=" + lobbyId + "&UserId=" + userId;

            //Führt die GetFunktion aus
            try {
                refreshResponse = refreshHttpClient.execute(new HttpGet(urlRefreshData));
            } catch (IOException e) {
                e.printStackTrace();
            }

            StatusLine statusLine = refreshResponse.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

                //Schreibt die Antwort in einen Output Stream und erzeugt daraus einen String
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                try {
                    refreshResponse.getEntity().writeTo(out);
                    refreshResponse.getEntity().consumeContent();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String responseString = out.toString();


                try {
                    //Erzeugt aus dem Antwort-String ein JSON-Objekt
                    JSONObject jsonObject = new JSONObject(responseString);


                    //--- Anfang Lobby Daten ---
                    //Erzeugt ein Lobby-Daten-Array aus "lobbydata"
                    JSONArray lobbyDataArray = jsonObject.getJSONArray("lobbydata");

                    //Aus dem Data-Array werden die LobbyIDs extrahiert und damit die zugehörigen User aus der DB geholt
                    for (int i = 0; i < lobbyDataArray.length(); i++) {

                        //Array mit den Daten einer Lobby
                        JSONArray oneLobbyArray = lobbyDataArray.getJSONArray(i);

                        //Inneres Array mit den Usern einer Lobby
                        JSONArray lobbyUsersArray = oneLobbyArray.getJSONArray(4);

                        //ArrayList für User
                        ArrayList<String> lobbyUsers = new ArrayList<String>();

                        for (int k = 0; k < lobbyUsersArray.length(); k++) {

                            //Inneres Array mit den Daten eines Users
                            JSONArray oneUserArray = lobbyUsersArray.getJSONArray(k);

                            //Die User einer Lobby werden der User-Liste zugefügt
                            //Lobby-Owner werden markiert
                            if ((Integer) oneUserArray.get(2) == 1) {
                                lobbyUsers.add((String) oneUserArray.get(1) + " (Owner)");
                            } else {
                                lobbyUsers.add((String) oneUserArray.get(1));
                            }
                        }

                        //Mit den Daten wird eine Lobby erstellt und der temporären Lobby-Liste zugefügt
                        Lobby lobby = new Lobby((String) oneLobbyArray.get(0), (String) oneLobbyArray.get(1), lobbyUsers);
                        tempLobbyList.add(lobby);
                    }

                    //Die "echte" LobbyListe wird aktualisiert
                    lobbyList.clear();
                    lobbyList = tempLobbyList;

                    //--- Ende Lobby Daten ---

                    //--- Anfang User Daten ---
                    if (refreshUser == 1) {

                        //Erzeugt ein User-Daten-Array aus "userdata"
                        JSONArray userDataArray = jsonObject.getJSONArray("userdata");

                        //Übergibt die Daten an das User-Objekt
                        user.setCurrentLobbyId((String) userDataArray.get(0));
                        user.setIsLobbyOwner((Integer) userDataArray.get(1));
                        user.setIsPainter((Integer) userDataArray.get(2));
                        user.setGameActive((Integer) userDataArray.get(3));
                    }

                    //--- Ende User Daten ---

                    //--- Anfang Game Daten ---
                    if (refreshGame == 1) {

                        //Erzeugt ein Game-Daten-Array aus "gamedata"
                        JSONArray gameDataArray = jsonObject.getJSONArray("gamedata");

                        //Übergibt die Daten an das GameObjekt
                        game.setId((String) gameDataArray.get(0));
                        game.setPainterId((String) gameDataArray.get(1));
                        game.setNextPainterId((String) gameDataArray.get(2));
                        game.setIsSolved((Integer) gameDataArray.get(3));
                        game.setActiveWord((String) gameDataArray.get(4));
                        game.setUsersReady((Integer) gameDataArray.get(5));

                        //Bildet ein inneres Array aus dem gamedata-Array mit UserIds
                        JSONArray gameUserArray = gameDataArray.getJSONArray(6);

                        game.getUserIds().clear();

                        //Übergibt die UserIds an das Game-Objekt
                        for (int i = 0; i < gameUserArray.length(); i++) {

                            game.getUserIds().add((String) gameUserArray.get(i));
                        }
                    }

                    //--- Ende Game Daten ---

                } catch (JSONException e) {
                    e.printStackTrace();
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

    public int getWordWait() {
        return wordWait;
    }

    public void setWordWait(int wordWait) {
        this.wordWait = wordWait;
    }

    public void setPictureWait(int pictureWait) {
        this.pictureWait = pictureWait;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getRefreshGame() {
        return refreshGame;
    }

    public void setRefreshGame(int refreshGame) {
        this.refreshGame = refreshGame;
    }
}