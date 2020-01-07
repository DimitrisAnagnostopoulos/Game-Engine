package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class Camera {

	private Vector3f position = new Vector3f(200, 50, 200);
	private float pitch;
	private float yaw;
	private float roll;

	public Camera() {
	}

	public void move() {
		if (Mouse.isButtonDown(1)) {
			pitch -= Mouse.getDY() * 1f;
			yaw += Mouse.getDX() * 1f;
			if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
				roll = -20f;
			} else if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
				roll = +20f;
			} else {
				roll = 0f;
			}
		}
		float dx = (float) Math.sin(Math.toRadians(yaw));
		float dz = (float) Math.cos(Math.toRadians(180 - yaw));
		float dy = (float) Math.tan(Math.toRadians(180 - pitch));
		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			increasePosition(dx, dy, dz);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			increasePosition(-dx, -dy, -dz);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			increasePosition((float) Math.sin(Math.toRadians(-yaw - 90)), 0, (float) Math.cos(Math.toRadians(-yaw - 90)));
		} else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			increasePosition((float) Math.sin(Math.toRadians(90 - yaw)), 0, (float) Math.cos(Math.toRadians(90 - yaw)));
		}
	}

	public void increasePosition(float dx, float dy, float dz) {
		this.position.x += dx;
		this.position.y += dy;
		this.position.z += dz;
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public float getRoll() {
		return roll;
	}

}
