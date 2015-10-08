package schule.adrian.montagsmaler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import Data.Data;
import controller.Controller;


/**
 * Start-Bildschirm mit Login Feldern und "Registrieren" Button
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //--- Start Attribute ---
    static Controller controller;
    private Button button_log;
    private Button button_reg;
    private EditText editText_username;
    private EditText editText_password;
    private TextView textView_inputFailed;

    //--- Ende Attribute ---

    @Override
    /**
     * Konstruktor
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Layout wird zugeordnet
        setContentView(R.layout.activity_main);

        //Instanziert den Controller
        this.controller = Controller.getInstance();

        //Ruft die getLobbys-Methode des Controllers im eigenen Thread auf
        thread_getLobbys.run();

        //Ermittelt die Bildschirmgröße und übergibt sie an den Controller
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        controller.getUser().setScreenWidth(displayMetrics.widthPixels);

        //Instanziert Buttons und Text-Felder
        this.button_log = (Button) findViewById(R.id.button_log);
        this.button_reg = (Button) findViewById(R.id.button_reg);
        this.button_log.setOnClickListener(this);
        this.button_reg.setOnClickListener(this);
        this.editText_username = (EditText) findViewById(R.id.editText_username);
        this.editText_password = (EditText) findViewById(R.id.editText_password);
        this.textView_inputFailed = (TextView) findViewById(R.id.textView_inputFailed);

        //Verhindert, dass der Internetzugriff über einen eigenen Thread laufen muss. Ggf Auslagerung in eigenen Thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        //Button "Login"
        if(v == button_log){

            //Ruft den Login-Thread auf
            thread_login.run();

        //Button "Registrieren
        } else if(v == button_reg){

            //Ruft den Thread zum Wechsel in die RegisterActivity auf
            thread_register.run();
        }
    }

    /**
     * Methode, die die Login-Daten validiert
     */
    public void requestPost() {

        //Prüft, ob Username und Passwort gefüllt wurde
        if((editText_username.getText().toString().equals("")) || (editText_password.getText().toString().equals(""))) {

            textView_inputFailed.setText("Bitte Username und Passwort eingeben");

        //Prüft, ob Leerzeichen enthalten sind
        } else if ((checkForSpace(editText_username) == true) || (checkForSpace(editText_password) == true)) {

            textView_inputFailed.setText("Aktion fehlgeschlagen: Leerstellen nicht erlaubt");

        //Wurde alles korrekt eingegeben, werden die Daten per HttpPost zur Validierung an den Server übertragen
        } else {

            HttpClient httpclient = new DefaultHttpClient();

            //HttpResponse
            HttpResponse response = null;

            //Holt die für die Post-Funktion benötigten Daten
            String user = editText_username.getText().toString();
            String pass = editText_password.getText().toString();
            String format = "json";
            String method = "validateUser";

            //Erzeugt den HttpPost
            HttpPost post = new HttpPost("http://" + Data.SERVERIP + "/MontagsMalerService/index.php");

            //Erzeugt Post-Parameter
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("Benutzername", user));
            params.add(new BasicNameValuePair("Passwort", pass));
            params.add(new BasicNameValuePair("format", format));
            params.add(new BasicNameValuePair("method", method));

            try {
                post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // writing error to Log
                e.printStackTrace();
            }

            //Führt die PostFunktion aus
            try {
                response = httpclient.execute(post);
                HttpEntity entity = response.getEntity();

                if (entity != null) {

                    //Schreibt die Antwort in einen Output Stream und erzeugt daraus einen String
                    ByteArrayOutputStream out = new ByteArrayOutputStream();

                    try {
                        entity.writeTo(out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String responseString = out.toString();

                    //Erzeugt aus dem Antwort-String ein JSON-Objekt
                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        String jsonResponse = jsonObject.getString("validationStatus");

                        //Prüft den Validation-Status
                        if (jsonResponse.equals("1")) {

                            //Setzt das Wait-Flag des Controller und übergibt dann den validierten User
                            controller.setWait(1);
                            controller.setUser(user);

                            //wartet auf Controller
                            while (controller.getWait() == 1) {
                            }

                            //Erst wenn der Controller getWait() wieder 0 setzt gehts weiter
                            goToActivity_Lobby();

                        //Schlägt die Validierung fehl, wird eine entsprechende Meldung angezeigt
                        } else {
                            textView_inputFailed.setText("Username oder Passwort nicht korrekt");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Methode, die eingegebenen Text auf Leerzeichen prüft
     * @param text
     * @return
     */
    public boolean checkForSpace(EditText text){
        Boolean booleanSpace = false;
        String checkText = text.getText().toString();

        //Prüft auf Leerzeichen
        for (int i = 0; i < checkText.length(); i++){
            if(checkText.charAt(i) == ' '){
                booleanSpace = true;
            }
        }

        //True = Leerzeichen vorhanden
        return booleanSpace;
    }

    //Methoden zum Wechsel der Activity
    public void goToActivity_Register(){
        Intent profileIntent = new Intent(this, RegisterActivity.class);
        startActivity(profileIntent);
    }
    public void goToActivity_Lobby(){
        Intent profileIntent = new Intent(this, LobbyOverviewActivity.class);
        startActivity(profileIntent);
    }

    //Threads für den Wechsel der Activity
    Thread thread_getLobbys = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                controller.getLobbys();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
    Thread thread_register = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                goToActivity_Register();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    //Threads für die Login-Methode
    Thread thread_login = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                requestPost();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
}