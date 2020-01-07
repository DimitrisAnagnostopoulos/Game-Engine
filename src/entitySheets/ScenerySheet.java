package entitySheets;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "scenery")
public class ScenerySheet extends EntitySheet implements Serializable {

	private static final long serialVersionUID = 1L;

	public ScenerySheet() {
		super();
	}

}
