package schule.adrian.montagsmaler;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import controller.Controller;
import view.WatchingView;

/**
 * Die WatchActivity ist der Bildschirm zum Raten
 */
public class WatchActivity extends AppCompatActivity implements View.OnClickListener{

    //--- Start Attribute ---
    private WatchingView watchView;
    private EditText editText_solvingWord;
    private Button button_Guess;
    private Handler handler;
    private Handler startHandler;
    private Dialog infoDialog;
    private Dialog guessIsWrongDialog;
    private Dialog startDialog;
    private int stopHandler;
    private int stopWatching;
    private String word;

    //--- Ende Attribute ---

    @Override
    /**
     * Konstruktor
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setzt die Layout XML
        setContentView(R.layout.activity_watch);

        //Instanziert das Malfeld
        this.watchView = (WatchingView) findViewById(R.id.watchView);

        //Setzt die Größe des Malfelds anhand der ermittelten Bildschirm-Breite
        this.watchView.getLayoutParams().width = Controller.getInstance().getUser().getScreenWidth();
        this.watchView.getLayoutParams().height = Controller.getInstance().getUser().getScreenWidth();
        this.editText_solvingWord = (EditText) findViewById(R.id.editText_solvingWord);

        //Instanziert Buttons, Eingabefelder und Dialoge
        this.button_Guess = (Button) findViewById(R.id.button_Guess);
        this.button_Guess.setOnClickListener(this);
        this.infoDialog = new Dialog(this);
        this.guessIsWrongDialog = new Dialog(this);
        this.startDialog = new Dialog(this);

        this.stopHandler = 0;
        this.stopWatching = 0;
        this.word = "";

        //Zeigt einen Start-Dialog an, er wird per Handler automatisch geschlossen
        showStartDialog();

        //Handler, der die Refresh-DrawPoints Runnable aufruft
        this.handler = new Handler();
        handler.postDelayed(refreshRunnable, 4000);

        //Handler, der den Start-Dialog schließt
        this.startHandler = new Handler();
        startHandler.postDelayed(startRunnable, 2000);
    }

    /**
     * Runnable-Objekt das kontinuierlich aufgerufen wird
     * Hier wird geprüft ob diverse Stati ein Ereignis auslösen müssen
     */
    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {

            //Holt den aktuellen Malbegriff (für die Anzeige bei Abbruch der Runde)
            word = Controller.getInstance().getGame().getActiveWord();

            //Ruft die Methode zum Abruf der Bildpunkte auf
            if (stopWatching == 0) {
                watchPainting();
            }

            //Bei Game-Status "gelöst" wird der Info-Dialog geöffnet (falls er nicht schon angezeigt wird)
            if (Controller.getInstance().getGame().getIsSolved() == 1 && !infoDialog.isShowing()) {

                //Zeigt den Info-Dialog
                showInfoDialog("Es wurde gelöst!", "OK", 1);

                //Stoppt den Abruf der Bilddaten
                stopWatching = 1;

                //Resettet die ID des zuletzt abgerufenen Bildpunkts für die neue Runde
                Controller.getInstance().setLastPP(0);

            //IsSolved-Status 2 wird genutzt, wenn der Maler die Runde abbrechen will. Ist das der Fall, wird der Info-Dialog geöffnet
            } else if (Controller.getInstance().getGame().getIsSolved() == 2 && !infoDialog.isShowing()) {

                //Zeigt den Info-Dialog
                showInfoDialog("Es wurde abgebrochen!\nDie Lösung war: " + word, "OK", 1);

                //Stoppt den Abruf der Bilddaten
                stopWatching = 1;

                //Resettet die ID des zuletzt abgerufenen Bildpunkts für die neue Runde
                Controller.getInstance().setLastPP(0);
            }

            //Status-Prüfung, die ggf. in eine neue Activity wechselt (-> neue Runde)
            //Prüft zunächst, ob es User mit Status "Ready" gibt
            if (Controller.getInstance().getGame().getUsersReady() > 0 && infoDialog.isShowing()) {

                //Prüft dann, ob alle User "Ready" sind
                if (Controller.getInstance().getGame().getUserIds().size() == Controller.getInstance().getGame().getUsersReady()) {

                    //Falls der Info-Dialog angezeigt wird, wird er geschlossen
                    if (infoDialog.isShowing()) {
                        infoDialog.dismiss();
                    }

                    //Prüft, ob es sich beim User um den Maler der nächsten Runde handelt
                    if (Controller.getInstance().getUser().getId().equals(Controller.getInstance().getGame().getNextPainterId())) {

                        //Der nächste Maler wechselt nun in die Draw Activity
                        thread_draw.run();

                        //Stoppt den Handler
                        stopHandler = 1;

                    } else {

                        //Der Rest wechselt in die Watch Activity
                        thread_watch.run();

                        //Stoppt den Handler
                        stopHandler = 1;
                    }
                }
            }

            //Solange der Handler nicht gestoppt wurde, ruft er die RefreshRunnable erneut auf
            if (stopHandler == 0) {

                handler.postDelayed(this, 100);
            }
        }
    };

    /**
     * Runnable-Objekt, das den Start-Dialog schließt
     */
    private Runnable startRunnable = new Runnable() {
        @Override
        public void run() {

            if (startDialog.isShowing()) {
                startDialog.dismiss();
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

    //Die Zurück-Taste soll nichts machen
    @Override
    public void onBackPressed() {}

    @Override
    /**
     * Erfasst und verarbeitet Clicks auf die Buttons der Activity
     */
    public void onClick(View v) {

        //Button "Raten"
        if(v == button_Guess){

            //Holt den eingegebenen Text
            String solvingWord = editText_solvingWord.getText().toString().toLowerCase();

            //Prüft, ob der Text mit dem Lösungswort übereinstimmt
            //Falls richtig:
            if (solvingWord.equals(Controller.getInstance().getGame().getActiveWord())) {

                //Ruft über den Controller einen Task auf, der Resolved und nextPainter in der DB setzt
                Controller.getInstance().setResolved(Controller.getInstance().getUser().getId(), "1");

                //Zeigt den Info-Dialog
                showInfoDialog("Es wurde abgebrochen!\nDie Lösung war: " + word, "OK", 1);

                //Stoppt den Abruf der Bilddaten
                stopWatching = 1;

                //Resettet die ID des zuletzt abgerufenen Bildpunkts für die neue Runde
                Controller.getInstance().setLastPP(0);

            //Falls falsch:
            } else {

                //Zeigt einen entsprechenden Dialog an
                showGuessIsWrongDialog();
            }

        }
    }

    /**
     * Methode die den Info-Dialog aufruft
     * @param text
     * @param btnText
     * @param solution
     */
    public void showInfoDialog(String text, String btnText, final int solution) {

        //Ordnet dem Dialog ein Layout zu
        infoDialog.setContentView(R.layout.watch_info_dialog);

        //Instanziert eine Textanzeige
        final TextView infoTextView = (TextView)infoDialog.findViewById(R.id.tv_infotext);

        //Setzt den übergebenen Text
        infoTextView.setText(text);

        //Instanziert einen Button für den Dialog
        final Button smallBtn = (Button)infoDialog.findViewById(R.id.info_btn);

        //Setzt den übergebenen Button-Text
        smallBtn.setText(btnText);

        //Definiert einen Listener für den Button
        smallBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //Bei Click auf "OK" und Solution = 0
                if (smallBtn.getText().equals("OK") && solution == 0) {

                    //Schliesst den Dialog
                    infoDialog.dismiss();

                //Bei Click auf "OK" und Solution = 1
                } else if (smallBtn.getText().equals("OK") && solution == 1) {

                    //Setzt neue Texte
                    infoTextView.setText("Nächste Runde?");
                    smallBtn.setText("Bereit");

                //Bei Click auf "Bereit"
                } else if (smallBtn.getText().equals("Bereit")){

                    //Blendet den Button aus
                    smallBtn.setEnabled(false);
                    smallBtn.setVisibility(View.INVISIBLE);

                    //Zeigt "Bitte warten..." an
                    infoTextView.setText("Bitte warten...");

                    //Setzt den Status des Users auf "Ready"
                    Controller.getInstance().setUserReady();
                }
            }
        });

        //Zeigt den Dialog an
        infoDialog.show();
    }

    /**
     * Methode die den Dialog bei Eingabe einer falschen Lösung aufruft
     */
    public void showGuessIsWrongDialog() {

        //Ordnet dem Dialog ein Layout zu
        guessIsWrongDialog.setContentView(R.layout.watch_info_dialog);

        //Instanziert eine Textanzeige
        final TextView infoTextView = (TextView)guessIsWrongDialog.findViewById(R.id.tv_infotext);

        //Setzt den Text
        infoTextView.setText("Die Lösung ist falsch!");

        //Instanziert einen Button für den Dialog
        final Button smallBtn = (Button)guessIsWrongDialog.findViewById(R.id.info_btn);

        //Setzt den Button-Text
        smallBtn.setText("OK");

        //Definiert einen Listener für den Button
        smallBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //Schliesst den Dialog
                guessIsWrongDialog.dismiss();
            }
        });

        //Zeigt den Dialog an
        guessIsWrongDialog.show();
    }

    /**
     * Methode die den Start-Dialog aufruft
     */
    public void showStartDialog() {

        //Ordnet dem Dialog ein Layout zu
        startDialog.setContentView(R.layout.watch_start_dialog);

        //Zeigt den Dialog an
        startDialog.show();
    }

    //Methode, die über den Controller Bildpunkte aus der DB abruft
    public void watchPainting(){

        //Setzt ein Wait-Flag
        Controller.getInstance().setPictureWait(1);

        //Holt per HttpGet Bildpunkte aus der DB
        Controller.getInstance().getPictureParts();

        //Wartet, bis der Controller fertig ist
        while (Controller.getInstance().getPictureWait() == 1) {};

        //Iteriert über die Liste mit abgerufenen Bildpunkten
        for (int i = 0; i < Controller.getInstance().getpParts().size(); i++) {

            //Erzeugt aus den Daten die Koordinaten, den Event-Wert und den Farb-Wert
            float x = (Controller.getInstance().getpParts().get(i).getX() * Controller.getInstance().getUser().getScreenWidth());
            float y = (Controller.getInstance().getpParts().get(i).getY() * Controller.getInstance().getUser().getScreenWidth());
            int event = Controller.getInstance().getpParts().get(i).getEvent();
            String color = Controller.getInstance().getpParts().get(i).getColor();

            //Übergibt die Werte an die Watch-View
            watchView.paintPicture(x, y, event, color);

            //Setzt die ID als Startwert für den nächsten Abruf von Bildpunkten
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