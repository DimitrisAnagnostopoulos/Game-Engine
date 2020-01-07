package entitySheets;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "item")
public class ItemSheet extends EntitySheet implements Serializable {

	private static final long serialVersionUID = 1L;

	public ItemSheet() {
		super();
	}

}
