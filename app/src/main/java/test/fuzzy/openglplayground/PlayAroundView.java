package test.fuzzy.openglplayground;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by fz on 14.11.15.
 */
public class PlayAroundView extends GLSurfaceView {
    public PlayAroundView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        setRenderer(new PlayAroundRenderer(context));
    }
}
