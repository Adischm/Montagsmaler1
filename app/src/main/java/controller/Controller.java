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

    public Controller(){

        this.pParts = new ArrayList<PicturePart>();
    }

    public ArrayList<Lobby> getLobbys() {

        ArrayList<Lobby> lobbyList = new ArrayList<Lobby>();
        ArrayList<Integer> testAL = new ArrayList<Integer>();
        testAL.add(1);
        testAL.add(3);
        testAL.add(4);

        Lobby testLobby = new Lobby(1, "testlobby", testAL);
        Lobby testLobby2 = new Lobby(2, "testlobby", testAL);
        Lobby testLobby3 = new Lobby(3, "testlobby", testAL);

        lobbyList.add(testLobby);
        lobbyList.add(testLobby2);
        lobbyList.add(testLobby3);

        return lobbyList;
    }

    public void getPictureParts() {

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = null;

        try {
            response = httpclient.execute(new HttpGet("http://192.168.43.226/MontagsMalerService/index.php?" +
                    "format=json&method=getDrawPoints"));
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
                            Integer.parseInt(jaa.getString(2)));
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
}
