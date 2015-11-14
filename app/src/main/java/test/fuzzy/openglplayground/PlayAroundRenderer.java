package test.fuzzy.openglplayground;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by fz on 14.11.15.
 */

public class PlayAroundRenderer implements  GLSurfaceView.Renderer {

    Context context;

    public PlayAroundRenderer(Context c) {
        context = c;
    }
    @Override
    public void onSurfaceCreated(GL10 ignore, EGLConfig config) {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
//        mProgram = createProgram(mVertexShader, mFragmentShader);
//        if (mProgram == 0) {
//            return;
//        }
//        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
//        checkGlError("glGetAttribLocation aPosition");
//        if (maPositionHandle == -1) {
//            throw new RuntimeException("Could not get attrib location for aPosition");
//        }
//        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
//        checkGlError("glGetAttribLocation aTextureCoord");
//        if (maTextureHandle == -1) {
//            throw new RuntimeException("Could not get attrib location for aTextureCoord");
//        }
//
//        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
//        checkGlError("glGetUniformLocation uMVPMatrix");
//        if (muMVPMatrixHandle == -1) {
//            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
//        }

    }

    @Override
    public void onSurfaceChanged(GL10 ignore, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        //float ratio = (float) width / height;
        //Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }
}
