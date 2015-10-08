package schule.adrian.montagsmaler;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import controller.Controller;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText editText_username;
    private EditText editText_password;
    private EditText editText_password_repeat;
    private Button button_register;
    private TextView textView_inputFailed;
    private Dialog successDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editText_username = (EditText) findViewById(R.id.editText_username);
        editText_password = (EditText) findViewById(R.id.editText_password);
        editText_password_repeat = (EditText) findViewById(R.id.editText_passwordRepeat);
        button_register = (Button) findViewById(R.id.button_register);
        textView_inputFailed = (TextView) findViewById(R.id.textView_inputFailed);
        successDialog = new Dialog(this);

        button_register.setOnClickListener(this);

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
    public void onClick(View v) {
        if(v == button_register){
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

                thread_main.run();

            }
        });

        //Zeigt den Dialog an
        successDialog.show();
    }

    public void registerAccount(){
        if((editText_username.getText().toString().equals("")) || (editText_password.getText().toString().equals("")) || (editText_password_repeat.getText().toString().equals(""))){
            textView_inputFailed.setText("Bitte Username und Passwort eingeben");
        }else if ((checkForSpace(editText_username) == true) || (checkForSpace(editText_password) == true) || (checkForSpace(editText_password_repeat))) {
            textView_inputFailed.setText("Aktion fehlgeschlagen: Leerstellen nicht erlaubt");
        }else if(!editText_password.getText().toString().equals(editText_password_repeat.getText().toString())){
            textView_inputFailed.setText("Die Passwörter stimmen nicht überein!");
        }else {
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = null;

            HttpPost post = new HttpPost("http://" + Data.SERVERIP + "/MontagsMalerService/index.php");

            String user = editText_username.getText().toString();
            String pass = editText_password.getText().toString();
            String passRepeat = editText_password_repeat.getText().toString();
            String format = "json";
            String method = "newAccount";

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

            try {
                response = httpClient.execute(post);
                HttpEntity entity = response.getEntity();

                if (entity != null) {

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try {
                        entity.writeTo(out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String responseString = out.toString();

                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        String jsonStatus = jsonObject.getString("registerStatus");
                        String jsonResponse = jsonObject.getString("data");

                        if (jsonStatus.equals("1")){

                            showSuccessDialog();

                        }else{
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

    public void goToActivity_Main(){
        Intent profileIntent = new Intent(this, MainActivity.class);
        startActivity(profileIntent);
        this.finish();
    }

    //Threads für den Wechsel der Activity
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

    Thread thread_registerAccount = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                //Your code goes here
                registerAccount();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
}
