package schule.adrian.montagsmaler;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import Data.Data;
import controller.Controller;

/**
 * Activity für den Lobby-Übersichts Screen
 */
public class LobbyTestActivity extends AppCompatActivity implements View.OnClickListener {

    //--- Anfang Attribute ---
    public Button button_new;
    public Button button_refresh;
    private HttpClient httpclient;
    private ArrayList<String> lobbyNames;
    //--- Ende Attribute ---

    @Override
    /**
     * Konstruktor
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Layout wird zugeordnet
        setContentView(R.layout.activity_lobby_test);

        //Instanziert HttpClient, Buttons und die Lobby-Liste
        this.httpclient =  new DefaultHttpClient();
        this.button_new = (Button)findViewById(R.id.button_lobby_new);
        this.button_new.setOnClickListener(this);
        this.button_refresh = (Button)findViewById(R.id.button_refresh);
        this.button_refresh.setOnClickListener(this);
        this.lobbyNames = new ArrayList<String>();

        /*//Setzt das Wait-Flag im Controller
        Controller.getInstance().setWait(1);

        //Ruft die Infos zu allen Lobbys ab, solange die Abfrage läuft, wird gewartet
        Controller.getInstance().getLobbys();

        //Wartet auf den Controller
        while (Controller.getInstance().getWait() == 1) {}*/

        //Sobald der Controller den Wait-Wert auf 0 setzt, gehts weiter
        //Aus der Loby-Liste des Controllers werden die Lobby-Namen geholt
        for (int i = 0; i < Controller.getInstance().getLobbyList().size(); i++) {
            lobbyNames.add(Controller.getInstance().getLobbyList().get(i).getName());
        }

        //Mit der Lobby-Namen-Liste wird ein Adapter für den ListView erzeugt
        ArrayAdapter<String> lobbylistAdapter = new ArrayAdapter<>(
                        this, R.layout.list_item_lobbyliste, R.id.list_item_lobbyliste_textview, lobbyNames);

        //Instanziert den ListView
        ListView listView = (ListView) findViewById(R.id.listView);

        //Übergibt den Adapter
        listView.setAdapter(lobbylistAdapter);

        //Setzt einen Klick-Listener für die Listen-Einträge
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                //Bei Klick auf einen Listeneintrag wird zunächst der passende Lobby-Name ermittelt
                String lobbyInfo = (String) adapterView.getItemAtPosition(position);

                //Mit dem Lobby-Namen kann die ActiveLobby des Controllers gesetzt werden
                for (int i = 0; i < Controller.getInstance().getLobbyList().size(); i++) {
                    if (Controller.getInstance().getLobbyList().get(i).getName().equals(lobbyInfo)) {
                        Controller.getInstance().setActiveLobby(Controller.getInstance().getLobbyList().get(i).getId());
                    }
                }

                //Die Detail-Ansicht wird im eigenen Thread aufgerufen
                thread_detail.run();
            }
        });

        //Setzt den Usernamen als Text auf dem Screen
        final TextView mTextView = (TextView) findViewById(R.id.lobby_user_text);
        mTextView.setText("User: " + Controller.getInstance().getUser().getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lobby_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        thread_main.run();
    }

    @Override
    /**
     * Click-Listener für alle Buttons der Activity
     */
    public void onClick(View v) {

        //Button "Neu" (Erstellt eine Lobby)
        if(v.getId()==R.id.button_lobby_new) {

            //Erzeugt einen Dialog für die Eingabe des Lobby-Namens
            final Dialog newLobbyDialog = new Dialog(this);

            //Ordnet dem Dialog ein Layout zu
            newLobbyDialog.setContentView(R.layout.lobby_creator);

            //Instanziert Buttons für den Dialog
            Button btnCreate = (Button)newLobbyDialog.findViewById(R.id.btn_create);
            Button btnBack = (Button)newLobbyDialog.findViewById(R.id.btn_back);

            //Definiert einen Listener für den ZurückButton
            btnBack.setOnClickListener(new OnClickListener() {

                 @Override
                 public void onClick(View v) {

                     newLobbyDialog.dismiss();
                 }
            });

            //Definiert einen Listener für den CreateButton
            btnCreate.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    //Ermittelt den eingegebenen Text
                    TextView tvLobbyName = (TextView) newLobbyDialog.findViewById(R.id.edit_lobbyname);
                    String lobbyName = tvLobbyName.getText().toString();

                    //Holt die ID des Users (also des Lobby-Erstellers)
                    String userId = Controller.getInstance().getUser().getId();

                    //HttpResponse
                    HttpResponse response = null;

                    //Execute-String (Lobby wird erzeugt)
                    String urlnewLobby = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=newLobby&LobbyName=" + lobbyName + "&LobbyOwnerUserId=" + userId;

                    //Führt die GetFunktion aus
                    try {
                        response = httpclient.execute(new HttpGet(urlnewLobby));
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

                        //Erzeugt aus dem Antwort-String ein JSON-Objekt und daraus 2 Strings
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            String status = jsonObject.getString("validationStatus");
                            String lobbyId = jsonObject.getString("lobbyId");

                            if (status.equals("1")) {

                                //Nun wird der User der Lobby zugeordnet
                                //Execute-String
                                String urlSetUserToLobby = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=setUserToLobby&LobbyId="
                                        + lobbyId + "&UserId=" + userId + "&Owner=1";

                                //Führt die GetFunktion aus
                                try {
                                    httpclient.execute(new HttpGet(urlSetUserToLobby));

                                    //Aktualisiert die Lobby-Liste des Controllers
                                    //TODO Kann das raus?
                                    //Controller.getInstance().getLobbys();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                //Macht die neu erstellte Lobby zur ActiveLobby
                                Controller.getInstance().setActiveLobby(lobbyId);

                                //Aktualisiert die User-Daten des Controllers
                                Controller.getInstance().getUser().setCurrentLobbyId(lobbyId);
                                Controller.getInstance().getUser().setIsLobbyOwner(1);

                                //Setzt das Wait-Flag des Controllers
                                Controller.getInstance().setWait(1);

                                //Aktualisiert die Lobby-Liste des Controllers
                                Controller.getInstance().getLobbys();

                                //Wartet auf den Controller
                                while (Controller.getInstance().getWait() == 1) {
                                }

                                //Sobald der Controller den Wait-Wert auf 0 setzt, gehts weiter
                                //Die Detail-Ansicht der erstellten Lobby wird aufgerufen
                                thread_detail.run();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            //Zeigt den Dialog an
            newLobbyDialog.show();
        } else if(v.getId()==R.id.button_refresh) {
            thread_lobby.run();
        }
    }

    //Methoden zum Wechsel der Activity
    public void goToActivity_Detail(){
        Intent profileIntent = new Intent(this, LobbyDetailActivity.class);
        startActivity(profileIntent);
        this.finish();
    }
    public void goToActivity_Lobby(){
        Intent profileIntent = new Intent(this, LobbyTestActivity.class);
        startActivity(profileIntent);
        this.finish();
    }
    public void goToActivity_Main(){
        Intent profileIntent = new Intent(this, MainActivity.class);
        startActivity(profileIntent);
        this.finish();
    }

    //Threads für den Wechsel der Activity
    Thread thread_detail = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                goToActivity_Detail();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
    Thread thread_lobby = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                //Your code goes here
                goToActivity_Lobby();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
    Thread thread_main = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                //Your code goes here
                goToActivity_Main();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
}