package entities;

import java.io.Serializable;

import org.lwjgl.util.vector.Vector3f;

import entitySheets.FloorSheet;

public class Floor extends Entity implements Serializable {

	private static final long serialVersionUID = 1L;

	public Floor(FloorSheet sheet, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		super(sheet, position, rotX, rotY, rotZ, scale);
	}

}
