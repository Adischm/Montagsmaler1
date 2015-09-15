package schule.adrian.montagsmaler;

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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    public EditText editText_username;
    public EditText editText_password;
    public EditText editText_password_repeat;
    public Button button_register;
    public TextView textView_inputFailed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editText_username = (EditText) findViewById(R.id.editText_username);
        editText_password = (EditText) findViewById(R.id.editText_password);
        editText_password_repeat = (EditText) findViewById(R.id.editText_passwordRepeat);
        button_register = (Button) findViewById(R.id.button_register);
        textView_inputFailed = (TextView) findViewById(R.id.textView_inputFailed);

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

    public void registerAccount(){
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = null;
        HttpPost post = new HttpPost("http://192.168.43.226/MontagsMalerService/index.php");

        String user = editText_username.getText().toString();
        String userDisplayname = user;
        String pass = editText_password.getText().toString();
        String passRepeat = editText_password_repeat.getText().toString();
        String mail = "test@mail.com";
        String format = "json";
        String method = "newAccount";

        if (!pass.equals(passRepeat)){
            textView_inputFailed.setText("Die Passwörter stimmen nicht überein!");
        }else{
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("AnzeigeName", userDisplayname));
            params.add(new BasicNameValuePair("LoginName", user));
            params.add(new BasicNameValuePair("LoginEmail", mail));
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
                        String jsonResponse = jsonObject.getString("data");
                        textView_inputFailed.setText(jsonResponse);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

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
