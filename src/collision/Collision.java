package collision;

import org.lwjgl.util.vector.Vector3f;

import entities.Entity;
import geometry.Plane;
import geometry.Triangle;
import models.Boundaries;
import toolbox.Maths;

public class Collision {

	public static Vector3f velocity;
	public static Vector3f normalizedVelocity;
	public static Vector3f basePoint;
	public static boolean foundCollision;
	public static double nearestDistance;
	public static Vector3f intersectionPoint;

	public static boolean boundingBoxCollision(Entity entityA, Entity entityB) {
		Boundaries a = entityA.getAbsoluteBoundingBox().getBoundaries();
		Boundaries b = entityB.getAbsoluteBoundingBox().getBoundaries();
		if (a.maxX < b.minX)
			return false;
		if (a.minX > b.maxX)
			return false;
		if (a.maxZ < b.minZ)
			return false;
		if (a.minZ > b.maxZ)
			return false;
		if (a.minY > b.maxY)
			return false;
		if (a.maxY < b.minY)
			return false;
		return true;
	}

	public static boolean pointInBoundingBox(Vector3f point, Entity entity) {
		Boundaries a = entity.getAbsoluteBoundingBox().getBoundaries();
		if (point.x < a.minX)
			return false;
		if (point.x > a.maxX)
			return false;
		if (point.z < a.minZ)
			return false;
		if (point.z > a.maxZ)
			return false;
		if (point.y > a.maxY)
			return false;
		if (point.y < a.minY)
			return false;
		return true;
	}

	public static boolean pointInAreaOfBoundingBox(Vector3f point, Entity entity) {
		Boundaries a = entity.getAbsoluteBoundingBox().getBoundaries();
		if (point.x < a.minX)
			return false;
		if (point.x > a.maxX)
			return false;
		if (point.z < a.minZ)
			return false;
		if (point.z > a.maxZ)
			return false;
		return true;
	}

	public static boolean pointIn2DTriangle(Vector3f point, Triangle triangle) {
		float x = point.x;
		float z = point.z;
		Vector3f A = triangle.vertices.get(0);
		Vector3f B = triangle.vertices.get(1);
		Vector3f C = triangle.vertices.get(2);

		float denominator = ((B.z - C.z) * (A.x - C.x) + (C.x - B.x) * (A.z - C.z));
		float l1 = ((B.z - C.z) * (x - C.x) + (C.x - B.x) * (z - C.z)) / denominator;
		float l2 = ((C.z - A.z) * (x - C.x) + (A.x - C.x) * (z - C.z)) / denominator;
		float l3 = 1 - l1 - l2;
		return 0 <= l1 && l1 <= 1 && 0 <= l2 && l2 <= 1 && 0 <= l3 && l3 <= 1;
	}

	public static boolean pointInTriangle(Vector3f point, Triangle triangle) {
		Vector3f pa = triangle.vertices.get(0);
		Vector3f pb = triangle.vertices.get(1);
		Vector3f pc = triangle.vertices.get(2);

		Vector3f e10 = Vector3f.sub(pb, pa, null);
		Vector3f e20 = Vector3f.sub(pc, pa, null);
		Vector3f vp = Vector3f.sub(point, pa, null);

		// Compute dot products
		float a = Vector3f.dot(e10, e10);
		float b = Vector3f.dot(e10, e20);
		float c = Vector3f.dot(e20, e20);
		float d = Vector3f.dot(vp, e10);
		float e = Vector3f.dot(vp, e20);

		// Compute barycentric coordinates
		float ac_bb = (a * c) - (b * b);
		float x = (d * c) - (e * b);
		float y = (e * a) - (d * b);
		float z = x + y - ac_bb;
		return z < 0 && x >= 0 && y >= 0;
	}

	public static float heightFromCoordsInsideTriangle(Vector3f point, Triangle triangle) {
		float x = point.x;
		float z = point.z;
		Vector3f p1 = triangle.vertices.get(0);
		Vector3f p2 = triangle.vertices.get(1);
		Vector3f p3 = triangle.vertices.get(2);

		float denominator = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);

		float l1 = ((p2.z - p3.z) * (x - p3.x) + (p3.x - p2.x) * (z - p3.z)) / denominator;
		float l2 = ((p3.z - p1.z) * (x - p3.x) + (p1.x - p3.x) * (z - p3.z)) / denominator;
		float l3 = 1 - l1 - l2;

