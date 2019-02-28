package gfx;

// Uppercase BlockType means it's visible,
// Lowercase means it's invisible
public enum BlockType {
    GRASS, DIRT, COBBLESTONE, STONE, AIR,
    grass, dirt, cobblestone, stone;

    public Character getBlockTypeChar(){
        Character res;
        switch(this.name()) {
            case "GRASS" : res = 'G'; break;
            case "DIRT" : res = 'D'; break;
            case "COBBLESTONE" : res = 'C'; break;
            case "STONE" : res = 'S'; break;
            case "grass" : res = 'g'; break;
            case "dirt" : res = 'd'; break;
            case "cobblestone" : res = 'c'; break;
            case "stone" : res = 's'; break;
            case "AIR" : res = 'A'; break;
            default : res = null; break;
        }
        return res;
    }
}
