package shaders;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import toolbox.Maths;

import entities.Camera;

public class BasicShader extends ShaderProgram {

	private static final String VERTEX_FILE = "src/shaders/basicVertexShader.txt";
	private static final String FRAGMENT_FILE = "src/shaders/basicFragmentShader.txt";

	private int location_BasicColour;
	private int location_projectionMatrix;
	private int location_viewMatrix;

	public BasicShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}

	@Override
	protected void getAllUniformLocations() {
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		location_BasicColour = super.getUniformLocation("basicColour");
	}

	public void loadBasicColour(Vector3f basicColour) {
		if (basicColour != null)
			super.loadVector(location_BasicColour, basicColour);
		else
			super.loadVector(location_BasicColour, new Vector3f(100, 100, 100));
	}

	public void loadViewMatrix(Camera camera) {
		Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		super.loadMatrix(location_viewMatrix, viewMatrix);
	}

	public void loadProjectionMatrix(Matrix4f projection) {
		super.loadMatrix(location_projectionMatrix, projection);
	}

}
