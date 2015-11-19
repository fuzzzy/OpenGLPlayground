package test.fuzzy.openglplayground;

/**
 * Created by fz on 19.11.15.
 */
public class GlPoint {
    public float x;
    public float y;
    public float z;

    public GlPoint() {
    }

    public GlPoint(GlPoint p) {
        set(p);
    }

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

    public GlPoint add(GlPoint p) {
        x += p.x;
        y += p.y;
        z += p.z;
        return this;
    }

    public GlPoint sub(GlPoint p) {
        x -= p.x;
        y -= p.y;
        z -= p.z;
        return this;
    }

    public boolean isZero() {
        return (x == 0 && y == 0  && z == 0);
    }

    static float dist(final GlPoint src, final GlPoint dst) {
        return (float)Math.sqrt((dst.x - src.x) * (dst.x - src.x) + (dst.y - src.y) * (dst.y - src.y) +  (dst.z - src.z) * (dst.z - src.z)) ;
    }
}
