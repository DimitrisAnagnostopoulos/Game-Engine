package geometry;

import org.lwjgl.util.vector.Vector3f;

public class Plane {

	public float equation[] = new float[4];
	public Vector3f origin;
	public Vector3f normal;

	public Plane(Vector3f origin, Vector3f normal) {
		this.normal = normal;
		this.origin = origin;
		equation[0] = normal.x;
		equation[1] = normal.y;
		equation[2] = normal.z;
		equation[3] = -(normal.x * origin.x + normal.y * origin.y + normal.z * origin.z);
	}

	public Plane(Vector3f p1, Vector3f p2, Vector3f p3) {
		normal = Vector3f.cross(Vector3f.sub(p2, p1, null), Vector3f.sub(p3, p1, null), null);
		if (normal.length() > 0)
			normal.normalise();
		origin = p1;
		equation[0] = normal.x;
		equation[1] = normal.y;
		equation[2] = normal.z;
		equation[3] = -(normal.x * origin.x + normal.y * origin.y + normal.z * origin.z);
	}

	public double signedDistanceTo(Vector3f point) {
		return (Vector3f.dot(point, normal)) + equation[3];
	}

	public boolean isFrontFacingTo(Vector3f direction) {
		double dot = Vector3f.dot(normal, direction);
		return (dot <= 0);
	}

}
