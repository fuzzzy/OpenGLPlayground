package test.fuzzy.openglplayground;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by fz on 14.11.15.
 */

public class PlayAroundRenderer implements  GLSurfaceView.Renderer {

    private static final String TAG = "Renderer";
    private static final int BYTES_PER_FLOAT = 4;
    Context context;

    private int glProgram;
    private int positionHandle;
    private int MVPMatrixHandle;
    private FloatBuffer verticesBuffer;
    private FloatBuffer routeBuffer;

    int pointsCount = 0;
    final float ROUTE_SCALE = 0.3f;
    final float BOTTOM_COORD = -1f;
    void prepareRoute() {
        LinkedList<GlPoint> route = Utils.readPointsFromCsv(context, R.raw.route);
        pointsCount = route.size() * 3 * BYTES_PER_FLOAT;
        routeBuffer = ByteBuffer.allocateDirect(route.size() * 3 * 3 * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        for(int i = 1; i < route.size(); i++) {
            GlPoint tail = route.get(i-1);
            GlPoint front = route.get(i);
            float xLen = front.x - tail.x;
            float yLen = front.y - tail.y;

            float leftPtX = tail.x - yLen / 2;
            float leftPtY = tail.y + xLen / 2;

            float rightPtX = tail.x + yLen / 2;
            float rightPtY = tail.y - xLen / 2;

            routeBuffer.put(front.x * ROUTE_SCALE).put(BOTTOM_COORD).put(-front.y * ROUTE_SCALE);
            routeBuffer.put(leftPtX * ROUTE_SCALE).put(BOTTOM_COORD).put(-leftPtY * ROUTE_SCALE);
            routeBuffer.put(rightPtX * ROUTE_SCALE).put(BOTTOM_COORD).put(-rightPtY * ROUTE_SCALE);
        }
        routeBuffer.position(0);
    }

    public PlayAroundRenderer(Context c) {
        context = c;
        prepareRoute();
    }

    private float[] mViewMatrix = new float[16];

    @Override
    public void onSurfaceCreated(GL10 ignore, EGLConfig config) {
        // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 2.0f;
        final float eyeZ = 1.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -15.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.vertex_shader);
        int fragmentShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.fragment_shader);

        glProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(glProgram, vertexShader);
        GLES20.glAttachShader(glProgram, fragmentShader);
        GLES20.glLinkProgram(glProgram);
        GLES20.glUseProgram(glProgram);

        positionHandle = GLES20.glGetAttribLocation(glProgram, "aPosition");
        MVPMatrixHandle = GLES20.glGetUniformLocation(glProgram, "uMVPMatrix");
    }

    private float[] projMatrix = new float[16];

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height)
    {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 45.0f;

        Matrix.frustumM(projMatrix, 0, left, right, bottom, top, near, far);
    }

    float vertices[] = {
        0.0f,  0.5f, // Vertex 1 (X, Y)
        0.5f, -0.5f, // Vertex 2 (X, Y)
        -0.5f, -0.5f  // Vertex 3 (X, Y)
    };

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private float[] modelMatrix = new float[16];

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(.5f, .5f, .5f, 1f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
// Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        // Draw the triangle facing straight on.
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);

        drawRoute();
    }

    /** Allocate storage for the final combined matrix. This will be passed into the shader program. */
    private float[] MVPMatrix = new float[16];


    private void drawRoute()
    {
        // Pass in the position information
        routeBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, routeBuffer);

        GLES20.glEnableVertexAttribArray(positionHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(MVPMatrix, 0, mViewMatrix, 0, modelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(MVPMatrix, 0, projMatrix, 0, MVPMatrix, 0);

        GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, MVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, pointsCount / 4);
    }

    /**
     * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
     *
     * @param type The type of shader we will be creating.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The shader object handler.
     */
    private int loadGLShader(int type, int resId) {
        String code = Utils.readRawTextFile(context, resId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
