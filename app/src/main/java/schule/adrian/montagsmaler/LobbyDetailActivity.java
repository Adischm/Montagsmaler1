package schule.adrian.montagsmaler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import controller.Controller;

public class LobbyDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby_detail);

        ArrayList<String> userNames = new ArrayList<String>();

        for (int i = 0; i < Controller.getInstance().getLobbys().size(); i++) {
            if (Controller.getInstance().getLobbys().get(i).getId().equals(Controller.getInstance().getActiveLobby())) {
                for (int j = 0; j < Controller.getInstance().getLobbys().get(i).getUsers().size(); j++) {
                    userNames.add(Controller.getInstance().getLobbys().get(i).getUsers().get(j));
                }
            }
        }

        ArrayAdapter<String> lobbylistAdapter = new ArrayAdapter<>(
                this, R.layout.list_item_lobbydetail, R.id.list_item_lobbydetail_textview, userNames);

        ListView detailListView = (ListView) findViewById(R.id.listViewDetail);
        detailListView.setAdapter(lobbylistAdapter);
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
}
