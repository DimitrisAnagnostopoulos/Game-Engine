package animation;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class JointTransform {

	// remember, this position and rotation are relative to the parent bone!
	private final Vector3f position;
	private final Quaternion rotation;

	public JointTransform(Vector3f position, Quaternion rotation) {
		this.position = position;
		this.rotation = rotation;
	}

	protected Matrix4f getLocalTransform() {
		Matrix4f matrix = new Matrix4f();
		matrix.translate(position);
		Matrix4f.mul(matrix, rotation.toRotationMatrix(), matrix);
		return matrix;
	}

	protected static JointTransform interpolate(JointTransform frameA, JointTransform frameB, float progression) {
		Vector3f pos = interpolate(frameA.position, frameB.position, progression);
		Quaternion rot = Quaternion.interpolate(frameA.rotation, frameB.rotation, progression);
		return new JointTransform(pos, rot);
	}

	private static Vector3f interpolate(Vector3f start, Vector3f end, float progression) {
		float x = start.x + (end.x - start.x) * progression;
		float y = start.y + (end.y - start.y) * progression;
		float z = start.z + (end.z - start.z) * progression;
		return new Vector3f(x, y, z);
	}

}
