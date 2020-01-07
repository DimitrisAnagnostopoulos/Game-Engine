package entities;

import java.io.Serializable;

import org.lwjgl.util.vector.Vector3f;

import entitySheets.ScenerySheet;

public class Scenery extends Entity implements Serializable {

	private static final long serialVersionUID = 1L;

	public Scenery(ScenerySheet sheet, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		super(sheet, position, rotX, rotY, rotZ, scale);
	}

}