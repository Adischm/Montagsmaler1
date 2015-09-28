package schule.adrian.montagsmaler;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import Data.Data;
import controller.Controller;

/**
 * Activity für den Lobby-Detail Screen
 */
public class LobbyDetailActivity extends AppCompatActivity implements View.OnClickListener {

    //--- Anfnag Attribute ---
    private String lobbyName;
    private String lobbyId;

    private HttpClient httpclient;
    private ListView detailListView;

    private Button button_join;
    private Button button_leave;
    private Button button_start;

    ArrayList<String> userNames;
    ArrayList<String> tempUserNames;
    ArrayAdapter<String> lobbylistAdapter;
    //--- Ende Attribute ---

    @Override
    //Konstruktor
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Layout wird zugeordnet
        setContentView(R.layout.activity_lobby_detail);

        //Instanziert HttpClient, Buttons und Array-Listen
        this.httpclient = new DefaultHttpClient();
        this.button_join = (Button)findViewById(R.id.btn_lobbydetail_join);
        this.button_join.setOnClickListener(this);
        this.button_leave = (Button)findViewById(R.id.btn_lobbydetail_leave);
        this.button_leave.setOnClickListener(this);
        this.button_start = (Button)findViewById(R.id.btn_lobbydetail_start);
        this.button_start.setOnClickListener(this);
        userNames = new ArrayList<String>();
        tempUserNames = new ArrayList<String>();

        //Ruft die User der Lobby vom Controller ab und fügt sie der User-Namen-Liste hinzu
        for (int i = 0; i < Controller.getInstance().getLobbyList().size(); i++) {
            if (Controller.getInstance().getLobbyList().get(i).getId().equals(Controller.getInstance().getActiveLobby())) {
                lobbyName = Controller.getInstance().getLobbyList().get(i).getName();
                lobbyId = Controller.getInstance().getLobbyList().get(i).getId();

                for (int j = 0; j < Controller.getInstance().getLobbyList().get(i).getUsers().size(); j++) {
                    userNames.add(Controller.getInstance().getLobbyList().get(i).getUsers().get(j));
                }
            }
        }

        //Mit der User-Namen-Liste wird ein Adapter für den ListView erzeugt
        lobbylistAdapter = new ArrayAdapter<>(this, R.layout.list_item_lobbydetail, R.id.list_item_lobbydetail_textview, userNames);

        //Instanziert den ListView
        detailListView = (ListView) findViewById(R.id.listViewDetail);
        detailListView.setAdapter(lobbylistAdapter);

        //Setzt den Lobbynamen als Text auf dem Screen
        final TextView mTextView = (TextView) findViewById(R.id.lobbyDetailName);
        mTextView.setText("Lobby: " + lobbyName);

        //Aktiviert Buttons, abhängig davon, ob der User bereits in der Lobby ist
        if (Controller.getInstance().getUser().getCurrentLobbyId().equals(lobbyId)) {
            button_join.setEnabled(false);
            button_leave.setEnabled(true);
        } else {
            button_join.setEnabled(true);
            button_leave.setEnabled(false);
        }

        //Handelt es sich um den Lobby-Owner, dann wird auch der Start-Button sichtbar
        if (Controller.getInstance().getUser().getIsLobbyOwner() == 1) {
            button_start.setEnabled(true);
        } else {
            button_start.setEnabled(false);
            button_start.setVisibility(View.INVISIBLE);
        }

        //Instanziert einen Timer
        Timer timer = new Timer();

