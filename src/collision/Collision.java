package collision;

import org.lwjgl.util.vector.Vector3f;

import entities.Entity;
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
	public static boolean stuck;
	public static Vector3f lastSafePosition;
	public static int collisionRecursionDepth;

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
		  
		// intersection data
		Vector3f sIPoint = new Vector3f();    // sphere intersection point
		Vector3f pIPoint = new Vector3f();    // plane intersection point 	
		Vector3f polyIPoint = new Vector3f(); // polygon intersection point
		
		// how long is our velocity
		float distanceToTravel = velocity.length();	 
		float distToPlaneIntersection;
		float distToEllipsoidIntersection;
		
		// Make the plane containing this triangle.      
	    Vector3f pOrigin = p1;
	    Vector3f v1 = Vector3f.sub(p2, p1, null);
	    Vector3f v2 = Vector3f.sub(p3, p1, null);
	                         
		// determine normal to plane containing polygon  
	    Vector3f pNormal = Vector3f.cross(v1, v2, null);
		pNormal.normalise();
		  
		// calculate sphere intersection point
		sIPoint = Vector3f.sub(basePoint, pNormal, null);     
		  
		// classify point to determine if ellipsoid span the plane
		String pClass = Maths.classifyPoint(sIPoint, pOrigin, pNormal);
		  
		  
		// find the plane intersection point
		if (pClass == "PLANE_BACKSIDE") { // plane is embedded in ellipsoid
			return;
		    
			// find plane intersection point by shooting a ray from the 
		    // sphere intersection point along the planes normal.
			//distToPlaneIntersection = Maths.intersectRayPlane(sIPoint, pNormal, pOrigin, pNormal);
		    
		    // calculate plane intersection point
		    //pIPoint.x = sIPoint.x + distToPlaneIntersection * pNormal.x; 
		    //pIPoint.y = sIPoint.y + distToPlaneIntersection * pNormal.y; 
		    //pIPoint.z = sIPoint.z + distToPlaneIntersection * pNormal.z; 	
		    
		}else { 
		    
			// shoot ray along the velocity vector
			distToPlaneIntersection = Maths.intersectRayPlane(sIPoint, normalizedVelocity, pOrigin, pNormal);
		    
		    // calculate plane intersection point
		    pIPoint.x = sIPoint.x + distToPlaneIntersection * normalizedVelocity.x; 
		    pIPoint.y = sIPoint.y + distToPlaneIntersection * normalizedVelocity.y; 
		    pIPoint.z = sIPoint.z + distToPlaneIntersection * normalizedVelocity.z; 	
		    
		}
		  
		// find polygon intersection point. By default we assume its equal to the 
		// plane intersection point
		  
		polyIPoint = pIPoint;
		distToEllipsoidIntersection = distToPlaneIntersection;
		  
		// if not in triangle. eg, it's on the plane, but outside the triangle bounds.
		if (!Maths.CheckPointInTriangle(pIPoint,p1,p2,p3)) {
			  
			polyIPoint = Maths.closestPointOnTriangle(p1, p2, p3, pIPoint);
			Vector3f invertedNormalizedVelocity = (Vector3f) new Vector3f(normalizedVelocity).negate();
			distToEllipsoidIntersection = Maths.intersectRaySphere(polyIPoint, invertedNormalizedVelocity, basePoint, 1.0f);  
			  
			if (distToEllipsoidIntersection > 0) { 	
				// calculate true sphere intersection point
				sIPoint.x = polyIPoint.x + distToEllipsoidIntersection * invertedNormalizedVelocity.x;
				sIPoint.y = polyIPoint.y + distToEllipsoidIntersection * invertedNormalizedVelocity.y;
				sIPoint.z = polyIPoint.z + distToEllipsoidIntersection * invertedNormalizedVelocity.z;
			}
		    
		}
		
		// Here we do the error checking to see if we got ourself stuck last frame
	     if (Maths.CheckPointInSphere(polyIPoint, basePoint, 1.0f)) {
	        stuck = true;
	     }
		  
		// Ok, now we might update the collision data if we hit something
		if ((distToEllipsoidIntersection > 0) && (distToEllipsoidIntersection <= distanceToTravel)) { 
			if ((foundCollision == false) || (distToEllipsoidIntersection < nearestDistance))  {
		      
				// if we are hit we have a closest hit so far. We save the information
				nearestDistance = distToEllipsoidIntersection;
				intersectionPoint = polyIPoint;
				foundCollision = true;
			}
		} 
	}

}
