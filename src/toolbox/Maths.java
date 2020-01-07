package toolbox;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;

public class Maths {

	public static float E = 0.00000001f;
	public static float root;

	public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, float rz, float scale) {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rx), new Vector3f(1, 0, 0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0, 1, 0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rz), new Vector3f(0, 0, 1), matrix, matrix);
		Matrix4f.scale(new Vector3f(scale, scale, scale), matrix, matrix);
		return matrix;
	}

	public static Matrix4f createViewMatrix(Camera camera) {
		Matrix4f viewMatrix = new Matrix4f();
		viewMatrix.setIdentity();
		Matrix4f.rotate((float) Math.toRadians(camera.getPitch()), new Vector3f(1, 0, 0), viewMatrix, viewMatrix);
		Matrix4f.rotate((float) Math.toRadians(camera.getYaw()), new Vector3f(0, 1, 0), viewMatrix, viewMatrix);
		Matrix4f.rotate((float) Math.toRadians(camera.getRoll()), new Vector3f(0, 0, 1), viewMatrix, viewMatrix);
		Vector3f cameraPos = camera.getPosition();
		Vector3f negativeCameraPos = new Vector3f(-cameraPos.x, -cameraPos.y, -cameraPos.z);
		Matrix4f.translate(negativeCameraPos, viewMatrix, viewMatrix);
		return viewMatrix;
	}

	public static Vector3f transformVectorThroughMatrix(Vector3f vector, Matrix4f matrix) {
		Vector4f translated = Matrix4f.transform(matrix, new Vector4f(vector.x, vector.y, vector.z, 1), null);
		Vector3f newVector = new Vector3f(translated.x, translated.y, translated.z);
		return newVector;
	}

	public static boolean getLowestRoot(float a, float b, float c, float maxR) {
		// Check if a solution exists
		float determinant = b * b - 4.0f * a * c;
		// If determinant is negative it means no solutions.
		if (determinant < 0.0f)
			return false;
		// calculate the two roots:
		float sqrtD = (float) Math.sqrt(determinant);
		float r1 = (-b - sqrtD) / (2 * a);
		float r2 = (-b + sqrtD) / (2 * a);
		// Sort so x1 <= x2
		if (r1 > r2) {
			float temp = r2;
			r2 = r1;
			r1 = temp;
		}
		// Get lowest root:
		if (r1 > 0 && r1 < maxR) {
			root = r1;
			return true;
		}
		// It is possible that we want x2 - this can happen
		// if x1 < 0
		if (r2 > 0 && r2 < maxR) {
			root = r2;
			return true;
		}
		// No (valid) solutions
		return false;
	}

	public static float distanceBetweenPoints(Vector3f a, Vector3f b) {
		return Vector3f.sub(a, b, null).length();
	}

	public static float distanceBetween2DPoints(Vector2f a, Vector2f b) {
		return Vector2f.sub(a, b, null).length();
	}

	public static float angleBetween2DPoints(Vector2f a, Vector2f b) {
		return (float) Math.toDegrees(Math.atan2(a.x * b.y - a.y * b.x, a.x * b.x + a.y * b.y));
	}

	public static float angleOfLine(Vector2f a, Vector2f b) {
		return (float) Math.toDegrees(Math.atan2(a.x - b.x, a.y - b.y));
	}

	public static Vector2f get2D(Vector3f point) {
		return new Vector2f(point.x, point.z);
	}

}