        //Erzeugt einen TimerTask, der kontinuierlich die User der Lobby aus der DB holt
        //Damit wird die Anzeige der zugehörigen User aktuell gehalten
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                detailListView.post(new Runnable() {
                    @Override
                    public void run() {

                        //Instanziert eine Temp-Liste
                        ArrayList<String> al = new ArrayList<String>();

                        //Fügt der Liste die Namen der zugehörigen User hinzu
                        for (int i = 0; i < Controller.getInstance().getLobbyList().size(); i++) {
                            if (Controller.getInstance().getLobbyList().get(i).getId().equals(Controller.getInstance().getActiveLobby())) {
                                lobbyName = Controller.getInstance().getLobbyList().get(i).getName();
                                lobbyId = Controller.getInstance().getLobbyList().get(i).getId();

                                for (int j = 0; j < Controller.getInstance().getLobbyList().get(i).getUsers().size(); j++) {
                                    al.add(Controller.getInstance().getLobbyList().get(i).getUsers().get(j));
                                }
                            }
                        }

                        //Ersetzt die User-Namen-Liste mit der neuen Liste
                        userNames = new ArrayList<String>(al);

                        //Falls 4 Spieler in der Lobby sind, dann wird der Join-Button deaktiviert
                        if (userNames.size() > 3) {
                            button_join.setEnabled(false);
                        }

                        //Leert den Adapter und lädt die neue Liste
                        lobbylistAdapter.clear();
                        lobbylistAdapter.addAll(userNames);

                        //Übergibt den Adapter erneut
                        detailListView.setAdapter(lobbylistAdapter);

                        //Startet den nächsten Task zum Abruf der Lobby-Daten
                        Controller.getInstance().getLobbys();
                    }
                });
            }

        //Intervall (initiale Pause, Pause zwishcne den Durchläufen
        }, 2000, 2000);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lobby_detail, menu);
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
    /**
     * Click-Listener für alle Buttons der Activity
     */
    public void onClick(View v) {

        //Button "Join"
        if(v.getId()==R.id.btn_lobbydetail_join) {

            //Fügt den User per AsyncTask der Lobby hinzu
            new JoinUserTask().execute();

            //Setzt die Buttons
            button_join.setEnabled(false);
            button_leave.setEnabled(true);

        //Button "Leave"
        } else if(v.getId()==R.id.btn_lobbydetail_leave) {

            //Prüft, ob der User, der die Lobby verlassen will, der Owner ist
            if (Controller.getInstance().getUser().getIsLobbyOwner() == 1) {

                //Falls ja, dann wird geprüft, ob noch mehr User in der Lobby sind
                if (userNames.size() > 1) {

                    //Falls ja, dann wird der erste andere User zum Owner (per AsyncTask)
                    for (int i = 0; i < userNames.size(); i++) {
                        if (!userNames.get(i).equals(Controller.getInstance().getUser().getName()) && !userNames.get(i).contains("(Owner)")) {
                            new SetOwnerTask().execute(userNames.get(i));
                            break;
                        }
                    }
                }
            }

            //Löscht den User per AsyncTask aus der Lobby
            new LeaveUserTask().execute();

            //Setzt die Buttons
            button_join.setEnabled(true);
            button_leave.setEnabled(false);
        }
    }

    /**
     * Fügt einen User per HttpGet einer Lobby hinzu
     */
    private class JoinUserTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            //Bildet den Execute String, falls die Lobby leer ist, wird der erste User automatisch zum Lobby-Owner
            String urlSetUserToLobby = "";

            if (userNames.size() > 0) {
                urlSetUserToLobby = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=setUserToLobby&LobbyId="
                        + lobbyId + "&UserId=" + Controller.getInstance().getUser().getId() + "&Owner=0";
            } else {
                urlSetUserToLobby = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=setUserToLobby&LobbyId="
                        + lobbyId + "&UserId=" + Controller.getInstance().getUser().getId() + "&Owner=1";
            }

            //Führt die GetFunktion aus
            try {
                httpclient.execute(new HttpGet(urlSetUserToLobby));

                //Aktualisiert die Lobby-Liste des Controllers
                Controller.getInstance().getLobbys();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    /**
     * Entfernt einen User per HttpGet aus einer Lobby
     */
    private class LeaveUserTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            //Execute-String
            String urlSetUserToLobby = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=deleteUserFromLobby"
                    + "&UserId=" + Controller.getInstance().getUser().getId();

            //Führt die GetFunktion aus
            try {
                httpclient.execute(new HttpGet(urlSetUserToLobby));
                Controller.getInstance().getLobbys();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    /**
     * Macht einen User per HttpGet zum Lobby-Owner
     */
    private class SetOwnerTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            //Extrahiert den übergebenen User-Namen
            String userName = strings[0];

            //Execute-String
            String urlSetOwner = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=setLobbyOwner&UserName=" + userName;

            //Führt die GetFunktion aus
            try {
                httpclient.execute(new HttpGet(urlSetOwner));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}

