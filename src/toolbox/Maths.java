package toolbox;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;

public class Maths {

	public static float E = 0.00000001f;

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
	
	public static float intersectRayPlane(Vector3f rOrigin, Vector3f rVector, Vector3f pOrigin, Vector3f pNormal) {

		float d = - (Vector3f.dot(pNormal,pOrigin));
	
		float numer = Vector3f.dot(pNormal,rOrigin) + d;
		float denom = Vector3f.dot(pNormal,rVector);
	
		if (denom == 0){  // normal is orthogonal to vector, cant intersect
		return (-1.0f);
		}
	
		return -(numer / denom);	
	}
	
	public static float _plane_dist(Vector3f planeOrigin, Vector3f planeNormal, Vector3f point){
		float    sb, sn, sd;
	
		sn = -Vector3f.dot(planeNormal, Vector3f.sub(point, planeOrigin, null));
		sd = Vector3f.dot(planeNormal, planeNormal);
		sb = sn / sd;
	
		Vector3f B = Vector3f.add(point, (Vector3f) planeNormal.scale(sb), null);
		return Vector3f.sub(point, B, null).length();
	}
	
	public static float plane_dist(Vector3f planeOrigin, Vector3f planeNormal, Vector3f point){
		return (Vector3f.dot(point, planeNormal)) - (planeNormal.x * planeOrigin.x + planeNormal.y * planeOrigin.y + planeNormal.z * planeOrigin.z);
	}

	public static float intersectRaySphere(Vector3f rO, Vector3f rV, Vector3f sO, float sR) {

		Vector3f Q = Vector3f.sub(sO, rO, null);
	
		float c = Q.length();
		float v = Vector3f.dot(Q,rV);
		float d = sR*sR - (c*c - v*v);
	
		// If there was no intersection, return -1
		if (d < 0.0) return (-1.0f);
	
		// Return the distance to the [first] intersecting point
		return (float) (v - Math.sqrt(d));
	}

	public static boolean _CheckPointInTriangle(Vector3f point, Vector3f pa, Vector3f pb, Vector3f pc) {

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
	
	public static boolean CheckPointInTriangle(Vector3f point, Vector3f a, Vector3f b, Vector3f c) {

		float total_angles = 0.0f;
	
		// make the 3 vectors
		Vector3f v1 = Vector3f.sub(point, a, null);
		Vector3f v2 = Vector3f.sub(point, b, null);
		Vector3f v3 = Vector3f.sub(point, c, null);
	
		v1.normalise();
		v2.normalise();
		v3.normalise();
	
		total_angles += Math.acos(Vector3f.dot(v1,v2));   
		total_angles += Math.acos(Vector3f.dot(v2,v3));
		total_angles += Math.acos(Vector3f.dot(v3,v1)); 
	
		if (Math.abs(total_angles-2 * Math.PI) <= 0.005)
		return (true);
	
		return (false);
	}

	public static Vector3f closestPointOnLine(Vector3f a, Vector3f b, Vector3f p) {

		// Determine t (the length of the vector from ‘a’ to ‘p’)
		Vector3f c = Vector3f.sub(p, a, null);
		Vector3f V = Vector3f.sub(b, a, null); 
	
		float d = V.length();
	
		V.normalise();  
		float t = Vector3f.dot(V,c);
	
	
		// Check to see if ‘t’ is beyond the extents of the line segment
		if (t < 0.0f) return (a);
		if (t > d) return (b);
	
	
		// Return the point between ‘a’ and ‘b’
		//set length of V to t. V is normalized so this is easy
		V.x = V.x * t;
		V.y = V.y * t;
		V.z = V.z * t;
	
		return Vector3f.add(a, V, null);	
	}

	public static Vector3f closestPointOnTriangle(Vector3f a, Vector3f b, Vector3f c, Vector3f p) {

		Vector3f Rab = closestPointOnLine(a, b, p);
		Vector3f Rbc = closestPointOnLine(b, c, p);
		Vector3f Rca = closestPointOnLine(c, a, p);
	
		float dAB = Vector3f.sub(p, Rab, null).length();
		float dBC = Vector3f.sub(p, Rbc, null).length();
		float dCA = Vector3f.sub(p, Rca, null).length();
	
	
		float min = dAB;
		Vector3f result = Rab;
	
		if (dBC < min) {
		min = dBC;
		result = Rbc;
		}
	
		if (dCA < min)
		result = Rca;
	
	
		return (result);	
	}

	public static boolean CheckPointInSphere(Vector3f point, Vector3f sO, float sR) {

		float d = Vector3f.sub(point, sO, null).length();

		if(d<= sR) return true;
			return false;	
	}
	
	Vector3f tangentPlaneNormalOfEllipsoid(Vector3f point, Vector3f eO, Vector3f eR) {
	
		Vector3f p = Vector3f.sub(point, eO, null);
	
		float a2 = eR.x * eR.x;
		float b2 = eR.y * eR.y;
		float c2 = eR.z * eR.z;
	
	
		Vector3f res = new Vector3f();
		res.x = p.x / a2;
		res.y = p.y / b2;
		res.z = p.z / c2;
	
		res.normalise();	
		return (res);	
	}

	public static String classifyPoint(Vector3f point, Vector3f pO, Vector3f pN) {
		Vector3f dir = Vector3f.sub(pO, point, null);
		float d = Vector3f.dot(dir, pN);
	
		if (d < -0.001f)
		return "PLANE_FRONT";	
		else
		if (d > 0.001f)
		return "PLANE_BACKSIDE";	
	
		return "ON_PLANE";	
	}

}
