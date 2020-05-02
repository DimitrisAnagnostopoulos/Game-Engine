package entities;

import java.io.Serializable;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import collision.Collision;
import entitySheets.EntitySheet;
import game.Handler;
import models.Boundaries;
import renderEngine.DisplayManager;
import traits.Collidable;

public class Player extends Entity implements Serializable, Collidable {

	private static final long serialVersionUID = 1L;
	private static final float RUN_SPEED = 80;
	private static final float TURN_SPEED = 160;
	private static final float JUMP_POWER = 50;

	private boolean isInAir = false;

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
		if (this.getPosition().y < terrainHeight) {
			upwardsSpeed = 0;
			isInAir = false;
			dy = terrainHeight - super.getPosition().y;
		}
		this.setVelocity(new Vector3f(dx, dy, dz));
		
		Boundaries b = this.getAbsoluteBoundingBox().getBoundaries();
		eRadius.x = (b.maxX - b.minX) / 2;
		eRadius.y = (b.maxY - b.minY) / 2;
		eRadius.z = (b.maxZ - b.minZ) / 2;
		Collision.stuck = false;
		Collision.collidedAtAnyPoint = false;
		this.setPosition(collideAndSlide(this.getPosition(), this.getVelocity()));
		if (Collision.stuck || (Collision.collidedAtAnyPoint && this.getPosition().getY() < terrainHeight - 0.1f)) {
			this.setPosition(Collision.lastSafePosition);
		} else {
			Collision.lastSafePosition = this.getPosition();
		}
		
		Handler.refreshGeometry(this);
	}

	private void jump() {
		if (!isInAir || true) {
			upwardsSpeed = JUMP_POWER;
			isInAir = true;
		}
	}

	private void checkInputs() {
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			this.setCurrentSpeed(RUN_SPEED);
			this.getModel().doAnimation("model.dae");
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			this.setCurrentSpeed(-RUN_SPEED);
		} else {
			this.setCurrentSpeed(0);
			this.getModel().stopAnimation();
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