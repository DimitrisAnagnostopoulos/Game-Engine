package textures;

import org.lwjgl.opengl.GL11;

import game.Game;

public class MaterialTexture extends Material
{
	private int textureID;
	public float shineDamper = 1;
	public float reflectivity = 0;
	public boolean hasTransparency = false;
	public boolean useFakeLighting = false;
	
	public MaterialTexture(String filePath, int startIndex, int indicesCount)
	{
		super(startIndex, indicesCount);
		this.type = MaterialType.TEXTURE;
		this.textureID = Game.loader.loadTexture(filePath);
	}

	public int getTextureID()
	{
		return textureID;
	}
	
	public void delete() {
		GL11.glDeleteTextures(textureID);
	}
}
