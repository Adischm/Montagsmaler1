package schule.adrian.montagsmaler;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import controller.Controller;
import view.WatchingView;

public class WatchActivity extends AppCompatActivity implements View.OnClickListener{

    private WatchingView watchView;
    private EditText editText_solvingWord;
    private Button button_Guess;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);
        watchView = (WatchingView) findViewById(R.id.watchView);
        watchView.getLayoutParams().width = Controller.getInstance().getUser().getScreenWidth();
        watchView.getLayoutParams().height = Controller.getInstance().getUser().getScreenWidth();
        editText_solvingWord = (EditText) findViewById(R.id.editText_solvingWord);
        button_Guess = (Button) findViewById(R.id.button_Guess);

        //Handler, der die Refresh-Runnable aufruft
        handler = new Handler();
        handler.postDelayed(runnable, 500);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            watchPainting();

            handler.postDelayed(this, 10);
        }
    };

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

    @Override
    public void onClick(View v) {
        if(v == button_Guess){
            String solvingWord = String.valueOf(editText_solvingWord.getText());
            //TODO Senden des Lösungswortes
        }
    }


    public void watchPainting(){
        Controller.getInstance().setPictureWait(1);
        Controller.getInstance().getPictureParts();

        while(Controller.getInstance().getPictureWait() == 1){

        }

        for (int i = 0; i < Controller.getInstance().getpParts().size(); i++) {

            float x = (Controller.getInstance().getpParts().get(i).getX() * Controller.getInstance().getUser().getScreenWidth());
            float y = (Controller.getInstance().getpParts().get(i).getY() * Controller.getInstance().getUser().getScreenWidth());
            int event = Controller.getInstance().getpParts().get(i).getEvent();

            watchView.paintPicture(x, y, event);

            Controller.getInstance().setLastPP(Controller.getInstance().getpParts().get(i).getId());
        }
    }
}