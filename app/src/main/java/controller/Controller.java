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

import Data.Data;
import model.Lobby;
import model.PicturePart;
import model.User;

/**
 * Kontroll-Klasse
 */
public class Controller {

    private static Controller instance = new Controller();

    /**
     * Singleton Pattern: Stellt sicher, dass nur eine Instanz von MainController existiert
     * @return
     */
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
    private int wait = 0;
    private HttpClient httpclient;
    //--- Ende Attribute ---

    /**
     * Konstruktor
     */
    public Controller(){

        this.pParts = new ArrayList<PicturePart>();
        this.user = new User();
        this.httpclient = new DefaultHttpClient();
        this.lobbyList = new ArrayList<Lobby>();
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
}