package pathfinding;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Entity;
import game.Game;
import geometry.Node;
import geometry.Triangle;
import models.Path;
import toolbox.Maths;

@SuppressWarnings("rawtypes")
public class Pathfinder {

	private Entity entity;
	public List<Node> nodes = new ArrayList<>();
	public List<Vector2f> points = new ArrayList<>();
	public int pathIndex = 0;
	public Path path;
	private static final float offsetDistance = 2f;

	public static class PriorityList extends LinkedList {

		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unchecked")
		public void add(Comparable object) {
			for (int i = 0; i < size(); i++) {
				if (object.compareTo(get(i)) <= 0) {
					add(i, object);
					return;
				}
			}
			addLast(object);
		}
	}

	public Pathfinder(Entity entity) {
		this.entity = entity;
	}

	public void findPath(Node startNode, Node goalNode, Vector3f startPoint, Vector3f endPoint) {
		if (this.nodes.isEmpty() == false && startNode == this.nodes.get(0) && goalNode == this.nodes.get(nodes.size() - 1)) {
			this.points = findPathPoints(this.nodes, Maths.get2D(startPoint), Maths.get2D(endPoint));
		} else {
			this.nodes = findPathNodes(startNode, goalNode);
			if (this.nodes.size() <= 1 && startNode.getNeighbors().contains(goalNode) == false) {
				this.nodes.clear();
				this.points.clear();
			} else {
				this.points = findPathPoints(this.nodes, Maths.get2D(startPoint), Maths.get2D(endPoint));
			}
		}
		this.path = Game.loader.generatePath(this.points, this.path);
	}

	private List<Vector2f> findPathPoints(List<Node> nodeList, Vector2f start, Vector2f end) {

		List<Vector2f> path = new ArrayList<Vector2f>();
		List<Vector2f> midpointPath = new ArrayList<Vector2f>();
		Vector2f[] leftSide = new Vector2f[nodeList.size()];
		Vector2f[] rightSide = new Vector2f[nodeList.size()];

		// create funnel and borders
		midpointPath.add(start);
		for (int i = 0; i <= nodeList.size() - 2; i++) {
			Vector3f[] commonVertices = getCommonVertices(nodeList.get(i), nodeList.get(i + 1));
			if (Maths.angleBetween2DPoints(Vector2f.sub(Maths.get2D(nodeList.get(i + 1).getCenter()), Maths.get2D(nodeList.get(i).getCenter()), null),
					Vector2f.sub(Maths.get2D(commonVertices[0]), Maths.get2D(nodeList.get(i).getCenter()), null)) < 0) {
				leftSide[i] = Maths.get2D(commonVertices[0]);
				rightSide[i] = Maths.get2D(commonVertices[1]);
			} else {
				leftSide[i] = Maths.get2D(commonVertices[1]);
				rightSide[i] = Maths.get2D(commonVertices[0]);
			}
			Vector2f midPoint = getMidPoint(leftSide[i], rightSide[i]);
			midpointPath.add(midPoint);
		}
		leftSide[nodeList.size() - 1] = rightSide[nodeList.size() - 1] = end;
		midpointPath.add(end);

		// now find the path
		path.add(start);
		int index = 1;
		for (int i = 0; i <= nodeList.size() - 1; i++) {
			// make sure all the left points are on the left of our path so far and
			// all the right are on our right
			for (int j = index; j <= i; j++) {
				float angleToLeft = Maths.angleBetween2DPoints(Vector2f.sub(leftSide[i], path.get(path.size() - 1), null), Vector2f.sub(leftSide[j], path.get(path.size() - 1), null));
				if (angleToLeft > 1) {
					index = i - 1;
					Vector2f offset = (Vector2f) Vector2f.sub(midpointPath.get(index), leftSide[index], null).normalise().scale(offsetDistance);
					path.add(Vector2f.add(leftSide[index], offset, null));	
					break;
				}
				float angleToRight = Maths.angleBetween2DPoints(Vector2f.sub(rightSide[i], path.get(path.size() - 1), null), Vector2f.sub(rightSide[j], path.get(path.size() - 1), null));
				if (angleToRight < -1) {
					index = i - 1;
					Vector2f offset = (Vector2f) Vector2f.sub(midpointPath.get(index), rightSide[index], null).normalise().scale(offsetDistance);
					path.add(Vector2f.add(rightSide[index], offset, null));
					break;
				}
			}
		}
		path.add(end);

		return path;
	}

