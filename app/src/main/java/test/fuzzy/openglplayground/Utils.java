package test.fuzzy.openglplayground;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 * Created by fz on 15.11.15.
 */
public class Utils {
    public static String readRawTextFile(final Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder res = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                res.append(line).append("\n");
            }
            reader.close();
            return res.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LinkedList<GlPoint> readPointsFromCsv(final Context ctx, int resId) {
        LinkedList<GlPoint> res = null;
        InputStream inputStream = ctx.getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            res = new LinkedList<GlPoint>();
            while ((line = reader.readLine()) != null) {
                try {
                    String[] row = line.split(",", 0);
                    GlPoint p = new GlPoint(0,0,0);
                    p.x = Float.parseFloat(row[0].trim());
                    p.y = Float.parseFloat(row[1].trim());
                    p.z = Float.parseFloat(row[2].trim());

                    res.add(p);
                }
                catch (Exception e) {
                    continue;
                }
            }
            reader.close();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