		return l1 * p1.y + l2 * p2.y + l3 * p3.y;
	}

	public static void checkTriangle(Triangle triangle) {
		Vector3f p1 = triangle.vertices.get(0);
		Vector3f p2 = triangle.vertices.get(1);
		Vector3f p3 = triangle.vertices.get(2);
		Plane trianglePlane = new Plane(p1, p2, p3);
		// Get interval of plane intersection:
		double t0, t1;
		boolean embeddedInPlane = false;
		double signedDistToTrianglePlane = trianglePlane.signedDistanceTo(Collision.basePoint);
		float normalDotVelocity = Vector3f.dot(trianglePlane.normal, Collision.velocity);

		if (trianglePlane.isFrontFacingTo(Collision.normalizedVelocity)) {
			// if sphere is travelling parrallel to the plane:
			if (normalDotVelocity == 0.0f && Collision.velocity.length() != 0) {
				if (Math.abs(signedDistToTrianglePlane) >= 1.0f) {
					// Sphere is not embedded in plane. No collision possible:
					return;
				} else {
					// sphere is embedded in plane. It intersects in the whole range
					// [0..1]
					embeddedInPlane = true;
					t0 = 0.0;
					t1 = 1.0;
				}
			} else {
				// N dot D is not 0. Calculate intersection interval:
				t0 = (-1.0 - signedDistToTrianglePlane) / normalDotVelocity;
				t1 = (1.0 - signedDistToTrianglePlane) / normalDotVelocity;
				// Swap so t0 < t1
				if (t0 > t1) {
					double temp = t1;
					t1 = t0;
					t0 = temp;
				}
				// Check that at least one result is within range:
				if (t0 > 1.0f || t1 < 0.0f) {
					// Both t values are outside values [0,1] No collision possible:
					return;
				}
				// Clamp to [0,1]
				if (t0 < 0.0)
					t0 = 0.0;
				if (t1 < 0.0)
					t1 = 0.0;
				if (t0 > 1.0)
					t0 = 1.0;
				if (t1 > 1.0)
					t1 = 1.0;
			}

			Vector3f collisionPoint = null;
			boolean foundCollison = false;
			float t = 1.0f;

			if (!embeddedInPlane) {
				Vector3f planeIntersectionPoint = Vector3f.add(Vector3f.sub(Collision.basePoint, trianglePlane.normal, null), (Vector3f) Collision.velocity.scale((float) t0), null);
				if (pointInTriangle(planeIntersectionPoint, triangle)) {
					foundCollison = true;
					t = (float) t0;
					collisionPoint = planeIntersectionPoint;
				}
			}

			if (foundCollison == false) {
				Vector3f velocity = new Vector3f(Collision.velocity);
				Vector3f base = new Vector3f(Collision.basePoint);
				float velocitySquaredLength = velocity.lengthSquared();
				float a, b, c;
				Maths.root = 0;
				// Check against points:
				a = velocitySquaredLength;
				// P1
				b = (float) (2.0 * (Vector3f.dot(velocity, Vector3f.sub(base, p1, null))));
				c = (float) (Vector3f.sub(p1, base, null).lengthSquared() - 1.0);
				if (Maths.getLowestRoot(a, b, c, t)) {
					t = Maths.root;
					foundCollison = true;
					collisionPoint = p1;
				}
				// P2
				b = (float) (2.0 * (Vector3f.dot(velocity, Vector3f.sub(base, p2, null))));
				c = (float) (Vector3f.sub(p2, base, null).lengthSquared() - 1.0);
				if (Maths.getLowestRoot(a, b, c, t)) {
					t = Maths.root;
					foundCollison = true;
					collisionPoint = p2;
				}
				// P3
				b = (float) (2.0 * (Vector3f.dot(velocity, Vector3f.sub(base, p3, null))));
				c = (float) (Vector3f.sub(p3, base, null).lengthSquared() - 1.0);
				if (Maths.getLowestRoot(a, b, c, t)) {
					t = Maths.root;
					foundCollison = true;
					collisionPoint = p3;
				}
				// Check against edges:
				// p1 -> p2:
				Vector3f edge = Vector3f.sub(p2, p1, null);
				Vector3f baseToVertex = Vector3f.sub(p1, base, null);
				float edgeSquaredLength = edge.lengthSquared();
				float edgeDotVelocity = Vector3f.dot(edge, velocity);
				float edgeDotBaseToVertex = Vector3f.dot(edge, baseToVertex);
				// Calculate parameters for equation
				a = edgeSquaredLength * -velocitySquaredLength + edgeDotVelocity * edgeDotVelocity;
				b = (float) (edgeSquaredLength * (2 * Vector3f.dot(velocity, baseToVertex)) - 2.0 * edgeDotVelocity * edgeDotBaseToVertex);
				c = edgeSquaredLength * (1 - baseToVertex.lengthSquared()) + edgeDotBaseToVertex * edgeDotBaseToVertex;
				// Does the swept sphere collide against infinite edge?
				if (Maths.getLowestRoot(a, b, c, t)) {
					// Check if intersection is within line segment:
					float f = (edgeDotVelocity * Maths.root - edgeDotBaseToVertex) / edgeSquaredLength;
					if (f >= 0.0 && f <= 1.0) {
						// intersection took place within segment.
						t = Maths.root;
						foundCollison = true;
						collisionPoint = Vector3f.add(p1, (Vector3f) edge.scale(f), null);
					}
				}
				// p2 -> p3:
				edge = Vector3f.sub(p3, p2, null);
				baseToVertex = Vector3f.sub(p2, base, null);
				edgeSquaredLength = edge.lengthSquared();
				edgeDotVelocity = Vector3f.dot(edge, velocity);
				edgeDotBaseToVertex = Vector3f.dot(edge, baseToVertex);
				a = edgeSquaredLength * -velocitySquaredLength + edgeDotVelocity * edgeDotVelocity;
				b = (float) (edgeSquaredLength * (2 * Vector3f.dot(velocity, baseToVertex)) - 2.0 * edgeDotVelocity * edgeDotBaseToVertex);
				c = edgeSquaredLength * (1 - baseToVertex.lengthSquared()) + edgeDotBaseToVertex * edgeDotBaseToVertex;
				if (Maths.getLowestRoot(a, b, c, t)) {
					float f = (edgeDotVelocity * Maths.root - edgeDotBaseToVertex) / edgeSquaredLength;
					if (f >= 0.0 && f <= 1.0) {
						t = Maths.root;
						foundCollison = true;
						collisionPoint = Vector3f.add(p2, (Vector3f) edge.scale(f), null);
					}
				}
				// p3 -> p1:
				edge = Vector3f.sub(p1, p3, null);
				baseToVertex = Vector3f.sub(p3, base, null);
				edgeSquaredLength = edge.lengthSquared();
				edgeDotVelocity = Vector3f.dot(edge, velocity);
				edgeDotBaseToVertex = Vector3f.dot(edge, baseToVertex);
				a = edgeSquaredLength * -velocitySquaredLength + edgeDotVelocity * edgeDotVelocity;
				b = (float) (edgeSquaredLength * (2 * Vector3f.dot(velocity, baseToVertex)) - 2.0 * edgeDotVelocity * edgeDotBaseToVertex);
				c = edgeSquaredLength * (1 - baseToVertex.lengthSquared()) + edgeDotBaseToVertex * edgeDotBaseToVertex;
				if (Maths.getLowestRoot(a, b, c, t)) {
					float f = (edgeDotVelocity * Maths.root - edgeDotBaseToVertex) / edgeSquaredLength;
					if (f >= 0.0 && f <= 1.0) {
						t = Maths.root;
						foundCollison = true;
						collisionPoint = Vector3f.add(p2, (Vector3f) edge.scale(f), null);
					}
				}
			}
			// Set result:
			if (foundCollison == true) {
				// distance to collision: ’t’ is time of collision
				float distToCollision = t * Collision.velocity.length();
				// Does this triangle qualify for the closest hit?
				// it does if it’s the first hit or the closest
				if (Collision.foundCollision == false || distToCollision < Collision.nearestDistance) {
					// Collision information nessesary for sliding
					Collision.nearestDistance = distToCollision;
					Collision.intersectionPoint = collisionPoint;
					Collision.foundCollision = true;
				}
			}
		}
	}

}
