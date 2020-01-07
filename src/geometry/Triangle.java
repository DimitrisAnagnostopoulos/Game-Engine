package geometry;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

public class Triangle {

	public List<Vector3f> vertices = new ArrayList<Vector3f>();

	public Triangle(Vector3f a, Vector3f b, Vector3f c) {
		this.vertices.add(a);
		this.vertices.add(b);
		this.vertices.add(c);
	}

	public void update(Vector3f a, Vector3f b, Vector3f c) {
		this.vertices.clear();
		this.vertices.add(a);
		this.vertices.add(b);
		this.vertices.add(c);
	}

}
