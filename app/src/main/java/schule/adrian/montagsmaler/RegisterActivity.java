package schule.adrian.montagsmaler;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

/**
 * Screen für die Registrierung neuer User
 */
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    //--- Start Attribute ---
    private EditText editText_username;
    private EditText editText_password;
    private EditText editText_password_repeat;
    private Button button_register;
    private TextView textView_inputFailed;
    private Dialog successDialog;

    //--- Ende Attribute ---

    @Override
    /**
     * Konstruktor
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Layout wird zugeordnet
        setContentView(R.layout.activity_register);

        //Instanziert Buttons, Text-Felder und Dialoge
        editText_username = (EditText) findViewById(R.id.editText_username);
        editText_password = (EditText) findViewById(R.id.editText_password);
        editText_password_repeat = (EditText) findViewById(R.id.editText_passwordRepeat);
        button_register = (Button) findViewById(R.id.button_register);
        button_register.setOnClickListener(this);
        textView_inputFailed = (TextView) findViewById(R.id.textView_inputFailed);
        successDialog = new Dialog(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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

        //Button "Registrieren"
        if(v == button_register){

            //Ruft den Register-Thread auf
            thread_registerAccount.run();
        }
    }

    /**
     * Methode die den Success-Dialog aufruft
     */
    public void showSuccessDialog() {

        //Ordnet dem Dialog ein Layout zu
        successDialog.setContentView(R.layout.register_success_dialog);

        //Instanziert einen Button für den Dialog
        final Button smallBtn = (Button)successDialog.findViewById(R.id.info_btn);

        //Definiert einen Listener für den Button
        smallBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //Ruft den Thread zum Wechsel in die MainActivity auf
                thread_main.run();
            }
        });

        //Zeigt den Dialog an
        successDialog.show();
    }

    /**
     * Methode, die die Registrierungsdaten validiert und dann per HttpPost an den Server überträgt
     */
    public void registerAccount() {

        //Prüft, ob Username und Passwort und Passwort-Wiederholung gefüllt wurde
        if((editText_username.getText().toString().equals("")) || (editText_password.getText().toString().equals("")) || (editText_password_repeat.getText().toString().equals(""))) {

            textView_inputFailed.setText("Bitte Username und Passwort eingeben");

        //Prüft, ob Leerzeichen enthalten sind
        } else if ((checkForSpace(editText_username) == true) || (checkForSpace(editText_password) == true) || (checkForSpace(editText_password_repeat))) {

            textView_inputFailed.setText("Aktion fehlgeschlagen: Leerstellen nicht erlaubt");

        //Prüft, ob Passwort und Passwort-Wiederholung identisch sind
        } else if(!editText_password.getText().toString().equals(editText_password_repeat.getText().toString())){

            textView_inputFailed.setText("Die Passwörter stimmen nicht überein!");

        //Wurde alles korrekt eingegeben, werden die Daten per HttpPost an den Server übertragen
        } else {

            HttpClient httpClient = new DefaultHttpClient();

            //HttpResponse
            HttpResponse response = null;

            //Holt die für die Post-Funktion benötigten Daten
            String user = editText_username.getText().toString();
            String pass = editText_password.getText().toString();
            String passRepeat = editText_password_repeat.getText().toString();
            String format = "json";
            String method = "newAccount";

            //Erzeugt den HttpPost
            HttpPost post = new HttpPost("http://" + Data.SERVERIP + "/MontagsMalerService/index.php");

            //Erzeugt Post-Parameter
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("LoginName", user));
            params.add(new BasicNameValuePair("LoginPasswort", pass));
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
                response = httpClient.execute(post);
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
                        String jsonStatus = jsonObject.getString("registerStatus");
                        String jsonResponse = jsonObject.getString("data");

                        //Prüft den Register-Status
                        if (jsonStatus.equals("1")){

                            //Ist alles korrekt, dann wird ein entsprechender Dialog aufgerufen
                            showSuccessDialog();

                        } else {

                            //Bei fehlerhafter Registrierung erfolgt eine Text-Ausgabe
                            textView_inputFailed.setText(jsonResponse);
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


    //Methode zum Wechsel der Activity
    public void goToActivity_Main(){
        Intent profileIntent = new Intent(this, MainActivity.class);
        startActivity(profileIntent);
        this.finish();
    }

    //Thread für den Wechsel der Activity
    Thread thread_main = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                goToActivity_Main();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    //Thread für die Register-Methode
    Thread thread_registerAccount = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                registerAccount();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
}