	private Vector3f[] getCommonVertices(Node a, Node b) {
		Vector3f[] common = new Vector3f[2];
		int count = 0;
		for (Vector3f vertex : a.vertices) {
			if (count < 2 && b.vertices.contains(vertex)) {
				common[count] = vertex;
				count++;
			}
		}
		return common;
	}

	public Vector2f getMidPoint(Vector2f a, Vector2f b) {
		return new Vector2f((a.x + b.x) / 2, (a.y + b.y) / 2);
	}
	
	protected List<Node> constructPath(Node node) {
		LinkedList<Node> path = new LinkedList<>();
		while (node.pathParent != null) {
			path.addFirst(node);
			node = node.pathParent;
		}
		return path;
	}

	public List<Node> findPathNodes(Node startNode, Node goalNode) {
		this.cleanUp();

		PriorityList openList = new PriorityList();
		LinkedList<Node> closedList = new LinkedList<>();

		startNode.costFromStart = 0;
		startNode.estimatedCostToGoal = startNode.getEstimatedCost(goalNode);
		startNode.pathParent = null;
		openList.add(startNode);

		while (!openList.isEmpty()) {
			Node node = (Node) openList.removeFirst();
			if (node == goalNode) {
				this.nodes = constructPath(goalNode);
				return this.nodes;
			}

			List neighbors = node.getNeighbors();
			for (int i = 0; i < neighbors.size(); i++) {
				Node neighborNode = (Node) neighbors.get(i);
				boolean isOpen = openList.contains(neighborNode);
				boolean isClosed = closedList.contains(neighborNode);
				float costFromStart = node.costFromStart + node.getCost(neighborNode);

				if ((!isOpen && !isClosed) || costFromStart < neighborNode.costFromStart) {
					neighborNode.pathParent = node;
					neighborNode.costFromStart = costFromStart;
					neighborNode.estimatedCostToGoal = neighborNode.getEstimatedCost(goalNode);
					if (isClosed) {
						closedList.remove(neighborNode);
					}
					if (!isOpen) {
						openList.add(neighborNode);
					}
				}
			}
			closedList.add(node);
		}

		this.nodes.clear();
		return this.nodes;
	}

	public void followPath() {
		Vector3f position3D = this.entity.getPosition();
		float currentSpeed = this.entity.getCurrentSpeed();
		if (!points.isEmpty() && pathIndex < points.size()) {
			Vector2f position = new Vector2f(position3D.x, position3D.z);
			Vector2f destination = points.get(pathIndex);
			if (Maths.distanceBetween2DPoints(position, destination) < 1 && currentSpeed > 0) {
				pathIndex++;
			} else if (pathIndex > 0 && Maths.distanceBetween2DPoints(position, points.get(pathIndex - 1)) < 1 && currentSpeed < 0) {
				pathIndex--;
			}
			float angle = this.entity.getRotY();
			if (currentSpeed > 0 && points.size() > pathIndex) {
				angle = Maths.angleOfLine(points.get(pathIndex), position);
			} else if (currentSpeed < 0 && pathIndex > 0) {
				angle = Maths.angleOfLine(points.get(pathIndex - 1), position);
			}
			float andgleDiff = this.entity.getRotY() - angle;
			if (andgleDiff > 1 && andgleDiff < 90) {
				this.entity.setCurrentTurnSpeed(-200);
			} else if (andgleDiff < -1 && andgleDiff > -90) {
				this.entity.setCurrentTurnSpeed(200);
			} else {
				this.entity.setRotY(angle);
			}
		} else {
			pathIndex = 0;
			points.clear();
		}
	}

	public void cleanUp() {
		for (Triangle triangle : this.entity.floor.getTriangles()) {
			Node node = (Node) triangle;
			node.cleanUp();
		}
	}

}
