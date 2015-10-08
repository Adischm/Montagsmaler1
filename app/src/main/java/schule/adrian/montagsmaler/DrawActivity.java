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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import controller.Controller;
import view.DrawingView;

/**
 * Die DrawActivity ist der Bildschirm zum Malen
 */
public class DrawActivity extends AppCompatActivity implements View.OnClickListener {

    //--- Start Attribute ---
    private DrawingView drawView;
    private ImageButton button_erase, currPaint;
    private Button button_newword, button_cancelround;
    private Dialog infoDialog;
    private Dialog cancelDialog;
    private Dialog startDialog;
    private Handler handler;
    private Handler resetHandler;
    private Handler startHandler;
    private int stopHandler;

    //--- Ende Attribute ---

    @Override
    /**
     * Konstruktor
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setzt die Layout XML
        setContentView(R.layout.activity_draw);

        //Setzt das Bild der Start-Farbe (=schwarz) als "gedrückt"
        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
        currPaint = (ImageButton)paintLayout.getChildAt(5);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        //Instanziert das Malfeld
        this.drawView = (DrawingView)findViewById(R.id.drawing);

        //Setzt die Größe des Malfelds anhand der ermittelten Bildschirm-Breite
        this.drawView.getLayoutParams().width = Controller.getInstance().getUser().getScreenWidth();
        this.drawView.getLayoutParams().height = Controller.getInstance().getUser().getScreenWidth();

        //Instanziert Buttons und Dialoge
        this.button_erase = (ImageButton)findViewById(R.id.erase_btn);
        this.button_erase.setOnClickListener(this);
        this.button_newword = (Button) findViewById(R.id.button_newword);
        this.button_newword.setOnClickListener(this);
        this.button_cancelround = (Button) findViewById(R.id.button_cancelround);
        this.button_cancelround.setOnClickListener(this);
        this.infoDialog = new Dialog(this);
        this.cancelDialog = new Dialog(this);
        this.startDialog = new Dialog(this);

        this.stopHandler = 0;

        //Setzt das Lösungswort als Text auf dem Screen
        final TextView mTextView = (TextView) findViewById(R.id.textView_solvingWord);
        mTextView.setText("Begriff: " + Controller.getInstance().getGame().getActiveWord());

        //Zeigt einen Start-Dialog an, er wird per Handler automatisch geschlossen
        showStartDialog();

        //Handler, der die RefreshRunnable aufruft (in Intervallen)
        this.handler = new Handler();
        handler.postDelayed(refreshRunnable, 5000);

        //Handler, der die ResetGameRunnable aufruft (1x)
        this.resetHandler = new Handler();
        resetHandler.postDelayed(resetGameRunnable, 3000);

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

            //Deaktiviert den "Neues-Wort-Button", sobald mit dem Malen begonnen wurde
            if (drawView.getDrawID() > 0) {
                button_newword.setEnabled(false);
            }

            //Bei Game-Status "gelöst" wird der Info-Dialog geöffnet (falls er nicht schon angezeigt wird)
            if (Controller.getInstance().getGame().getIsSolved() == 1 && !infoDialog.isShowing()) {

                //Zeigt den Info-Dialog
                showInfoDialog("Es wurde gelöst!", "OK");

            //IsSolved-Status 2 wird genutzt, wenn der Maler die Runde abbrechen will. Ist das der Fall, wird der Info-Dialog geöffnet
            } else if (Controller.getInstance().getGame().getIsSolved() == 2 && !infoDialog.isShowing()) {

                //Schliesst den Cancel-Dialog bei Bedarf
                if (cancelDialog.isShowing()) {
                    cancelDialog.dismiss();
                }

                //Zeigt den Info-Dialog
                showInfoDialog("Nächste Runde?", "Bereit");
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
     * Runnable-Objekt, das kurz nach dem Start der Runde diverse Stati (die sonst gleich wieder Ereignisse auslösen würden) resettet
     */
    private Runnable resetGameRunnable = new Runnable() {
        @Override
        public void run() {

            //Ruft über den Controller einen Task auf, der u.a. alle isReadyStates der User wieder auf null setzt
            Controller.getInstance().resetGame("0");
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

    /**
     * Methode, die bei Click eine Farbe an die DrawView übergibt
     * @param view
     */
    public void paintClicked(View view){

        if(view!=currPaint){

            ImageButton imgView = (ImageButton)view;

            //Holt den Farbcode vom ImageButton
            String color = view.getTag().toString();

            //Übergibt die Farbe
            drawView.setColor(color);

            //Setzt das Bild der Farbe als "gedrückt"
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint=(ImageButton)view;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_draw, menu);
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
    public void onClick(View view){

        //Button "Löschen"
        if(view.getId()==R.id.erase_btn){

            //Löscht das Bild in der >View
            drawView.deletePainting();

        //Button "Neues Wort"
        } else if(view.getId()==R.id.button_newword){

            //Deaktiviert den Button (-> darf nur 1x pro Runde gemacht werden)
            button_newword.setEnabled(false);

            //Holt über den Controller ein neues Wort
            Controller.getInstance().setWordWait(1);
            Controller.getInstance().updateWord();

            //Wartet, bis der Controller das neue Wort per HttpGet aus der DB geholt hat
            while (Controller.getInstance().getWordWait() == 1) {}

            //Setzt das Lösungswort als Text auf dem Screen
            final TextView mTextView = (TextView) findViewById(R.id.textView_solvingWord);
            mTextView.setText("Begriff: " + Controller.getInstance().getGame().getActiveWord());

        //Button "Abbrechen"
        } else if(view.getId()==R.id.button_cancelround){

            //Öffnet den Abbrechen-Dialog
            showCancelDialog();
        }
    }

    /**
     * Methode die den Info-Dialog aufruft
     * @param text
     * @param btnText
     */
    public void showInfoDialog(String text, String btnText) {

        //Ordnet dem Dialog ein Layout zu
        infoDialog.setContentView(R.layout.draw_info_dialog);

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

                //Bei Click auf "OK"
                if (smallBtn.getText().equals("OK")) {

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

                    //Löscht die Bild-Daten des Games
                    Controller.getInstance().truncateCoordinates();

                    //Setzt den Status des Users auf "Ready"
                    Controller.getInstance().setUserReady();
                }

            }
        });

        //Zeigt den Dialog an
        infoDialog.show();
    }

