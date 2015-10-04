package schule.adrian.montagsmaler;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import controller.Controller;
import view.DrawingView;

public class DrawActivity extends AppCompatActivity implements View.OnClickListener {

    private DrawingView drawView;
    private ImageButton button_erase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        this.drawView = (DrawingView)findViewById(R.id.drawing);
        this.drawView.getLayoutParams().width = Controller.getInstance().getUser().getScreenWidth();
        this.drawView.getLayoutParams().height = Controller.getInstance().getUser().getScreenWidth();
        this.button_erase = (ImageButton)findViewById(R.id.erase_btn);
        this.button_erase.setOnClickListener(this);

        //Setzt das Lösungswort als Text auf dem Screen
        final TextView mTextView = (TextView) findViewById(R.id.textView_solvingWord);
        mTextView.setText("Begriff: " + Controller.getInstance().getGame().getActiveWord());
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

    @Override
    public void onClick(View view){
        //respond to clicks
        if(view.getId()==R.id.erase_btn){
            drawView.deletePainting();
        }
    }


}
