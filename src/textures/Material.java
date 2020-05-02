package textures;

import org.lwjgl.util.vector.Vector3f;

enum MaterialType {COLOR, TEXTURE, MULTITEXTURE}

abstract class Material
{
	protected MaterialType type;
	protected int startIndex;
	protected int indicesCount;
	public Vector3f hightlightColor;
	
	public Material(int startIndex, int indicesCount)
	{
		this.startIndex = startIndex;
		this.indicesCount = indicesCount;
	}

	public MaterialType getType()
	{
		return type;
	}

	public int getStartIndex()
	{
		return startIndex;
	}

	public void setStartIndex(int startIndex)
	{
		this.startIndex = startIndex;
	}

	public int getIndicesCount()
	{
		return indicesCount;
	}

	public void setIndicesCount(int indicesCount)
	{
		this.indicesCount = indicesCount;
	}
}
