package test.fuzzy.openglplayground;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by fz on 14.11.15.
 */
public class PlayAroundRenderer implements CardboardView.StereoRenderer {
    private static final String TAG = "Renderer";
    private static final int BYTES_PER_FLOAT = 4;
    Context context;

    private int glProgram;
    private int positionHandle;
    private int MVPMatrixHandle;

    private float[] viewMatrix = new float[16];
    private float[] projMatrix = new float[16];
    private float[] modelMatrix = new float[16];
    private float[] MVPMatrix = new float[16];

    private FloatBuffer routeBuffer;

    GlPoint cameraPosition = new GlPoint(0,0,0);
    GlPoint cameraSpeed = new GlPoint(0,0,0);
    int cameraTrackTargetIdx = 1;

    LinkedList<GlPoint> cameraTrack = new LinkedList<GlPoint>();

    int pointsCount = 0;

    final float ROUTE_SCALE = 0.3f;
    final float BOTTOM_COORD = -1f;

    final float SPEED = 0.1f;

    public PlayAroundRenderer(Context c) {
        context = c;
        prepareRoute();
    }

    void prepareRoute() {
        LinkedList<GlPoint> route = Utils.readPointsFromCsv(context, R.raw.route_);
        pointsCount = route.size() * 3 ;
        routeBuffer = ByteBuffer.allocateDirect(route.size() * 3 * 3 * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        cameraTrack.add(new GlPoint(route.get(0).x * ROUTE_SCALE, 1f, -route.get(0).y * ROUTE_SCALE));

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

            cameraTrack.add(new GlPoint(front.x * ROUTE_SCALE, 1f, -front.y * ROUTE_SCALE));
        }
        routeBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.vertex_shader);
        int fragmentShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.fragment_shader);

        glProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(glProgram, vertexShader);
        GLES20.glAttachShader(glProgram, fragmentShader);
        GLES20.glLinkProgram(glProgram);
        GLES20.glUseProgram(glProgram);

        positionHandle = GLES20.glGetAttribLocation(glProgram, "aPosition");
        MVPMatrixHandle = GLES20.glGetUniformLocation(glProgram, "uMVPMatrix");

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.setIdentityM(viewMatrix, 0);
    }

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

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        //TODO: bad!
        updateCamera();
    }

    float[] eyeViewMatrix = new float[16];
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClearColor(.5f, .5f, .5f, 1f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        Matrix.multiplyMM(eyeViewMatrix, 0, eye.getEyeView(), 0, viewMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, eyeViewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, projMatrix, 0, MVPMatrix, 0);
        GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, MVPMatrix, 0);

        drawRoute();
    }

    private void drawRoute()
    {
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, routeBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, pointsCount);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);
        final float ratio = (float) width / height;
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 40f);
    }

    int getNextTrackIdx(int currentPoint) {
        if(cameraTrack.size() < 1 ) {
            return -1;
        }
        int dstPointIdx = currentPoint + 1;
        if(dstPointIdx >= cameraTrack.size()) {
            dstPointIdx = 0;
        }
        return dstPointIdx;
    }

    float dist(final GlPoint src, final GlPoint dst) {
        return (float)Math.sqrt((dst.x - src.x) * (dst.x - src.x) + (dst.y - src.y) * (dst.y - src.y) +  (dst.z - src.z) * (dst.z - src.z)) ;
    }

    void updateCamera() {
        cameraPosition.x += cameraSpeed.x;
        cameraPosition.y += cameraSpeed.y;
        cameraPosition.z += cameraSpeed.z;

        GlPoint target = cameraTrack.get(getNextTrackIdx(cameraTrackTargetIdx));

        if((cameraSpeed.x == 0 && cameraSpeed.y == 0  && cameraSpeed.z == 0) || dist(cameraPosition, target) < SPEED ) {
            float distance = 0;
            GlPoint src;
            GlPoint dst;
            do {
                cameraTrackTargetIdx = getNextTrackIdx(cameraTrackTargetIdx);
                cameraPosition.x = cameraTrack.get(cameraTrackTargetIdx).x;
                cameraPosition.y = cameraTrack.get(cameraTrackTargetIdx).y;
                cameraPosition.z = cameraTrack.get(cameraTrackTargetIdx).z;

                src = cameraTrack.get(cameraTrackTargetIdx);
                dst = cameraTrack.get(getNextTrackIdx(cameraTrackTargetIdx));
                float segmentDistance = dist(src, dst);
                if(segmentDistance == 0) {
                    segmentDistance = 0.0001f;
                }

                distance += segmentDistance;
            }
            while(distance < SPEED);

            cameraSpeed.x = (dst.x - src.x) / (distance / SPEED);
            cameraSpeed.y = (dst.y - src.y) / (distance / SPEED);
            cameraSpeed.z = (dst.z - src.z) / (distance / SPEED);
        }

        GlPoint lookAt = cameraTrack.get(getNextTrackIdx(getNextTrackIdx(cameraTrackTargetIdx)));

        Matrix.setLookAtM(viewMatrix, 0
                , cameraPosition.x , cameraPosition.y, cameraPosition.z
                , lookAt.x, lookAt.y, lookAt.z
                , 0, 1, 0);
    }

    private void checkGlError(String op) {
        int error;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    @Override
    public void onRendererShutdown() {

    }
}
