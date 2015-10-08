package schule.adrian.montagsmaler;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import Data.Data;
import controller.Controller;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static Controller controller;
    private HttpClient httpclient;
    private Button button_log;
    private Button button_reg;
    private EditText editText_username;
    private EditText editText_password;
    private TextView textView_inputFailed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.controller = Controller.getInstance();
        thread_getLobbys.run();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        controller.getUser().setScreenWidth(displayMetrics.widthPixels);

        this.httpclient = new DefaultHttpClient();

        this.button_log = (Button) findViewById(R.id.button_log);
        this.button_reg = (Button) findViewById(R.id.button_reg);
        this.editText_username = (EditText) findViewById(R.id.editText_username);
        this.editText_password = (EditText) findViewById(R.id.editText_password);
        this.textView_inputFailed = (TextView) findViewById(R.id.textView_inputFailed);

        this.button_log.setOnClickListener(this);
        this.button_reg.setOnClickListener(this);

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
    public void onClick(View v) {
        if(v == button_log){
            thread_login.run();
        }else if(v == button_reg){
            thread_register.run();
        }
    }

    public void requestPost() {
        if((editText_username.getText().toString().equals("")) || (editText_password.getText().toString().equals(""))){
            textView_inputFailed.setText("Bitte Username und Passwort eingeben");
        }else if ((checkForSpace(editText_username) == true) || (checkForSpace(editText_password) == true)){
            textView_inputFailed.setText("Aktion fehlgeschlagen: Leerstellen nicht erlaubt");
        }else {
            HttpResponse response = null;

            String user = editText_username.getText().toString();
            String pass = editText_password.getText().toString();
            String format = "json";
            String method = "validateUser";

            HttpPost post = new HttpPost("http://" + Data.SERVERIP + "/MontagsMalerService/index.php");
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

            try {
                response = httpclient.execute(post);
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    //InputStream instream = entity.getContent();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try {
                        entity.writeTo(out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String responseString = out.toString();

                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        String jsonResponse = jsonObject.getString("validationStatus");

                        if (jsonResponse.equals("1")) {

                            controller.setWait(1);
                            controller.setUser(user);

                            //wartet auf Controller
                            while (controller.getWait() == 1) {
                            }
                            //Erst wenn der Controller getWait() wieder 0 ist gehts weiter
                            goToActivity_Lobby();

                        }else{
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

    public boolean checkForSpace(EditText text){
        Boolean booleanSpace = false;
        String checkText = text.getText().toString();

        for (int i = 0; i < checkText.length(); i++){
            if(checkText.charAt(i) == ' '){
                booleanSpace = true;
            }
        }
        return booleanSpace;
    }

    public void goToActivity_Register(){
        Intent profileIntent = new Intent(this, RegisterActivity.class);
        startActivity(profileIntent);
    }
    public void goToActivity_Lobby(){
        Intent profileIntent = new Intent(this, LobbyOverviewActivity.class);
        startActivity(profileIntent);
    }
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
}