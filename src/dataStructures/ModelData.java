package dataStructures;

public class ModelData {

	private final SkeletonData joints;
	private final MeshData mesh;

	public ModelData(MeshData mesh, SkeletonData joints) {
		this.joints = joints;
		this.mesh = mesh;
	}

	public SkeletonData getJointsData() {
		return joints;
	}

	public MeshData getMeshData() {
		return mesh;
	}

	public float[] getVertices() {
		return mesh.getVertices();
	}

	public float[] getTextureCoords() {
		return mesh.getTextureCoords();
	}

	public float[] getNormals() {
		return mesh.getNormals();
	}

	public int[] getIndices() {
		return mesh.getIndices();
	}

	public int[] getJointIds() {
		return mesh.getJointIds();
	}

	public float[] getVertexWeights() {
		return mesh.getVertexWeights();
	}

	public int[] getJointIndices() {
		return mesh.getJointIds();
	}

	public float[] getWeights() {
		return mesh.getVertexWeights();
	}

}
