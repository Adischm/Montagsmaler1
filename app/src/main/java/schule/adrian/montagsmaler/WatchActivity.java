package schule.adrian.montagsmaler;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Timer;
import java.util.TimerTask;

import controller.Controller;
import view.WatchingView;

public class WatchActivity extends AppCompatActivity{

    private WatchingView watchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);
        watchView = (WatchingView)findViewById(R.id.watchView);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                watchView.post(new Runnable() {
                    @Override
                    public void run() {
                        watchPainting();
                    }
                });
            }
        }, 500, 500);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_watch, menu);
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

    Timer timer = new Timer();

    public void watchPainting(){
        Controller.getInstance().getPictureParts();

        for (int i = 0; i < Controller.getInstance().getpParts().size(); i++) {

            float x = Controller.getInstance().getpParts().get(i).getX();
            float y = Controller.getInstance().getpParts().get(i).getY();
            int event = Controller.getInstance().getpParts().get(i).getEvent();

            watchView.paintPicture(x, y, event);

            Controller.getInstance().setLastPP(Controller.getInstance().getpParts().get(i).getId());
        }
    }


}