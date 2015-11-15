package test.fuzzy.openglplayground;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by fz on 14.11.15.
 */

public class PlayAroundRenderer implements  GLSurfaceView.Renderer {

    private static final String TAG = "Renderer";
    private static final int BYTES_PER_FLOAT = 4;
    Context context;

    public PlayAroundRenderer(Context c) {
        context = c;
    }


    @Override
    public void onSurfaceCreated(GL10 ignore, EGLConfig config) {

        int[] vbo = new int[1];

        GLES20.glGenBuffers(1, vbo, 0); // Generate 1 buffer
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);

        final FloatBuffer verticesBuffer = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        verticesBuffer.put(vertices).position(0);

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.length, verticesBuffer, GLES20.GL_STATIC_DRAW);
    }

    @Override
    public void onSurfaceChanged(GL10 ignore, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        //float ratio = (float) width / height;
        //Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }


    float vertices[] = {
            0.0f,  0.5f, // Vertex 1 (X, Y)
            0.5f, -0.5f, // Vertex 2 (X, Y)
            -0.5f, -0.5f  // Vertex 3 (X, Y)
    };

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(.5f, .5f, .5f, 1f);
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
