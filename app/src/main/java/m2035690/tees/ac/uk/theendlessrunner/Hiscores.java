package m2035690.tees.ac.uk.theendlessrunner;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class Hiscores extends Activity {

    private static String hiscore_path;
    private static String hiscore_name = "hs.txt";
    private static Context m_context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //turn title off
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        //set to full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.hiscores);

        TextView score1 = (TextView) findViewById(R.id.score1);
        TextView score2 = (TextView) findViewById(R.id.score2);
        TextView score3 = (TextView) findViewById(R.id.score3);
        TextView score4 = (TextView) findViewById(R.id.score4);
        TextView score5 = (TextView) findViewById(R.id.score5);

        Vector<Integer> scores = loadScores();
        score1.setText(String.valueOf(scores.get(0)));
        score2.setText(String.valueOf(scores.get(1)));
        score3.setText(String.valueOf(scores.get(2)));
        score4.setText(String.valueOf(scores.get(3)));
        score5.setText(String.valueOf(scores.get(4)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_level_select, menu);
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

    public static void Initialise(Context context)
    {
        m_context = context;
        hiscore_path = context.getFilesDir().getAbsolutePath();
        File dir = new File(hiscore_path);
        dir.mkdirs();
        File file = new File(hiscore_path + "/" + hiscore_name);
        try {
            //if file not already on device, create and populate it
            if(file.createNewFile())
            {
                FileOutputStream fos = context.openFileOutput(hiscore_name, Context.MODE_PRIVATE);
                String str = String.valueOf(0) + "\n";
                for(int i = 0; i < 5; i++)
                {
                    fos.write(str.getBytes());
                }
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Vector<Integer> loadScores() {
        Vector<Integer> ret = new Vector<>();
        File file = new File(hiscore_path + "/" + hiscore_name);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null)
            {
                ret.add(Integer.parseInt(line));
            }
            br.close();
        }
        catch (IOException e) {e.printStackTrace();}
        return ret;
    }

    public static void AddNewScore(int value)
    {
        Vector<Integer> scores = loadScores();
        //check where score fits
        int index = scores.size() - 1;
        while(index >= 0 && value > scores.get(index))
        {
            index--;
        }
        //save if needs be
        if(index < scores.size() - 1)
        {
            scores.insertElementAt(value, index + 1);
            saveScores(scores);
        }
    }

    private static void saveScores(Vector<Integer> scores)
    {
        FileOutputStream fos;
        try {
            fos = m_context.openFileOutput(hiscore_name, Context.MODE_PRIVATE);
            for(int i = 0; i < scores.size(); i++)
            {
                String str = String.valueOf(scores.get(i)) + "\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onBackClick(View v)
    {
        onDestroy();
        finish();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
}
