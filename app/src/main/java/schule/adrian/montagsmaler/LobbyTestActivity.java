package schule.adrian.montagsmaler;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import controller.Controller;

public class LobbyTestActivity extends AppCompatActivity implements View.OnClickListener {

    public Button button_new;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby_test);
        button_new = (Button)findViewById(R.id.button_lobby_new);
        button_new.setOnClickListener(this);

        ArrayList<String> lobbyNames = new ArrayList<String>();

        for (int i = 0; i < Controller.getInstance().getLobbys().size(); i++) {
            lobbyNames.add(Controller.getInstance().getLobbys().get(i).getName());
        }

        ArrayAdapter<String> lobbylistAdapter = new ArrayAdapter<>(
                        this, R.layout.list_item_lobbyliste, R.id.list_item_lobbyliste_textview, lobbyNames);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(lobbylistAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String lobbyInfo = (String) adapterView.getItemAtPosition(position);

                for (int i = 0; i < Controller.getInstance().getLobbys().size(); i++) {
                    if (Controller.getInstance().getLobbys().get(i).getName().equals(lobbyInfo)) {
                        Controller.getInstance().setActiveLobby(Controller.getInstance().getLobbys().get(i).getId());
                    }
                }

                goToActivity_Detail();
            }
        });
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
    public void onClick(View v) {
        if(v.getId()==R.id.button_lobby_new) {
            //draw button clicked
            final Dialog newLobbyDialog = new Dialog(this);
            newLobbyDialog.setTitle("Lobby erstellen");
            newLobbyDialog.setContentView(R.layout.lobby_creator);

            newLobbyDialog.show();
        }
    }

    public void goToActivity_Detail(){
        Intent profileIntent = new Intent(this, LobbyDetailActivity.class);
        startActivity(profileIntent);
    }
}
