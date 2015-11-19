package test.fuzzy.openglplayground;

/**
 * Created by fz on 19.11.15.
 */
public class GlPoint {
    public float x;
    public float y;
    public float z;

    public GlPoint(float _x, float _y, float _z) {
        x = _x;
        y = _y;
        z = _z;
    }

    public void set(float[] crds, int offset) {
        x = crds[offset+ 0];
        y = crds[offset +1];
        z = crds[offset + 2];
    }

    public void set(GlPoint p) {
        x = p.x;
        y = p.y;
        z = p.z;
    }

    @Override
    public String toString() {
        return "("+x+", "+y+", "+z+")";
    }
}
