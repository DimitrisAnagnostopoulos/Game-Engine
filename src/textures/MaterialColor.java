package textures;

import org.lwjgl.util.vector.Vector3f;

public class MaterialColor extends Material
{
	private Vector3f color;

	public MaterialColor(Vector3f color, int startIndex, int indicesCount)
	{
		super(startIndex, indicesCount);
		this.type = MaterialType.COLOR;
		this.color = color;
	}
	
	public Vector3f getColor()
	{
		return color;
	}
}
