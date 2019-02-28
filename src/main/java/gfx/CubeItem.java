package gfx;

public class CubeItem extends GameItem {

    private BlockType blockType;

    public CubeItem(Mesh mesh){
        super(mesh);
    }

    public CubeItem(){
        super();
    }

    public void setBlockType(BlockType blockType){
        this.blockType = blockType;
    }

    public BlockType getBlockType(){
        return this.blockType;
    }


}
