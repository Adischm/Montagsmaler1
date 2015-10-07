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

public class DrawActivity extends AppCompatActivity implements View.OnClickListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
        currPaint = (ImageButton)paintLayout.getChildAt(5);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        this.drawView = (DrawingView)findViewById(R.id.drawing);
        this.drawView.getLayoutParams().width = Controller.getInstance().getUser().getScreenWidth();
        this.drawView.getLayoutParams().height = Controller.getInstance().getUser().getScreenWidth();
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
        handler.postDelayed(refreshRunnable, 4000);

        //Handler, der die ResetGameRunnable aufruft (1x)
        this.resetHandler = new Handler();
        resetHandler.postDelayed(resetGameRunnable, 2000);

        //Handler, der den Start-Dialog schließt
        this.startHandler = new Handler();
        startHandler.postDelayed(startRunnable, 2000);
    }


    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {

            if (drawView.getDrawID() > 0) {
                button_newword.setEnabled(false);
            }

            if (Controller.getInstance().getGame().getIsSolved() == 1 && !infoDialog.isShowing()) {

                showInfoDialog("Es wurde gelöst!", "OK");
            } else if (Controller.getInstance().getGame().getIsSolved() == 2 && !infoDialog.isShowing()) {

                if (cancelDialog.isShowing()) {
                    cancelDialog.dismiss();
                }

                showInfoDialog("Nächste Runde?", "Bereit");
            }

            if (Controller.getInstance().getGame().getUsersReady() > 0) {

                if (Controller.getInstance().getGame().getUserIds().size() == Controller.getInstance().getGame().getUsersReady()) {

                    if (infoDialog.isShowing()) {
                        infoDialog.dismiss();
                    }

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

    private Runnable resetGameRunnable = new Runnable() {
        @Override
        public void run() {

            //Ruft über den Controller einen Task auf, der alle isReadyStates der User wieder auf null setzt
            Controller.getInstance().resetGame("0");
        }
    };

    private Runnable startRunnable = new Runnable() {
        @Override
        public void run() {

            if (startDialog.isShowing()) {
                startDialog.dismiss();
            }
        }
    };

    public void paintClicked(View view){
        //use chosen color
        if(view!=currPaint){
            //update color
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();
            drawView.setColor(color);
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
    public void onClick(View view){
        //respond to clicks
        if(view.getId()==R.id.erase_btn){

            drawView.deletePainting();

        } else if(view.getId()==R.id.button_newword){

            button_newword.setEnabled(false);

            Controller.getInstance().setWordWait(1);
            Controller.getInstance().updateWord();

            while (Controller.getInstance().getWordWait() == 1) {}

            //Setzt das Lösungswort als Text auf dem Screen
            final TextView mTextView = (TextView) findViewById(R.id.textView_solvingWord);
            mTextView.setText("Begriff: " + Controller.getInstance().getGame().getActiveWord());

        } else if(view.getId()==R.id.button_cancelround){

            showCancelDialog();
        }
    }

    public void showInfoDialog(String text, String btnText) {

        //Ordnet dem Dialog ein Layout zu
        infoDialog.setContentView(R.layout.draw_info_dialog);

        final TextView infoTextView = (TextView)infoDialog.findViewById(R.id.tv_infotext);
        infoTextView.setText(text);

        //Instanziert einen Button für den Dialog
        final Button smallBtn = (Button)infoDialog.findViewById(R.id.info_btn);
        smallBtn.setText(btnText);

        //Definiert einen Listener für den Button
        smallBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (smallBtn.getText().equals("OK")) {

                    infoTextView.setText("Nächste Runde?");
                    smallBtn.setText("Bereit");

                } else if (smallBtn.getText().equals("Bereit")){
                    smallBtn.setEnabled(false);
                    smallBtn.setVisibility(View.INVISIBLE);
                    infoTextView.setText("Bitte warten...");

                    Controller.getInstance().truncateCoordinates();

                    Controller.getInstance().setUserReady();
                }

            }
        });

        //Zeigt den Dialog an
        infoDialog.show();
    }

    public void showCancelDialog() {

        //Ordnet dem Dialog ein Layout zu
        cancelDialog.setContentView(R.layout.draw_cancel_dialog);

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

                infoTextView.setText("Bitte warten...");
                yesButton.setVisibility(View.INVISIBLE);
                noButton.setVisibility(View.INVISIBLE);
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                cancelDialog.dismiss();
            }
        });

        //Zeigt den Dialog an
        cancelDialog.show();
    }

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
