package gfx;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;
import utils.Transformation;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;

public class InstancedMesh extends Mesh {

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int VECTOR4F_SIZE_BYTES = 4 * FLOAT_SIZE_BYTES;
    private static final int MATRIX_SIZE_FLOATS = 4 * 4;
    private static final int MATRIX_SIZE_BYTES = MATRIX_SIZE_FLOATS * FLOAT_SIZE_BYTES;
    private static final int INSTANCE_SIZE_BYTES = MATRIX_SIZE_BYTES * 2 + FLOAT_SIZE_BYTES * 2;
    private static final int INSTANCE_SIZE_FLOATS = MATRIX_SIZE_FLOATS * 2 + 2;

    private final int numInstances;
    private final int instanceModelVBO;
    private FloatBuffer modelViewBuffer;

    public InstancedMesh(float[] positions, float[] textCoords, float[] normals, int[] indices, int numInstances) {
        super(positions, textCoords, normals, indices);

        this.numInstances = numInstances;

        glBindVertexArray(vaoId);

        // Model View Matrix
        instanceModelVBO = glGenBuffers();
        vboIdList.add(instanceModelVBO);
        modelViewBuffer = MemoryUtil.memAllocFloat(numInstances * INSTANCE_SIZE_FLOATS);
        glBindBuffer(GL_ARRAY_BUFFER, instanceModelVBO);
        int start = 3;
        int strideStart = 0;
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
            glVertexAttribDivisor(start, 1);
            start++;
            strideStart += VECTOR4F_SIZE_BYTES;
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        if (this.modelViewBuffer != null) {
            MemoryUtil.memFree(this.modelViewBuffer);
            this.modelViewBuffer = null;
        }
    }

    @Override
    protected void initRender() {
        super.initRender();

        int start = 3;
        int numElements = 4 * 2 + 1;
        for (int i = 0; i < numElements; i++) {
            glEnableVertexAttribArray(start + i);
        }
    }

    @Override
    protected void endRender() {
        int start = 3;
        int numElements = 4 * 2 + 1;
        for (int i = 0; i < numElements; i++) {
            glDisableVertexAttribArray(start + i);
        }

        super.endRender();
    }

    public void renderListInstanced(List<GameItem> gameItems, Transformation transformation, Matrix4f viewMatrix) {
        initRender();

        int chunkSize = numInstances;
        int length = gameItems.size();
        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(length, i + chunkSize);
            List<GameItem> subList = gameItems.subList(i, end);
            renderChunkInstanced(subList, transformation, viewMatrix);
        }

        endRender();
    }

    private void renderChunkInstanced(List<GameItem> gameItems, Transformation transformation, Matrix4f viewMatrix) {
        this.modelViewBuffer.clear();

        int i = 0;

        //Texture text = getTexture();
        for (GameItem gameItem : gameItems) {
            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(gameItem, viewMatrix);
            if (modelViewMatrix != null) {
                modelViewMatrix.get(MATRIX_SIZE_FLOATS * i, modelViewBuffer);
            }

            i++;
        }

        glBindBuffer(GL_ARRAY_BUFFER, instanceModelVBO);
        glBufferData(GL_ARRAY_BUFFER, modelViewBuffer, GL_DYNAMIC_DRAW);

        glDrawElementsInstanced(
                GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0, gameItems.size());

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
