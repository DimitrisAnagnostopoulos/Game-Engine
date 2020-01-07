package entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import entitySheets.PersonSheet;
import game.Handler;
import geometry.Node;
import pathfinding.Pathfinder;
import renderEngine.DisplayManager;
import toolbox.Maths;

public class Person extends Entity implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final float RUN_SPEED = 20;

	public Pathfinder pathfinder;

	public Person(PersonSheet sheet, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		super(sheet, position, rotX, rotY, rotZ, scale);
	}

	public void update() {
		super.update();
		followPlayer();
		move();
	}

	public void move() {
		if (super.pathfinder != null && super.pathfinder.points.isEmpty() == false) {
			super.pathfinder.followPath();
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
			dy = terrainHeight - super.getPosition().y;
		}
		super.setVelocity(new Vector3f(dx, dy, dz));
		super.move();
	}

	private void followPlayer() {
		if (super.pathfinder != null && this.floor != null && this.triangle != null && Handler.player.triangle != null
				&& this.floor == Handler.player.floor) {
			super.pathfinder.findPath((Node) this.triangle, (Node) Handler.player.triangle, this.getPosition(),
					Handler.player.getPosition());
			boolean collides = false;
			List<Entity> persons = new ArrayList<>();
			persons.addAll(Handler.getEntities("Person"));
			persons.add(Handler.player);
			for (Entity person : persons) {
				if (person == this)
					continue;
				float distance = RUN_SPEED;
				float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));
				float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));
				Vector3f futurePosition = Vector3f.add(this.getPosition(), new Vector3f(dx, 0, dz), null);
				if (Maths.distanceBetweenPoints(futurePosition, person.getPosition()) < this.getRadius()
						+ person.getRadius()
						&& Maths.distanceBetweenPoints(futurePosition, person.getPosition()) < Maths
								.distanceBetweenPoints(this.getPosition(), person.getPosition())) {
					collides = true;
					break;
				}
			}
			if (!super.pathfinder.points.isEmpty() && !collides) {
				this.setCurrentSpeed(RUN_SPEED);
			} else {
				this.setCurrentSpeed(0);
				this.setCurrentTurnSpeed(0);
			}
		} else {
			this.setCurrentSpeed(0);
			this.setCurrentTurnSpeed(0);
		}
	}

}
