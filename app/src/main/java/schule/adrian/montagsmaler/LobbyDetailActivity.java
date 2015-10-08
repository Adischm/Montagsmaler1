package schule.adrian.montagsmaler;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.ArrayList;

import Data.Data;
import controller.Controller;

/**
 * Activity für den Lobby-Detail Screen
 */
public class LobbyDetailActivity extends AppCompatActivity implements View.OnClickListener {

    //--- Anfang Attribute ---
    private String lobbyName;
    private String lobbyId;
    private int stopHandler;
    private Handler handler;
    private ListView detailListView;
    private Dialog gameStartsDialog;
    private TextView mTextView;
    private Button button_join;
    private Button button_leave;
    private Button button_start;
    private ArrayList<String> userNames;
    private ArrayList<String> tempUserNames;
    private ArrayAdapter<String> lobbylistAdapter;

    //--- Ende Attribute ---

    @Override
    /**
     * Konstruktor
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Layout wird zugeordnet
        setContentView(R.layout.activity_lobby_detail);

        //Instanziert Buttons und Array-Listen
        this.button_join = (Button)findViewById(R.id.btn_lobbydetail_join);
        this.button_join.setOnClickListener(this);
        this.button_leave = (Button)findViewById(R.id.btn_lobbydetail_leave);
        this.button_leave.setOnClickListener(this);
        this.button_start = (Button)findViewById(R.id.btn_lobbydetail_start);
        this.button_start.setOnClickListener(this);
        this.userNames = new ArrayList<String>();
        this.tempUserNames = new ArrayList<String>();

        this.stopHandler = 0;

        //Dialog, der beim GameStart angezeigt wird
        this.gameStartsDialog = new Dialog(this);

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
        mTextView = (TextView) findViewById(R.id.lobbyDetailName);
        mTextView.setText("Lobby: " + lobbyName);

        //Aktiviert Buttons, abhängig davon, ob der User bereits in der Lobby ist
        if (Controller.getInstance().getUser().getCurrentLobbyId().equals(lobbyId)) {
            button_join.setEnabled(false);
            button_leave.setEnabled(true);

            //Handelt es sich um den Lobby-Owner, dann wird auch der Start-Button sichtbar
            if (Controller.getInstance().getUser().getIsLobbyOwner() == 1) {

                //Aber nnur, wenn mindestens 2 Spieler in der Lobby sind
                if (userNames.size() > 1) {
                    button_start.setEnabled(true);
                } else {
                    button_start.setEnabled(false);
                }
            } else {
                button_start.setEnabled(false);
                button_start.setVisibility(View.INVISIBLE);
            }

        } else {
            button_join.setEnabled(true);
            button_leave.setEnabled(false);
            button_start.setEnabled(false);
            button_start.setVisibility(View.INVISIBLE);
        }

        //Handler, der die Refresh-Runnable aufruft
        handler = new Handler();
        handler.postDelayed(refreshRunnable, 2000);
    }

    /**
     * Runnable-Objekt das kontinuierlich aufgerufen wird
     * Hier wird geprüft ob diverse Stati ein Ereignis auslösen müssen
     * Zudem wird die User-Liste aktualisiert
     */
    private Runnable refreshRunnable = new Runnable() {
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

            //Leert den Adapter und lädt die neue Liste
            lobbylistAdapter.clear();
            lobbylistAdapter.addAll(userNames);

            //Übergibt den Adapter erneut
            detailListView.setAdapter(lobbylistAdapter);

            //Falls 4 Spieler in der Lobby sind, dann wird der Join-Button deaktiviert
            if (userNames.size() > 3) {
                button_join.setEnabled(false);
            }

            //Wurde der GameActive Status vom Lobby-Owner auf 2 gesetzt, dann wird der Start-Dialog eingeblendet,
            //Sofern er noch nicht bereits sichtbar ist
            if (!gameStartsDialog.isShowing()) {

                if (Controller.getInstance().getUser().getGameActive() == 2) {

                    //Übergibt vor dem Start die aktuelle Lobby ans Game-Objekt
                    Controller.getInstance().getGame().setLobbyId(lobbyId);

                    //Aktiviert die Refresh-Schleife für das Game im Controller
                    Controller.getInstance().setRefreshGame(1);

                    //Zeigt den Dialog an
                    showStartDialog();
                }
            }

            //Status-Prüfung, die ggf. in eine neue Activity wechselt (-> neue Runde)
            //Prüft zunächst, ob es User mit Status "Ready" gibt
            if (Controller.getInstance().getGame().getUsersReady() > 0) {

                //Prüft dann, ob alle User "Ready" sind
                if (Controller.getInstance().getGame().getUserIds().size() == Controller.getInstance().getGame().getUsersReady()) {

                    //Falls der Start-Dialog angezeigt wird, wird er geschlossen
                    if (gameStartsDialog.isShowing()) {
                        gameStartsDialog.dismiss();
                    }

                    //Prüft, ob es sich beim User um den Maler der ersten Runde handelt
                    if (Controller.getInstance().getUser().getIsPainter() == 1) {

                        //Der Maler setzt für alle Spieler GameActive auf 1
                        String[] array = {lobbyId, "1"};
                        Controller.getInstance().setGameActive(array);

                        //Der Maler wechselt in die Draw View
                        thread_draw.run();

                        //Stoppt den Handler
                        stopHandler = 1;

                    } else {

                        //Der Rest wechselt in die Watch View
                        thread_watch.run();

                        //Stoppt den Handler
                        stopHandler = 1;
                    }
                }
            }

            //Handelt es sich um den Lobby-Owner, dann wird auch der Start-Button sichtbar
            if (Controller.getInstance().getUser().getIsLobbyOwner() == 1) {

                if (userNames.size() > 1) {
                    button_start.setEnabled(true);
                } else {
                    button_start.setEnabled(false);
                }
            }

            //Solange der Handler nicht gestoppt wurde, ruft er die RefreshRunnable erneut auf
            if (stopHandler == 0) {

                handler.postDelayed(this, 500);
            }
        }
    };

    /**
     * Methode die den Start-Dialog aufruft
     */
    public void showStartDialog() {

        //Ordnet dem Dialog ein Layout zu
        gameStartsDialog.setContentView(R.layout.gamestart_waiter);

        //Instanziert einen Button für den Dialog
        final Button smallBtn = (Button)gameStartsDialog.findViewById(R.id.btn_ready);

        //Definiert einen Listener für den Button
        smallBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //Setzt den Status des User-Objekts auf "Ready"
                Controller.getInstance().getUser().setIsReady(1);

                //Setzt den "Bitte warten" Text
                final TextView mTextView = (TextView) gameStartsDialog.findViewById(R.id.tv_gamestart);
                mTextView.setText("Bitte warten...");

                //Blendet den Button aus
                smallBtn.setEnabled(false);
                smallBtn.setVisibility(View.INVISIBLE);

                //Übergibt den Ready-Status an die DB
                Controller.getInstance().setUserReady();
            }
        });

        //Zeigt den Dialog an
        gameStartsDialog.show();
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
     * Die Zurück-Taste ruft die Lobby-Übersichts-Activity auf
     */
    public void onBackPressed() {

        thread_lobby.run();
    }

    @Override
    /**
     * Click-Listener für alle Buttons der Activity
     */
    public void onClick(View v) {

        //Button "Join"
        if(v.getId()==R.id.btn_lobbydetail_join) {

            //Sind keine anderen User in der Lobby, dann wird der Joiner zum Owner
            if (userNames.size() == 0) {
                Controller.getInstance().getUser().setIsLobbyOwner(1);
            }

            //Fügt den User per AsyncTask der Lobby hinzu
            new JoinUserTask().execute();

            //Setzt die Buttons
            button_join.setEnabled(false);
            button_leave.setEnabled(true);

            //Handelt es sich beim User um den Lobby-Owner, dann wird der Start-Button sichtbar
            if (Controller.getInstance().getUser().getIsLobbyOwner() == 1) {
                button_start.setEnabled(true);
                button_start.setVisibility(View.VISIBLE);
            } else {
                button_start.setEnabled(false);
                button_start.setVisibility(View.INVISIBLE);
            }

        //Button "Leave"
        } else if(v.getId()==R.id.btn_lobbydetail_leave) {

            //Löscht die LobbyId aus dem User-Objekt
            Controller.getInstance().getUser().setCurrentLobbyId("");

            //Prüft, ob der User, der die Lobby verlassen will, der Owner ist
            if (Controller.getInstance().getUser().getIsLobbyOwner() == 1) {

                //Falls ja, dann wird die OwnerInfo an den Controller übergeben
                Controller.getInstance().getUser().setIsLobbyOwner(0);

                //Prüft ob weitere User in der Lobby sind
                if (userNames.size() > 1) {

                    //Falls ja, dann wird der erste andere User zum Owner (per AsyncTask)
                    for (int i = 0; i < userNames.size(); i++) {
                        if (!userNames.get(i).equals(Controller.getInstance().getUser().getName()) && !userNames.get(i).contains("(Owner)")) {
                            new SetOwnerTask().execute(userNames.get(i));

                            //Unterbricht die Schleife
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
            button_start.setEnabled(false);
            button_start.setVisibility(View.INVISIBLE);

        //Button "Start"
        } else if(v.getId()==R.id.btn_lobbydetail_start) {

            //Erstellt über den Controller ein Game in der DB und setzt die Stati der Spieler,
            //sodass bei allen Spielern der Start-Dialog angezeigt wird
            Controller.getInstance().createGame(lobbyId);
        }
    }

    //Methoden zum Wechsel der Activity
    public void goToActivity_Draw(){
        Intent profileIntent = new Intent(this, DrawActivity.class);
        startActivity(profileIntent);
        this.finish();
    }
    public void goToActivity_Watch(){
        Intent profileIntent = new Intent(this, WatchActivity.class);
        startActivity(profileIntent);
        this.finish();
    }
    public void goToActivity_Lobby(){
        Intent profileIntent = new Intent(this, LobbyOverviewActivity.class);
        startActivity(profileIntent);
        //this.finish();
    }

    //Threads für den Wechsel der Activity
    Thread thread_draw = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                goToActivity_Draw();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
    Thread thread_watch = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                goToActivity_Watch();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
    Thread thread_lobby = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                goToActivity_Lobby();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    //--- Anfang Tasks ---

    /**
     * Fügt einen User per HttpGet einer Lobby hinzu
     */
    private class JoinUserTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            HttpClient httpclient = new DefaultHttpClient();

            //Bildet den Execute String, falls die Lobby leer ist, wird der erste User automatisch zum Lobby-Owner
            String urlSetUserToLobby = "";

            if (userNames.size() > 0) {
                urlSetUserToLobby = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=setUserToLobby&LobbyId="
                        + lobbyId + "&UserId=" + Controller.getInstance().getUser().getId() + "&Owner=0";
            } else {
                urlSetUserToLobby = "http://" + Data.SERVERIP + "/MontagsMalerService/index.php?format=json&method=setUserToLobby&LobbyId="
                        + lobbyId + "&UserId=" + Controller.getInstance().getUser().getId() + "&Owner=1";

                //Übergibt LobbyOwner Info an Controller
                Controller.getInstance().getUser().setIsLobbyOwner(1);
            }

            //Führt die GetFunktion aus
            try {
                httpclient.execute(new HttpGet(urlSetUserToLobby));
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

            HttpClient httpclient = new DefaultHttpClient();

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

            HttpClient httpclient = new DefaultHttpClient();

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

    //--- Ende Tasks ---
}