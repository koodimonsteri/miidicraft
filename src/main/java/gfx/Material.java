package gfx;

import org.joml.Vector4f;

public class Material {

    private Vector4f diffuse;
    private Vector4f specular;
    private Vector4f ambient;
    private float reflectance;

    private Texture texture;

    public Material(Texture tex, float refl){
        this.texture = tex;
        this.reflectance = refl;

    }


}
