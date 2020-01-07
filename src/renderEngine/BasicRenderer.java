package renderEngine;

import models.RawModel;
import models.Model;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import shaders.BasicShader;
import toolbox.OpenGlUtils;
import entities.Entity;

public class BasicRenderer {

	private BasicShader shader;

	public BasicRenderer(BasicShader shader, Matrix4f projectionMatrix) {
		this.shader = shader;
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}

	public void render(Map<Model, List<Entity>> entities) {
		try {
			OpenGlUtils.goWireframe(true);
			for (Model model : entities.keySet()) {
				List<Entity> batch = entities.get(model);
				for (Entity entity : batch) {

					if (entity.getBoundingBox() != null) {
						// Bounding boxes
						RawModel bb = entity.getBoundingBox();
						GL30.glBindVertexArray(bb.getVaoID());
						GL20.glEnableVertexAttribArray(0);
						shader.loadBasicColour(bb.basicColour);
						GL11.glDrawElements(GL11.GL_TRIANGLES, bb.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
						GL20.glDisableVertexAttribArray(0);
						GL30.glBindVertexArray(0);

						// Absolute coordinate axis aligned bounding boxes
						RawModel abb = entity.getAbsoluteBoundingBox();
						GL30.glBindVertexArray(abb.getVaoID());
						GL20.glEnableVertexAttribArray(0);
						GL11.glDrawElements(GL11.GL_TRIANGLES, abb.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
						GL20.glDisableVertexAttribArray(0);
						GL30.glBindVertexArray(0);

						// Paths
						if (entity.pathfinder != null) {
							RawModel path = entity.pathfinder.path;
							if (path != null) {
								GL30.glBindVertexArray(path.getVaoID());
								GL20.glEnableVertexAttribArray(0);
								GL11.glDrawElements(GL11.GL_LINE_STRIP, path.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
								GL20.glDisableVertexAttribArray(0);
								GL30.glBindVertexArray(0);
							}
						}
					}

				}
			}
			if (!MasterRenderer.wireframe) OpenGlUtils.goWireframe(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
