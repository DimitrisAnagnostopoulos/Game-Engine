package dataStructures;

public class MeshData {

	private static final int DIMENSIONS = 3;

	private float[] vertices;
	private float[] textureCoords;
	private float[] normals;
	private int[] indices;
	private int[] materialChangeIndices;
	private int[] jointIds;
	private float[] vertexWeights;

	public MeshData(float[] vertices, float[] textureCoords, float[] normals, int[] indices, int[] materialChangeIndices, int[] jointIds,
			float[] vertexWeights) {
		this.vertices = vertices;
		this.textureCoords = textureCoords;
		this.normals = normals;
		this.indices = indices;
		this.materialChangeIndices = materialChangeIndices;
		this.jointIds = jointIds;
		this.vertexWeights = vertexWeights;
	}

	public int[] getJointIds() {
		return jointIds;
	}

	public float[] getVertexWeights() {
		return vertexWeights;
	}

	public float[] getVertices() {
		return vertices;
	}

	public float[] getTextureCoords() {
		return textureCoords;
	}

	public float[] getNormals() {
		return normals;
	}

	public int[] getIndices() {
		return indices;
	}

	public int[] getMaterialChangeIndices()
	{
		return materialChangeIndices;
	}

	public int getVertexCount() {
		return vertices.length / DIMENSIONS;
	}

}
