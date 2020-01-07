package entities;

import java.io.Serializable;

import org.lwjgl.util.vector.Vector3f;

import entitySheets.ItemSheet;

public class Item extends Entity implements Serializable {

	private static final long serialVersionUID = 1L;

	public Item(ItemSheet sheet, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		super(sheet, position, rotX, rotY, rotZ, scale);
	}

}
