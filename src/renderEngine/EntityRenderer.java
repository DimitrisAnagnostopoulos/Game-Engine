package renderEngine;

import models.RawModel;
import models.Model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import shaders.StaticShader;
import textures.ModelTexture;
import toolbox.Maths;
import toolbox.OpenGlUtils;
import entities.Entity;

public class EntityRenderer {

	private StaticShader shader;

	public EntityRenderer(StaticShader shader, Matrix4f projectionMatrix) {
		this.shader = shader;
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}

	public void render(Map<Model, List<Entity>> entities) {
		for (Model model : entities.keySet()) {
			int[] materialChangeIndices = model.getRawModel().getMaterialChangeIndices();
			int totalCount = model.getRawModel().getVertexCount();
			if (materialChangeIndices == null || materialChangeIndices.length == 0) materialChangeIndices = new int[]{0};
			if (materialChangeIndices.length > 1) System.out.println(" totalCount "+totalCount+" changes "+Arrays.toString(materialChangeIndices));
			for (int i = 0; i < materialChangeIndices.length; i++) {
				int offset = materialChangeIndices[i];
				int count = 0;
				if (i+1 < materialChangeIndices.length) {
					count = materialChangeIndices[i+1] - materialChangeIndices[i];
				} else if (totalCount > materialChangeIndices[i]) {
					count = totalCount - materialChangeIndices[i];
				} else {
					continue;
				}
				if (materialChangeIndices.length > 1) System.out.println("i: "+i+" count "+count+" offset "+offset);
				prepareTexturedModel(model, i);
				List<Entity> batch = entities.get(model);
				for (Entity entity : batch) {
					prepareInstance(entity);
					GL11.glDrawElements(GL11.GL_TRIANGLES, count, GL11.GL_UNSIGNED_INT, offset * 4);
				}
				unbindTexturedModel();
			}
		}
	}

	private void prepareTexturedModel(Model model, int i) {
		RawModel rawModel = model.getRawModel();
		GL30.glBindVertexArray(rawModel.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		GL20.glEnableVertexAttribArray(4);
		ModelTexture texture = model.getTexture();
		if (texture.isHasTransparency()) {
			OpenGlUtils.cullBackFaces(false);
		}
		shader.loadFakeLightingVariable(texture.isUseFakeLighting());
		shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		int textureID = model.getTexture().getID();
		//System.out.println(textureID);
		if (i >= 1) textureID = 6;
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		if (model.getRawModel().getJoints() != null) {
			shader.loadJointTransforms(model.getJointTransforms());
			shader.loadAnimated(true);
		} else {
			shader.loadAnimated(false);
		}
	}

	private void unbindTexturedModel() {
		OpenGlUtils.cullBackFaces(true);
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(3);
		GL20.glDisableVertexAttribArray(4);
		GL30.glBindVertexArray(0);
	}

	private void prepareInstance(Entity entity) {
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRotX(),
				entity.getRotY(), entity.getRotZ(), entity.getScale());
		shader.loadTransformationMatrix(transformationMatrix);
	}

}
