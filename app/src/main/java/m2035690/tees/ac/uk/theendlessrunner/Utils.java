package m2035690.tees.ac.uk.theendlessrunner;

/**
 * Created by Dan on 27/02/2016.
 */
public class Utils {
    public static int dipToPix(int dip) { return (int)(dip * GamePanel.DENSITY + 0.5f); }
    public static float dipToPix(float dip) { return dip * GamePanel.DENSITY + 0.5f; }

    public static int pixToDip(int pix) { return (int)((pix - 0.5f) / GamePanel.DENSITY); }
    public static float pixToDip(float pix) { return (pix - 0.5f) / GamePanel.DENSITY; }
}
