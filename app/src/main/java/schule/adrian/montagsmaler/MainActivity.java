package schule.adrian.montagsmaler;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    public Button button_log;
    public Button button_reg;
    public TextView textview_user;
    public TextView textview_pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button_log = (Button) findViewById(R.id.button_log);
        button_reg = (Button) findViewById(R.id.button_reg);
        textview_user = (TextView) findViewById(R.id.username);
        textview_pass = (TextView) findViewById(R.id.password);
        button_log.setOnClickListener(this);
        button_reg.setOnClickListener(this);

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

    public void requestGetTest(){
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = null;

        CharSequence user = textview_user.getText();
        CharSequence pw = textview_pass.getText();

        try {
            response = httpclient.execute(new HttpGet("http://192.168.43.226/MontagsMalerService/index.php?" +
                                                    "format=json&method=validateUser&Benutzername=" +
                                                    user +
                                                    "&Passwort=" +
                                                    pw));
        } catch (IOException e) {
            e.printStackTrace();
        }

        StatusLine statusLine = response.getStatusLine();
        if(statusLine.getStatusCode() == HttpStatus.SC_OK){
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
                String jsonResponse = jsonObject.getString("code");
                button_log.setText(jsonResponse);
                /*if (jsonResponse.equals("1")) {

                    goToActivity_Lobby();

                }*/
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else{
            //Closes the connection.
            try {
                response.getEntity().getContent().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                throw new IOException(statusLine.getReasonPhrase());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void requestPostTest() {
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = null;

        String user = textview_user.getText().toString();
        String pass = textview_pass.getText().toString();
        String format = "json";
        String method = "validateUser";

        HttpPost post = new HttpPost("http://192.168.43.226/MontagsMalerService/index.php");
        //HttpPost post = new HttpPost("http://postcatcher.in/catchers/55f160782dea750300000518");

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
            response = httpClient.execute(post);
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
                button_log.setText(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goToActivity_Register(){
        Intent profileIntent = new Intent(this, RegisterActivity.class);
        startActivity(profileIntent);
    }

    public void goToActivity_Lobby(){
        Intent profileIntent = new Intent(this, LobbyTestActivity.class);
        startActivity(profileIntent);
    }

    Thread thread_login = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                //Your code goes here
                //requestGetTest();
                requestPostTest();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    Thread thread_register = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                //Your code goes here
                goToActivity_Register();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
}