    /**
     * Methode die den Abbrechen-Dialog aufruft
     */
    public void showCancelDialog() {

        //Ordnet dem Dialog ein Layout zu
        cancelDialog.setContentView(R.layout.draw_cancel_dialog);

        //Instanziert eine Textanzeige
        final TextView infoTextView = (TextView)cancelDialog.findViewById(R.id.tv_infotext);

        //Instanziert Buttons für den Dialog
        final Button yesButton = (Button)cancelDialog.findViewById(R.id.yes_btn);
        final Button noButton = (Button)cancelDialog.findViewById(R.id.no_btn);

        //Definiert einen Listener für den Ja-Button
        yesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //Erzeugt eine Zufallszahl mit max = Anzahl User
                //Mit dieser Zahl wird der nächste Maler festgelegt
                int randomInt = (int)(Math.random() * Controller.getInstance().getGame().getUserIds().size());

                //Übergibt den GameActive Status und nextPainter an die DB
                for (int i = 0; i < Controller.getInstance().getGame().getUserIds().size(); i++) {

                    //Bei match mit der Zufallszahl: isPainter = 1 ...
                    if (i == randomInt) {

                        //Ruft über den Controller einen Task auf, der Resolved und nextPainter in der DB setzt
                        Controller.getInstance().setResolved(Controller.getInstance().getGame().getUserIds().get(i), "2");

                    }
                }

                //Zeigt "Bitte warten..." an
                infoTextView.setText("Bitte warten...");

                //Blendet die Buttons aus
                yesButton.setVisibility(View.INVISIBLE);
                noButton.setVisibility(View.INVISIBLE);
            }
        });

        //Definiert einen Listener für den Nein-Button
        noButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //Schliesst den Dialog
                cancelDialog.dismiss();
            }
        });

        //Zeigt den Dialog an
        cancelDialog.show();
    }

    /**
     * Methode die den Start-Dialog aufruft
     */
    public void showStartDialog() {

        //Ordnet dem Dialog ein Layout zu
        startDialog.setContentView(R.layout.draw_start_dialog);

        //Zeigt den Dialog an
        startDialog.show();
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