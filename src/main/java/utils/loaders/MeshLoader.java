package utils.loaders;

import gfx.InstancedMesh;
import gfx.Mesh;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import utils.Utils;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.assimp.Assimp.*;

public class MeshLoader {

    public static Mesh loadMesh(String objPath, int instances) throws Exception {
        return loadMesh(objPath, instances, aiProcess_GenSmoothNormals | aiProcess_Triangulate | aiProcess_SortByPType | aiProcess_JoinIdenticalVertices | aiProcess_FixInfacingNormals);
    }

    public static Mesh loadMesh(String objPath, int instances, int flags) throws Exception {

        Mesh mesh = null;
        try {

            AIScene aiScene = aiImportFile(objPath, flags);

            if (aiScene == null) {
                throw new Exception("Failed to load model !!!");
            }

            AIMesh aiMesh = AIMesh.create(aiScene.mMeshes().get(0));

            mesh = processMesh(aiMesh, instances);

        } catch(NoSuchMethodException e){
            e.getCause();
            e.printStackTrace();
        }

        return mesh;
    }

    private static Mesh processMesh(AIMesh aiMesh, int instances) {

        List<Float> vertices = new ArrayList<>();
        List<Float> textures = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        // Process vertices
        AIVector3D.Buffer aiVertices = aiMesh.mVertices();
        while (aiVertices.remaining() > 0) {
            AIVector3D aiVertex = aiVertices.get();
            vertices.add(aiVertex.x());
            vertices.add(aiVertex.y());
            vertices.add(aiVertex.z());
        }

        // Process texture coordinates
        AIVector3D.Buffer textCoords = aiMesh.mTextureCoords(0);
        int numTextCoords = textCoords != null ? textCoords.remaining() : 0;
        for (int i = 0; i < numTextCoords; i++) {
            AIVector3D textCoord = textCoords.get();
            textures.add(textCoord.x());
            textures.add(1 - textCoord.y());
        }

        // Process face normals
        AIVector3D.Buffer aiNormals = aiMesh.mNormals();
        while (aiNormals != null && aiNormals.remaining() > 0) {
            AIVector3D aiNormal = aiNormals.get();
            normals.add(aiNormal.x());
            normals.add(aiNormal.y());
            normals.add(aiNormal.z());
        }

        // Process indices
        int numFaces = aiMesh.mNumFaces();
        AIFace.Buffer aiFaces = aiMesh.mFaces();
        for (int i = 0; i < numFaces; i++) {
            AIFace aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.remaining() > 0) {
                indices.add(buffer.get());
            }
        }

        Mesh mesh;
        if(instances > 1){
            mesh = new InstancedMesh(Utils.listToArray(vertices), Utils.listToArray(textures),
                    Utils.listToArray(normals), Utils.listIntToArray(indices), instances);
        } else {
            mesh = new Mesh(Utils.listToArray(vertices), Utils.listToArray(textures),
                    Utils.listToArray(normals), Utils.listIntToArray(indices));
        }

        return mesh;
    }

}
