package schule.adrian.montagsmaler;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
/*import android.app.Activity;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;*/


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    public Button button1;
    public TextView text1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text1 = (TextView) findViewById(R.id.text1);
        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(this);

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

    public void switchText(TextView text){
        if(text.getText().equals("Hello World")){
            text.setText("Bye World");
        }else{
            text.setText("Hello World");
        }

    }

    @Override
    public void onClick(View v) {
        if(v == button1){
           // switchText(text1);
           // requestTest();
            thread.run();
        }
    }

    public void requestTest(){
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = null;
        try {
            response = httpclient.execute(new HttpGet("http://192.168.43.109/HelloWorldTest/TestController.php?functionName=getLoginFromDatabase"));
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
            //..more logic
            button1.setText(responseString);
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

    Thread thread = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                //Your code goes here
                requestTest();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
}
