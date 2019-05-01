package com.pedro.encoder.input.gl.render.filters;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import androidx.annotation.RequiresApi;
import com.pedro.encoder.R;
import com.pedro.encoder.utils.gl.GlUtil;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by pedro on 29/01/18.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class SwirlFilterRender extends BaseFilterRender {

  //rotation matrix
  private final float[] squareVertexDataFilter = {
      // X, Y, Z, U, V
      -1f, -1f, 0f, 0f, 0f, //bottom left
      1f, -1f, 0f, 1f, 0f, //bottom right
      -1f, 1f, 0f, 0f, 1f, //top left
      1f, 1f, 0f, 1f, 1f, //top right
  };

  private int program = -1;
  private int aPositionHandle = -1;
  private int aTextureHandle = -1;
  private int uMVPMatrixHandle = -1;
  private int uSTMatrixHandle = -1;
  private int uSamplerHandle = -1;
  private int uTimeHandle = -1;
  private int uResolutionHandle = -1;
  private int uRadiusHandle = -1;
  private int uCenterHandle = -1;

  private long START_TIME = System.currentTimeMillis();
  private boolean isIncrement = true;
  private float time = 0f;
  private float radius = 0.2f;
  private float centerX = 0.5f, centerY = 0.5f;

  public SwirlFilterRender() {
    squareVertex = ByteBuffer.allocateDirect(squareVertexDataFilter.length * FLOAT_SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer();
    squareVertex.put(squareVertexDataFilter).position(0);
    Matrix.setIdentityM(MVPMatrix, 0);
    Matrix.setIdentityM(STMatrix, 0);
  }

  @Override
  protected void initGlFilter(Context context) {
    String vertexShader = GlUtil.getStringFromRaw(context, R.raw.simple_vertex);
    String fragmentShader = GlUtil.getStringFromRaw(context, R.raw.swirl_fragment);

    program = GlUtil.createProgram(vertexShader, fragmentShader);
    aPositionHandle = GLES20.glGetAttribLocation(program, "aPosition");
    aTextureHandle = GLES20.glGetAttribLocation(program, "aTextureCoord");
    uMVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
    uSTMatrixHandle = GLES20.glGetUniformLocation(program, "uSTMatrix");
    uSamplerHandle = GLES20.glGetUniformLocation(program, "uSampler");
    uTimeHandle = GLES20.glGetUniformLocation(program, "uTime");
    uResolutionHandle = GLES20.glGetUniformLocation(program, "uResolution");
    uRadiusHandle = GLES20.glGetUniformLocation(program, "uRadius");
    uCenterHandle = GLES20.glGetUniformLocation(program, "uCenter");
  }

  @Override
  protected void drawFilter() {
    GLES20.glUseProgram(program);

    squareVertex.position(SQUARE_VERTEX_DATA_POS_OFFSET);
    GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false,
        SQUARE_VERTEX_DATA_STRIDE_BYTES, squareVertex);
    GLES20.glEnableVertexAttribArray(aPositionHandle);

    squareVertex.position(SQUARE_VERTEX_DATA_UV_OFFSET);
    GLES20.glVertexAttribPointer(aTextureHandle, 2, GLES20.GL_FLOAT, false,
        SQUARE_VERTEX_DATA_STRIDE_BYTES, squareVertex);
    GLES20.glEnableVertexAttribArray(aTextureHandle);

    GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, MVPMatrix, 0);
    GLES20.glUniformMatrix4fv(uSTMatrixHandle, 1, false, STMatrix, 0);
    GLES20.glUniform1f(uTimeHandle, getTime());
    GLES20.glUniform2f(uResolutionHandle, getWidth(), getHeight());
    GLES20.glUniform2f(uCenterHandle, centerX, centerY);
    GLES20.glUniform1f(uRadiusHandle, radius);
    GLES20.glUniform1i(uSamplerHandle, 4);
    GLES20.glActiveTexture(GLES20.GL_TEXTURE4);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, previousTexId);
  }

  private float getTime() {
    float interval = ((float) (System.currentTimeMillis() - START_TIME)) / 1000f;
    START_TIME = System.currentTimeMillis();
    if (isIncrement) {
      time += interval;
    } else {
      time -= interval;
    }
    if (time > 2) {
      isIncrement = false;
    } else if (time < -2) {
      isIncrement = true;
    }
    return time;
  }

  @Override
  public void release() {
    GLES20.glDeleteProgram(program);
  }

  public float getRadius() {
    return radius;
  }

  public void setRadius(float radius) {
    this.radius = radius;
  }

  public float[] getCenter() {
    return new float[] { centerX, centerY };
  }

  public void setCenterX(float centerX, float centerY) {
    this.centerX = centerX;
    this.centerY = centerY;
  }
}
