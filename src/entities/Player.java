package entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import collision.Collision;
import entitySheets.EntitySheet;
import game.Handler;
import geometry.Plane;
import geometry.Triangle;
import models.Boundaries;
import renderEngine.DisplayManager;

public class Player extends Entity implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final float RUN_SPEED = 80;
	private static final float TURN_SPEED = 160;
	private static final float JUMP_POWER = 50;
	private static final Vector3f eRadius = new Vector3f();

	private boolean isInAir = false;
	private int collisionRecursionDepth;

	public Player(EntitySheet sheet, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		super(sheet, position, rotX, rotY, rotZ, scale);
	}

	public void move() {
		checkInputs();
		if (this.pathfinder != null && this.pathfinder.points.isEmpty() == false) {
			this.pathfinder.followPath();
		}
		super.increaseRotation(0, currentTurnSpeed * DisplayManager.getDelta(), 0);
		float distance = getCurrentSpeed() * DisplayManager.getDelta();
		float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));
		float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));
		upwardsSpeed += GRAVITY * DisplayManager.getDelta();
		float dy = upwardsSpeed * DisplayManager.getDelta();
		float terrainHeight = super.findTerrainHeight();
		terrainHeight = (terrainHeight > MINIMUM_TERRAIN_HEIGHT) ? terrainHeight : MINIMUM_TERRAIN_HEIGHT;
		if (super.getPosition().y < terrainHeight) {
			upwardsSpeed = 0;
			isInAir = false;
			dy = terrainHeight - super.getPosition().y;
		}	
		super.setVelocity(new Vector3f(dx, dy, dz));
		collideAndSlide(new Vector3f(dx, dy, dz));
		Handler.refreshGeometry(this);
	}

	private void collideAndSlide(Vector3f velocity) {
		if (velocity.length() == 0) return;
		Boundaries b = this.getAbsoluteBoundingBox().getBoundaries();
		eRadius.x = (b.maxX - b.minX) / 2;
		eRadius.y = (b.maxY - b.minY) / 2;
		eRadius.z = (b.maxZ - b.minZ) / 2;
		Vector3f position = new Vector3f(super.getPosition());
		Vector3f eSpacePosition = new Vector3f(position.x / eRadius.x, (position.y + eRadius.y) / eRadius.y, position.z / eRadius.z);
		Vector3f eSpaceVelocity = new Vector3f(velocity.x / eRadius.x, velocity.y / eRadius.y, velocity.z / eRadius.z);

		collisionRecursionDepth = 0;
		Vector3f finalPosition = collideWithWorld(eSpacePosition, eSpaceVelocity);
		finalPosition.set(finalPosition.x * eRadius.x, (finalPosition.y * eRadius.y) - eRadius.y, finalPosition.z * eRadius.z);
		super.setPosition(finalPosition);
		this.getBoundingBox().basicColour = Collision.foundCollision ? new Vector3f(100, 0, 0) : null;
	}

	private Vector3f collideWithWorld(Vector3f pos, Vector3f vel) {
		float veryCloseDistance = 0.005f;
		if (collisionRecursionDepth > 5) {
			return pos;
		}
		
		Vector3f dest = Vector3f.add(pos, vel, null);
		Plane firstPlane = null;
		
		for (int i = 0; i < 3; ++i) {
			if (vel.length() == 0) return pos;
			Collision.velocity = new Vector3f(vel);
			Collision.normalizedVelocity = new Vector3f(vel);
			Collision.normalizedVelocity.normalise();
			Collision.basePoint = new Vector3f(pos);
			Collision.foundCollision = false;
			Collision.nearestDistance = -1;
			checkCollision();
			if (Collision.foundCollision == false) return dest;
			isInAir = false;
			upwardsSpeed = 0;
			float dist = (float) Collision.nearestDistance;
			float short_dist = (float) Math.max(dist - veryCloseDistance, 0.0f);
			Vector3f V = new Vector3f(vel);
			V.normalise().scale(dist);
			Vector3f touchPoint = Vector3f.add(pos, V, null);
			V = new Vector3f(vel);
			V.normalise().scale(short_dist);
			pos = Vector3f.add(pos, V, null);
			if (i == 0) {
				float long_radius = 1.0f + veryCloseDistance;
				Vector3f firstPlaneOrigin = new Vector3f(Collision.intersectionPoint);
				Vector3f firstPlaneNormal = Vector3f.sub(touchPoint, Collision.intersectionPoint, null);
				firstPlaneNormal.normalise();
				firstPlane = new Plane(firstPlaneOrigin, firstPlaneNormal);
				float magnitude = (float) (firstPlane.signedDistanceTo(dest) - long_radius);
				dest = Vector3f.sub(dest, (Vector3f) firstPlaneNormal.scale(magnitude), null);
				vel = Vector3f.sub(dest, pos, null);
			} else if (i == 1) {
				Vector3f secondPlaneOrigin = new Vector3f(touchPoint);
				Vector3f secondPlaneNormal = Vector3f.sub(touchPoint, Collision.intersectionPoint, null);
				secondPlaneNormal.normalise();
				Plane secondPlane = new Plane(secondPlaneOrigin, secondPlaneNormal);
				Vector3f crease = (Vector3f) Vector3f.cross(firstPlane.normal, secondPlane.normal, null);
				if (crease.length() > 0) {
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
		collisionRecursionDepth++;
		return collideWithWorld(pos, vel);
	}

	public void checkCollision() {
		List<Entity> entities = new ArrayList<>();
		entities.addAll(Handler.getEntities("Scenery"));
		entities.addAll(Handler.getEntities("Person"));
		for (Entity entity : entities) {
			if (entity.getAbsoluteBoundingBox() != null && Collision.boundingBoxCollision(this, entity)) {
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

	private void jump() {
		if (!isInAir) {
			this.upwardsSpeed = JUMP_POWER;
			isInAir = true;
		}
	}

	private void checkInputs() {
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			this.setCurrentSpeed(RUN_SPEED);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			this.setCurrentSpeed(-RUN_SPEED);
		} else {
			this.setCurrentSpeed(0);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			this.currentTurnSpeed = -TURN_SPEED;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			this.currentTurnSpeed = TURN_SPEED;
		} else {
			this.currentTurnSpeed = 0;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			jump();
		}
	}

	public void update() {
		super.update();
	}

}