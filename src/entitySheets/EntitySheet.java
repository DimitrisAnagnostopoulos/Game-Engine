package entitySheets;

import java.io.Serializable;

public class EntitySheet implements Serializable {

	private static final long serialVersionUID = 1L;
	public String name = "";
	public String model = "";
	public String boundingBox = "";
	public String texture = "";
	public boolean transparency = false;
	public boolean useFakeLighting = false;
	public float shineDamper = 10;
	public float reflectivity = 0.2f;

}