package schule.adrian.montagsmaler;

import android.app.Dialog;
import android.content.Intent;
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
    private int stopHandler;
    private int stopWatching;

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
        this.stopHandler = 0;
        this.stopWatching = 0;

        //Handler, der die Refresh-DrawPoints Runnable aufruft
        this.handler = new Handler();
        handler.postDelayed(refreshRunnable, 4000);
    }

    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {

            if (stopWatching == 0) {
                watchPainting();
            }

            if (Controller.getInstance().getGame().getIsSolved() == 1 && !infoDialog.isShowing()) {

                showInfoDialog("Es wurde gelöst!", "OK", 1);
                stopWatching = 1;
                Controller.getInstance().setLastPP(0);
            }

            if (Controller.getInstance().getGame().getUsersReady() > 0) {

                if (Controller.getInstance().getGame().getUserIds().size() == Controller.getInstance().getGame().getUsersReady()) {

                    if (Controller.getInstance().getUser().getId().equals(Controller.getInstance().getGame().getNextPainterId())) {

                        //Der naächste Maler wechselt nun in die Draw View
                        thread_draw.run();

                        stopHandler = 1;

                    } else {

                        //Der Rest wechselt in die Watch View
                        thread_watch.run();
                        stopHandler = 1;
                    }
                }
            }

            if (stopHandler == 0) {

                handler.postDelayed(this, 100);
            }
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

            Log.i("FU", "VorMethode LobbyId: " + Controller.getInstance().getGame().getLobbyId());

            if (solvingWord.equals(Controller.getInstance().getGame().getActiveWord())) {

                //Ruft über den Controller einen Task auf, der Resolved und nextPainter in der DB setzt
                Controller.getInstance().setResolved();

                showInfoDialog("Die Lösung ist richtig!\nDu bist der nächste Maler", "OK", 1);

                stopWatching = 1;
                Controller.getInstance().setLastPP(0);

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

                if (smallBtn.getText().equals("OK") && decider == 0) {
                    infoDialog.dismiss();
                } else if (smallBtn.getText().equals("OK") && decider == 1) {
                    infoTextView.setText("Nächste Runde?");
                    smallBtn.setText("Bereit");
                } else if (smallBtn.getText().equals("Bereit")){
                    smallBtn.setEnabled(false);
                    smallBtn.setVisibility(View.INVISIBLE);
                    infoTextView.setText("Bitte warten...");
                    Controller.getInstance().setUserReady();
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
            String color = Controller.getInstance().getpParts().get(i).getColor();

            watchView.paintPicture(x, y, event, color);

            Controller.getInstance().setLastPP(Controller.getInstance().getpParts().get(i).getId());
        }
    }

    //Methoden zum Wechsel der Activity
    public void goToActivity_Draw(){
        Intent profileIntent = new Intent(this, DrawActivity.class);
        startActivity(profileIntent);
        this.finish();
    }
    public void goToActivity_Watch(){
        Intent profileIntent = new Intent(this, WatchActivity.class);
        startActivity(profileIntent);
        this.finish();
    }

    //Threads für den Wechsel der Activity
    Thread thread_draw = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                goToActivity_Draw();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
    Thread thread_watch = new Thread(new Runnable(){
        @Override
        public void run() {
            try {
                goToActivity_Watch();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
}