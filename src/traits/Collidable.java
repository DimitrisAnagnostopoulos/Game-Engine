package traits;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import collision.Collision;
import entities.Entity;
import game.Handler;
import geometry.Triangle;
import toolbox.Maths;

public interface Collidable {
	
	static Vector3f eRadius = new Vector3f();
	
	default Vector3f collideAndSlide(Vector3f position, Vector3f velocity) {
		if (velocity.length() == 0) return position;
		Vector3f eSpacePosition = new Vector3f(position.x / eRadius.x, (position.y + eRadius.y) / eRadius.y, position.z / eRadius.z);
		Vector3f eSpaceVelocity = new Vector3f(velocity.x / eRadius.x, velocity.y / eRadius.y, velocity.z / eRadius.z);
		Collision.collisionRecursionDepth = 0;
		Vector3f finalPosition = this.collideWithWorld(eSpacePosition, eSpaceVelocity);
		finalPosition.set(finalPosition.x * eRadius.x, (finalPosition.y * eRadius.y) - eRadius.y, finalPosition.z * eRadius.z);
		return finalPosition;
	}
	
	default Vector3f collideWithWorld(Vector3f pos, Vector3f vel) {
		Collision.lastSafePosition = Vector3f.add(pos, vel, null);
		
		float veryCloseDistance = 0.001f;
		if (Collision.collisionRecursionDepth > 5) {
			return pos;
		}
		
		Vector3f dest = Vector3f.add(pos, vel, null);
		Vector3f firstPlaneOrigin = null;
		Vector3f firstPlaneNormal = null;
		
		for (int i = 0; i < 3; ++i) {
			if (vel.length() == 0) return pos;
			Collision.velocity = new Vector3f(vel);
			Collision.normalizedVelocity = (Vector3f) new Vector3f(vel).normalise();
			Collision.basePoint = new Vector3f(pos);
			Collision.foundCollision = false;
			Collision.nearestDistance = -1;
			
			checkCollision();
			
			if (Collision.foundCollision == false) return dest;
			
			if (Collision.stuck) {
			    //return Collision.lastSafePosition;
			}else{
				Collision.lastSafePosition = pos;
			}
			
			float dist = (float) Collision.nearestDistance;
			float short_dist = (float) Math.max(dist - veryCloseDistance, 0.0f);
			
			Vector3f touchPoint = Vector3f.add(pos, (Vector3f) new Vector3f(Collision.normalizedVelocity).scale(dist), null);
			pos = Vector3f.add(pos, (Vector3f) new Vector3f(Collision.normalizedVelocity).scale(short_dist), null);
			
			if (i == 0) {
				float long_radius = 1.0f + veryCloseDistance;
				firstPlaneOrigin = new Vector3f(Collision.intersectionPoint);
				firstPlaneNormal = Vector3f.sub(touchPoint, Collision.intersectionPoint, null);
				float pdist = Maths.plane_dist(firstPlaneOrigin, firstPlaneNormal, dest);
				float magnitude = pdist - long_radius;
				dest = Vector3f.sub(dest, (Vector3f) firstPlaneNormal.scale(magnitude), null);
				vel = Vector3f.sub(dest, pos, null);
			} else if (i == 1) {
				@SuppressWarnings("unused")
				Vector3f secondPlaneOrigin = new Vector3f(Collision.intersectionPoint);
				Vector3f secondPlaneNormal = Vector3f.sub(touchPoint, Collision.intersectionPoint, null);
				Vector3f crease = (Vector3f) Vector3f.cross(firstPlaneNormal, secondPlaneNormal, null);
				if (crease.length() > 0.00000001f) {
					crease.normalise();
					float dis = Vector3f.dot(Vector3f.sub(dest, pos, null), crease);
					vel = (Vector3f) crease.scale(dis);
					dest = Vector3f.add(pos, vel, null);
				}
			}
		}

		if (vel.length() < veryCloseDistance) {
			return pos;
		}
		Collision.collisionRecursionDepth++;
		return collideWithWorld(pos, vel);
	}

	default void checkCollision() {
		List<Entity> entities = new ArrayList<>();
		entities.addAll(Handler.getEntities("Scenery"));
		entities.addAll(Handler.getEntities("Person"));
		for (Entity entity : entities) {
			if (entity.getAbsoluteBoundingBox() != null /* && Collision.boundingBoxCollision((Entity) this, entity) */) {
				if (entity.getTriangles() != null) {
					for (Triangle triangle : entity.getTriangles()) {
						Vector3f p1 = new Vector3f(triangle.vertices.get(0));
						p1.set(p1.x / eRadius.x, p1.y / eRadius.y, p1.z / eRadius.z);
						Vector3f p2 = new Vector3f(triangle.vertices.get(1));
						p2.set(p2.x / eRadius.x, p2.y / eRadius.y, p2.z / eRadius.z);
						Vector3f p3 = new Vector3f(triangle.vertices.get(2));
						p3.set(p3.x / eRadius.x, p3.y / eRadius.y, p3.z / eRadius.z);
						Collision.checkTriangle(new Triangle(p1, p2, p3));
					}
				}
			}
		}

	}
	
}
