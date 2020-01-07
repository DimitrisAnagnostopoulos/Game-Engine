package entities;

import models.Boundaries;
import models.RawModel;
import models.Model;
import pathfinding.Pathfinder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;

import collision.Collision;
import entitySheets.EntitySheet;
import game.Handler;
import geometry.Node;
import geometry.Triangle;

public class Entity implements Serializable {

	private static final long serialVersionUID = 1L;
	protected static final float MINIMUM_TERRAIN_HEIGHT = 0;
	protected static final float GRAVITY = -300;
	private static final float STEP = 100;
	private Model model;

	private Vector3f position;
	private float rotX, rotY, rotZ;
	private float scale;
	private EntitySheet sheet;
	private Vector3f velocity;

	protected float currentSpeed = 0;
	protected float currentTurnSpeed = 0;
	protected float upwardsSpeed = 0;
	protected List<Triangle> triangles;
	private Map<Vector3f, List<Triangle>> trianglesToVerticesMap;

	public Floor floor;
	public Triangle triangle;
	public Pathfinder pathfinder;

	public Entity(EntitySheet sheet, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		this.sheet = sheet;
		this.model = null;
		this.position = position;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.scale = scale;
	}

	public Entity(Model model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		this.model = model;
		this.position = position;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.scale = scale;
	}

	public void increasePosition(float dx, float dy, float dz) {
		this.position.x += dx;
		this.position.y += dy;
		this.position.z += dz;
	}

	public void increaseRotation(float dx, float dy, float dz) {
		this.rotX += dx;
		this.rotY += dy;
		this.rotZ += dz;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public float getRotX() {
		return rotX;
	}

	public void setRotX(float rotX) {
		this.rotX = rotX;
	}

	public float getRotY() {
		return rotY;
	}

	public void setRotY(float rotY) {
		this.rotY = rotY;
	}

	public float getRotZ() {
		return rotZ;
	}

	public void setRotZ(float rotZ) {
		this.rotZ = rotZ;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public EntitySheet getSheet() {
		return sheet;
	}

	public void setSheet(EntitySheet sheet) {
		this.sheet = sheet;
	}

	public float getCurrentSpeed() {
		return currentSpeed;
	}

	public void setCurrentSpeed(float currentSpeed) {
		this.currentSpeed = currentSpeed;
	}

	public float getCurrentTurnSpeed() {
		return currentTurnSpeed;
	}

	public void setCurrentTurnSpeed(float currentTurnSpeed) {
		this.currentTurnSpeed = currentTurnSpeed;
	}

	public void update() {
		this.getModel().update();
	}

	public void move() {
		this.increasePosition(getVelocity().x, getVelocity().y, getVelocity().z);
		Handler.refreshGeometry(this);
	}

	public Vector3f getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector3f velocity) {
		this.velocity = velocity;
	}

	public List<Triangle> getTriangles() {
		return triangles;
	}

	public void setTriangles(List<Triangle> triangles) {
		this.triangles = triangles;
	}

	public Map<Vector3f, List<Triangle>> getTrianglesToVerticesMap() {
		return trianglesToVerticesMap;
	}

	public void setTrianglesToVerticesMap(Map<Vector3f, List<Triangle>> trianglesToVerticesMap) {
		this.trianglesToVerticesMap = trianglesToVerticesMap;
	}
	
	public RawModel getRawModel() {
		if (this.getModel() != null) {
			return this.getModel().getRawModel();
		} else {
			return null;
		}
	}

	public void setRawModel(RawModel rawModel) {
		if (this.getModel() != null) {
			this.getModel().setRawModel(rawModel);
		}
	}
	
	public RawModel getBoundingBox() {
		if (this.getModel() != null) {
			return this.getModel().getBoundingBox();
		} else {
			return null;
		}
	}

	public void setBoundingBox(RawModel boundingBox) {
		if (this.getModel() != null) {
			this.getModel().setBoundingBox(boundingBox);
		}
	}

	public RawModel getAbsoluteBoundingBox() {
		if (this.getModel() != null) {
			return this.getModel().getAbsoluteBoundingBox();
		} else {
			return null;
		}
	}

	public void setAbsoluteBoundingBox(RawModel absoluteBoundingBox) {
		if (this.getModel() != null) {
			this.getModel().setAbsoluteBoundingBox(absoluteBoundingBox);
		}
	}

	public float findTerrainHeight() {
		Vector3f position = this.getPosition();
		this.floor = this.findFloor();
		if (this.floor != null && this.floor.getTriangles() != null) {
			List<Triangle> triangles = this.floor.getTriangles();
			if (this.triangle != null && triangles.contains(this.triangle) && Collision.pointIn2DTriangle(position, this.triangle)) {
				float height = Collision.heightFromCoordsInsideTriangle(position, this.triangle);
				if (height < position.y + STEP) {
					return height;
				}
				this.triangle = null;
			} else {
				this.triangle = null;
				for (Triangle triangle : triangles) {
					if (Collision.pointIn2DTriangle(position, triangle)) {
						float height = Collision.heightFromCoordsInsideTriangle(position, triangle);
						if (height < position.y + STEP) {
							this.triangle = triangle;
							return height;
						}
					}
				}
			}
		}
		return 0;
	}

	public Floor findFloor() {
		Vector3f position = this.getPosition();
		if (this.floor != null && this.floor.getBoundingBox() != null && Collision.pointInAreaOfBoundingBox(position, this.floor)) {
			return this.floor;
		} else if (Handler.getEntities().get(Floor.class) != null) {
			this.floor = null;
			this.triangle = null;
			float highestMinYOfBoundingBoxes = -1000;
			for (Entity floor : Handler.getEntities().get(Floor.class)) {
				if (floor.getAbsoluteBoundingBox() != null && Collision.pointInAreaOfBoundingBox(position, floor)) {
					float minY = floor.getAbsoluteBoundingBox().getBoundaries().minY;
					if (minY > highestMinYOfBoundingBoxes) {
						highestMinYOfBoundingBoxes = minY;
						this.floor = (Floor) floor;
					}
				}
			}
			return this.floor;
		}
		return null;
	}
	
	public Node getNode() {
		Node node = (Node) this.triangle;
		return node;
	}
	
	public float getRadius() {
		float radius = 0;
		if (this.getBoundingBox() != null) {
			Boundaries b = this.getAbsoluteBoundingBox().getBoundaries();
			radius = Math.max(Math.abs(b.maxX - b.minX), Math.abs(b.maxZ - b.minZ)) / 2;
		}
		return radius;
	}

}
