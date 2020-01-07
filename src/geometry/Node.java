package geometry;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import entities.Entity;
import entities.Floor;
import toolbox.Maths;

@SuppressWarnings("rawtypes")
public class Node extends Triangle implements Comparable {

	public Node(Vector3f a, Vector3f b, Vector3f c, Entity entity, boolean walkable) {
		super(a, b, c);
		this.floor = (Floor) entity;
		this.center = new Vector3f((a.x + b.x + c.x) / 3.0f, (a.y + b.y + c.y) / 3.0f, (a.z + b.z + c.z) / 3.0f);
		this.walkable = walkable;
	}

	private Floor floor;
	private Vector3f center;

	public Node pathParent;
	public float costFromStart;
	public float estimatedCostToGoal;
	public boolean walkable;

	public Vector3f getCenter() {
		return center;
	}

	public float getCost() {
		return costFromStart + estimatedCostToGoal;
	}

	public int compareTo(Object other) {
		float thisValue = this.getCost();
		float otherValue = ((Node) other).getCost();

		float v = thisValue - otherValue;
		return (v > 0) ? 1 : (v < 0) ? -1 : 0;
	}

	public float getCost(Node node) {
		return Maths.distanceBetweenPoints(this.center, node.center);
	}

	public float getEstimatedCost(Node node) {
		return Maths.distanceBetweenPoints(this.center, node.center);
	}

	public List getNeighbors() {
		List<Node> neighbors = new ArrayList<>();
		outerLoop: for (Vector3f vertex : this.vertices) {
			// gather the nodes that share at least two vertices with our node
			for (Triangle triangle : floor.getTrianglesToVerticesMap().get(vertex)) {
				Node node = (Node) triangle;
				if (triangle == this || node.walkable == false) continue;
				List<Vector3f> common = new ArrayList<Vector3f>(this.vertices);
				common.retainAll(node.vertices);
				if (common.size() >= 2 && neighbors.contains(triangle) == false) neighbors.add((Node) node);
				if (neighbors.size() >= 3) break outerLoop;
			}
		}
		return neighbors;
	}

	public void cleanUp() {
		this.pathParent = null;
		this.costFromStart = 1000000f;
		this.estimatedCostToGoal = 1000000f;
	}

}
