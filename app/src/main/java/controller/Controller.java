package controller;

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

import model.Lobby;
import model.PicturePart;

public class Controller {

    private static Controller instance = new Controller();

    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }

        return instance;
    }

    private ArrayList<PicturePart> pParts;
    private ArrayList<Lobby> lobbyList;
    private int lastPP = 0;
    private String activeLobby = "";

    public Controller(){

        this.pParts = new ArrayList<PicturePart>();
    }

//    public ArrayList<Lobby> getLobbys() {
//
//        ArrayList<Lobby> lobbyList = new ArrayList<Lobby>();
//        ArrayList<Integer> testAL = new ArrayList<Integer>();
//        testAL.add(1);
//        testAL.add(3);
//        testAL.add(4);
//
//        Lobby testLobby = new Lobby(1, "testlobby1", testAL);
//        Lobby testLobby2 = new Lobby(2, "testlobby2", testAL);
//        Lobby testLobby3 = new Lobby(3, "testlobby3", testAL);
//
//        lobbyList.add(testLobby);
//        lobbyList.add(testLobby2);
//        lobbyList.add(testLobby3);
//
//        return lobbyList;
//    }

    public ArrayList<Lobby> getLobbys() {

        lobbyList = new ArrayList<Lobby>();

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = null;

        try {
            response = httpclient.execute(new HttpGet("http://192.168.43.226/MontagsMalerService/index.php?" +
                    "format=json&method=getAllLobbyFromDb"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        StatusLine statusLine = response.getStatusLine();
        if(statusLine.getStatusCode() == HttpStatus.SC_OK) {
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


            try {
                JSONObject jsonObject = new JSONObject(responseString);
                JSONArray ja = jsonObject.getJSONArray("data");

                for (int i = 0; i < ja.length(); i++) {
                    JSONArray jaa = ja.getJSONArray(i);

                    ArrayList<String> lobbyUsers = new ArrayList<String>();

                    HttpResponse userResponse = null;

                    try {
                        userResponse = httpclient.execute(new HttpGet("http://192.168.43.226/MontagsMalerService/index.php?" +
                                "format=json&method=getAllUserForLobbyId&LobbyId=" + jaa.get(0)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    StatusLine userStatusLine = userResponse.getStatusLine();
                    if(statusLine.getStatusCode() == HttpStatus.SC_OK) {
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

                        try {
                            JSONObject userJsonObject = new JSONObject(userResponseString);
                            JSONArray uja = userJsonObject.getJSONArray("data");

                            for (int j = 0; j < uja.length(); j++) {
                                JSONArray ujaa = uja.getJSONArray(j);

                                lobbyUsers.add((String)ujaa.get(1));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    Lobby lobby = new Lobby((String)jaa.get(0), (String)jaa.get(1), lobbyUsers);
                    lobbyList.add(lobby);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return lobbyList;
    }

    public void getPictureParts() {

        pParts.clear();

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = null;

        try {
            response = httpclient.execute(new HttpGet("http://192.168.43.226/MontagsMalerService/index.php?" +
                    "format=json&method=getDrawPoints&minId=" + lastPP));
        } catch (IOException e) {
            e.printStackTrace();
        }

        StatusLine statusLine = response.getStatusLine();
        if(statusLine.getStatusCode() == HttpStatus.SC_OK) {
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


            try {
                JSONObject jsonObject = new JSONObject(responseString);
                JSONArray ja = jsonObject.getJSONArray("data");

                for (int i = 0; i < ja.length(); i++) {
                    JSONArray jaa = ja.getJSONArray(i);

                    PicturePart pP = new PicturePart(Float.parseFloat(jaa.getString(0)), Float.parseFloat(jaa.getString(1)),
                            Integer.parseInt(jaa.getString(2)), Integer.parseInt(jaa.getString(3)));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void testIt() {

    }

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
}
