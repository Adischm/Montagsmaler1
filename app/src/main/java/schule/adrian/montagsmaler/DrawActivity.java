package schule.adrian.montagsmaler;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import controller.Controller;
import view.DrawingView;

public class DrawActivity extends AppCompatActivity implements View.OnClickListener {

    private DrawingView drawView;
    private ImageButton button_erase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        drawView = (DrawingView)findViewById(R.id.drawing);
        drawView.getLayoutParams().width = Controller.getInstance().getUser().getScreenWidth();
        drawView.getLayoutParams().height = Controller.getInstance().getUser().getScreenWidth();
        button_erase = (ImageButton)findViewById(R.id.erase_btn);
        button_erase.setOnClickListener(this);
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
