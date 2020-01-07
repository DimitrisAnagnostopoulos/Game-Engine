package entitySheets;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "floor")
public class FloorSheet extends EntitySheet implements Serializable {

	private static final long serialVersionUID = 1L;

	public FloorSheet() {
		super();
	}

}