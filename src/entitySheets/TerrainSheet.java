package entitySheets;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "terrain")
public class TerrainSheet extends EntitySheet implements Serializable {

	private static final long serialVersionUID = 1L;

	public TerrainSheet() {
		super();
	}

}
