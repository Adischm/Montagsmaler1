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
import android.widget.TextView;

import controller.Controller;
import view.DrawingView;

public class DrawActivity extends AppCompatActivity implements View.OnClickListener {

    private DrawingView drawView;
    private ImageButton button_erase;
    private Dialog infoDialog;
    private Handler handler;
    private int stopHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        this.drawView = (DrawingView)findViewById(R.id.drawing);
        this.drawView.getLayoutParams().width = Controller.getInstance().getUser().getScreenWidth();
        this.drawView.getLayoutParams().height = Controller.getInstance().getUser().getScreenWidth();
        this.button_erase = (ImageButton)findViewById(R.id.erase_btn);
        this.button_erase.setOnClickListener(this);
        this.infoDialog = new Dialog(this);
        this.stopHandler = 0;

        //Setzt das Lösungswort als Text auf dem Screen
        final TextView mTextView = (TextView) findViewById(R.id.textView_solvingWord);
        mTextView.setText("Begriff: " + Controller.getInstance().getGame().getActiveWord());

        //Ruft über den Controller einen Task auf, der alle isReadyStates der User wieder auf null setzt
        Controller.getInstance().resetGame("0");

        //Handler, der die Refresh-DrawPoints Runnable aufruft
        this.handler = new Handler();
        handler.postDelayed(refreshRunnable, 4000);
    }


private Runnable refreshRunnable = new Runnable() {
    @Override
    public void run() {

        if (Controller.getInstance().getGame().getIsSolved() == 1) {

            showInfoDialog("Es wurde gelöst!", "OK");
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

            handler.postDelayed(this, 500);
        }
    }
};

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

    @Override
    public void onClick(View view){
        //respond to clicks
        if(view.getId()==R.id.erase_btn){
            drawView.deletePainting();
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
                    infoTextView.setText("Bitte warten...");
                    Controller.getInstance().setUserReady();
                }

            }
        });

        //Zeigt den Dialog an
        infoDialog.show();
    }

    //Methoden zum Wechsel der Activity
    public void goToActivity_Draw(){
        Intent profileIntent = new Intent(this, DrawActivity.class);
        startActivity(profileIntent);
    }
    public void goToActivity_Watch(){
        Intent profileIntent = new Intent(this, WatchActivity.class);
        startActivity(profileIntent);
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
