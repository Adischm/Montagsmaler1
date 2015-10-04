package schule.adrian.montagsmaler;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import controller.Controller;
import view.WatchingView;

public class WatchActivity extends AppCompatActivity implements View.OnClickListener{

    private WatchingView watchView;
    private EditText editText_solvingWord;
    private Button button_Guess;
    private Handler handler;
    private Dialog infoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        this.watchView = (WatchingView) findViewById(R.id.watchView);
        this.watchView.getLayoutParams().width = Controller.getInstance().getUser().getScreenWidth();
        this.watchView.getLayoutParams().height = Controller.getInstance().getUser().getScreenWidth();
        this.editText_solvingWord = (EditText) findViewById(R.id.editText_solvingWord);
        this.button_Guess = (Button) findViewById(R.id.button_Guess);
        this.button_Guess.setOnClickListener(this);
        this.infoDialog = new Dialog(this);

        //Handler, der die Refresh-DrawPoints Runnable aufruft
        this.handler = new Handler();
        handler.postDelayed(refreshRunnable, 500);
    }

    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            watchPainting();
            handler.postDelayed(this, 100);
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
            String solvingWord = editText_solvingWord.getText().toString().toLowerCase();

            Log.i("FU", "Lösung: " + solvingWord);

            if (solvingWord.equals(Controller.getInstance().getGame().getActiveWord())) {

                showInfoDialog("Die Lösung ist richtig!" + System.getProperty ("line.separator") + "Du bist der nächste Maler", "OK", 1);

            } else {

                showInfoDialog("Die Lösung ist falsch!", "OK", 0);
            }

        }
    }

    public void showInfoDialog(String text, String btnText, final int solution) {

        //Ordnet dem Dialog ein Layout zu
        infoDialog.setContentView(R.layout.watch_info_dialog);

        final TextView infoTextView = (TextView)infoDialog.findViewById(R.id.tv_infotext);
        infoTextView.setText(text);

        //Instanziert einen Button für den Dialog
        final Button smallBtn = (Button)infoDialog.findViewById(R.id.info_btn);
        smallBtn.setText(btnText);

        //Definiert einen Listener für den Button
        smallBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int decider = solution;

                if (decider == 0) {
                    infoDialog.dismiss();
                } else if (decider == 1) {
                    infoTextView.setText("Bereit für die nächste Runde?");
                    smallBtn.setText("Bereit");
                    decider = 2;
                } else if (decider == 2){

                }

            }
        });

        //Zeigt den Dialog an
        infoDialog.show();
    }

    public void watchPainting(){

        Controller.getInstance().setPictureWait(1);
        Controller.getInstance().getPictureParts();

        while (Controller.getInstance().getPictureWait() == 1) {};

        for (int i = 0; i < Controller.getInstance().getpParts().size(); i++) {

            float x = (Controller.getInstance().getpParts().get(i).getX() * Controller.getInstance().getUser().getScreenWidth());
            float y = (Controller.getInstance().getpParts().get(i).getY() * Controller.getInstance().getUser().getScreenWidth());
            int event = Controller.getInstance().getpParts().get(i).getEvent();

            watchView.paintPicture(x, y, event);

            Controller.getInstance().setLastPP(Controller.getInstance().getpParts().get(i).getId());
        }
    }
}