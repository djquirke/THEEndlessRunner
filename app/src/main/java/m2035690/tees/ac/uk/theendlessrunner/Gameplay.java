package m2035690.tees.ac.uk.theendlessrunner;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class Gameplay extends Activity {

    private GamePanel gp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //turn title off
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        //set to full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(gp = new GamePanel(this));
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        gp.onStart();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        gp.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        System.out.println("GamePanel: " + gp);
        gp.onResume();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        gp.onStop();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        gp.onRestart();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        gp.onDestroy();
    }
}
