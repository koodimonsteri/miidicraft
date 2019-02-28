package gfx;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Mesh {

    protected final int vaoId;
    protected List<Integer> vboIdList;
    private final int vertexCount;

    private Texture texture;
    private Material material;

    public Mesh(float[] positions, float[] textureCoords, float[] normals, int[] indices){
        FloatBuffer positionBuffer = null;
        FloatBuffer vectorNormalBuffer = null;
        FloatBuffer textureCoordBuffer = null;
        IntBuffer indicesBuffer = null;

        try {
            vertexCount = indices.length;
            vboIdList = new ArrayList<>();

            // Create VAO
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // Create positions VBO
            int vboId = glGenBuffers();
            vboIdList.add(vboId);
            positionBuffer = MemoryUtil.memAllocFloat(positions.length);
            positionBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, positionBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // Create texture coordinates VBO
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            textureCoordBuffer = MemoryUtil.memAllocFloat(textureCoords.length);
            textureCoordBuffer.put(textureCoords).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, textureCoordBuffer, GL_DYNAMIC_DRAW);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

            // Create vertex normals VBO
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            vectorNormalBuffer = MemoryUtil.memAllocFloat(normals.length);
            vectorNormalBuffer.put(normals).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, vectorNormalBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

            // Create index VBO
            vboId = glGenBuffers();
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

        } finally {
            if (positionBuffer != null) {
                MemoryUtil.memFree(positionBuffer);
            }
            if (textureCoordBuffer != null) {
                MemoryUtil.memFree(textureCoordBuffer);
            }
            if (vectorNormalBuffer != null) {
                MemoryUtil.memFree(vectorNormalBuffer);
            }
            if (indicesBuffer != null) {
                MemoryUtil.memFree(indicesBuffer);
            }
        }
    }

    public void render(){

        initRender();

        // Draw the mesh
        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);

        endRender();
    }

    public void renderList(List<GameItem> gItems, Consumer<GameItem> consumer){

        initRender();

        for(GameItem gItem : gItems){
            consumer.accept(gItem);
            glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
        }

        endRender();
    }

    protected void initRender(){
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getId());

        glBindVertexArray(getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
    }

    protected void endRender(){
        // Restore state
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);

        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);

        // Delete the VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for(int vboId : vboIdList){
            glDeleteBuffers(vboId);
        }

        // Delete texture
        texture.cleanUp();

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public Texture getTexture() {
        return this.texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public int getVaoId() {
        return vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}
