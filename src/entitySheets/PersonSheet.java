package entitySheets;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "person")
public class PersonSheet extends EntitySheet implements Serializable {

	private static final long serialVersionUID = 1L;

	public PersonSheet() {
		super();
	}

}
