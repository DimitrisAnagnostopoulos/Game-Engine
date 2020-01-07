package renderEngine;

import java.io.FileInputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.Boundaries;
import models.Path;
import models.RawModel;
import objConverter.OBJFileLoader;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import colladaLoader.ColladaLoader;
import dataStructures.MeshData;
import dataStructures.ModelData;
import entities.Entity;
import geometry.Node;
import geometry.Triangle;

public class Loader {

	private static final int BYTES_PER_FLOAT = 4;
	private static final int BYTES_PER_INT = 4;
	private List<Integer> vaos = new ArrayList<Integer>();
	private List<Integer> vbos = new ArrayList<Integer>();
	private List<Integer> textures = new ArrayList<Integer>();

	public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices, int[] jointIds,
			float[] vertexWeights) {
		int vaoID = createVAO();
		int indicesVboID = bindIndicesBuffer(indices);
		int verticesVboID = storeFloatDataInAttributeList(0, 3, positions);
		storeFloatDataInAttributeList(1, 2, textureCoords);
		storeFloatDataInAttributeList(2, 3, normals);
		storeIntDataInAttributeList(3, 3, jointIds);
		storeFloatDataInAttributeList(4, 3, vertexWeights);
		unbindVAO();
		RawModel rawModel = new RawModel(vaoID, indices.length);
		rawModel.setIndicesVboID(indicesVboID);
		rawModel.setVerticesVboID(verticesVboID);
		return rawModel;
	}

	public int loadTexture(String fileName) {
		Texture texture = null;
		try {
			texture = TextureLoader.getTexture("PNG", new FileInputStream("res/textures/" + fileName));
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Tried to load texture " + fileName + ".png, didn't work");
			System.exit(-1);
		}
		textures.add(texture.getTextureID());
		return texture.getTextureID();
	}

	public void cleanUp() {
		for (int vao : vaos) {
			GL30.glDeleteVertexArrays(vao);
		}
		for (int vbo : vbos) {
			GL15.glDeleteBuffers(vbo);
		}
		for (int texture : textures) {
			GL11.glDeleteTextures(texture);
		}
	}

	private int createVAO() {
		int vaoID = GL30.glGenVertexArrays();
		vaos.add(vaoID);
		GL30.glBindVertexArray(vaoID);
		return vaoID;
	}

	private int storeFloatDataInAttributeList(int attributeNumber, int attrSize, float[] data) {
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer = storeData(data);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW);
		GL20.glVertexAttribPointer(attributeNumber, attrSize, GL11.GL_FLOAT, false, attrSize * BYTES_PER_FLOAT, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		return vboID;
	}

	public int storeIntDataInAttributeList(int attributeNumber, int attrSize, int[] data) {
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		IntBuffer buffer = storeData(data);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW);
		GL30.glVertexAttribIPointer(attributeNumber, attrSize, GL11.GL_INT, attrSize * BYTES_PER_INT, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		return vboID;
	}

	private void unbindVAO() {
		GL30.glBindVertexArray(0);
	}

	private int bindIndicesBuffer(int[] indices) {
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
		IntBuffer buffer = storeDataInIntBuffer(indices);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW);
		return vboID;
	}

	private IntBuffer storeDataInIntBuffer(int[] data) {
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}

	private FloatBuffer storeData(float[] data) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}

	public IntBuffer storeData(int[] data) {
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}

	public List<Vector3f> generateVertices(ModelData data) {
		List<Vector3f> vertices = new ArrayList<Vector3f>();

		float[] verticesData = data.getVertices();
		for (int i = 0; i < verticesData.length / 3; i++) {
			vertices.add(new Vector3f(verticesData[i * 3], verticesData[i * 3 + 1], verticesData[i * 3 + 2]));
		}

		return vertices;
	}

	private List<Integer> generateIndices(ModelData data) {
		List<Integer> indices = new ArrayList<Integer>();

		int[] indicesData = data.getIndices();
		for (int i = 0; i < indicesData.length; i++) {
			indices.add(indicesData[i]);
		}

		return indices;
	}

	private List<Integer> generateJointIndices(ModelData data) {
		List<Integer> jointIndices = new ArrayList<Integer>();

		int[] jointIndicesData = data.getJointIndices();
		for (int i = 0; i < jointIndicesData.length; i++) {
			jointIndices.add(jointIndicesData[i]);
		}

		return jointIndices;
	}

	public List<Float> generateWeights(ModelData data) {
		List<Float> weights = new ArrayList<Float>();

		float[] weightsData = data.getWeights();
		for (int i = 0; i < weightsData.length; i++) {
			weights.add(weightsData[i]);
		}

		return weights;
	}

	public List<Triangle> generateTriangles(List<Vector3f> vertices, Entity entity) {
		List<Integer> indices = entity.getBoundingBox().getIndices();
		List<Triangle> triangles = new ArrayList<Triangle>();

		for (int i = 0; i < indices.size() / 3; i++) {
			Triangle triangle = new Triangle(vertices.get(indices.get(i * 3)), vertices.get(indices.get(i * 3 + 1)),
					vertices.get(indices.get(i * 3 + 2)));
			triangles.add(triangle);
		}

		return triangles;
	}

	public List<Triangle> generateNodes(List<Vector3f> vertices, Entity entity) {
		List<Integer> indices = entity.getBoundingBox().getIndices();
		List<Triangle> nodes = new ArrayList<Triangle>();

		if (entity.getTriangles() == null) {
			ModelData data = this.loadModelDataFromFile(entity.getSheet().model);
			float[] normals = data.getNormals();
			for (int i = 0; i < indices.size() / 3; i++) {
				boolean walkable = (boolean) (normals[indices.get(i * 3) * 3 + 1] >= 0);
				Node node = new Node(vertices.get(indices.get(i * 3)), vertices.get(indices.get(i * 3 + 1)),
						vertices.get(indices.get(i * 3 + 2)), entity, walkable);
				nodes.add(node);
			}
		} else {
			List<Triangle> existingNodes = entity.getTriangles();
			for (int i = 0; i < indices.size() / 3; i++) {
				Node existingNode = (Node) existingNodes.get(i);
				boolean walkable = existingNode.walkable;
				Node node = new Node(vertices.get(indices.get(i * 3)), vertices.get(indices.get(i * 3 + 1)),
						vertices.get(indices.get(i * 3 + 2)), entity, walkable);
				nodes.add(node);
			}
		}

		return nodes;
	}

	public void populateTrianglesToVerticesMap(List<Triangle> triangles, Entity entity) {
		if (entity.getTrianglesToVerticesMap() == null)
			entity.setTrianglesToVerticesMap(new HashMap<Vector3f, List<Triangle>>());
		for (Triangle triangle : triangles) {
			for (Vector3f vertex : triangle.vertices) {
				List<Triangle> trianglesWithVertex = entity.getTrianglesToVerticesMap().get(vertex);
				if (trianglesWithVertex != null) {
					trianglesWithVertex.add(triangle);
				} else {
					List<Triangle> newTriangles = new ArrayList<Triangle>();
					newTriangles.add(triangle);
					entity.getTrianglesToVerticesMap().put(vertex, newTriangles);
				}
			}
		}
	}

	public Boundaries generateBoundaries(List<Vector3f> vertices) {
		float minX = vertices.get(0).x;
		float maxX = vertices.get(0).x;
		float minY = vertices.get(0).y;
		float maxY = vertices.get(0).y;
		float minZ = vertices.get(0).z;
		float maxZ = vertices.get(0).z;

		for (Vector3f vertex : vertices) {
			if (vertex.x < minX)
				minX = vertex.x;
			if (vertex.x > maxX)
				maxX = vertex.x;
			if (vertex.y < minY)
				minY = vertex.y;
			if (vertex.y > maxY)
				maxY = vertex.y;
			if (vertex.z < minZ)
				minZ = vertex.z;
			if (vertex.z > maxZ)
				maxZ = vertex.z;
		}
		;

		return new Boundaries(minX, maxX, minY, maxY, minZ, maxZ);
	}

	public RawModel generateBoundingBox(Boundaries boundaries, RawModel existingRawModel) {
		float minX = boundaries.minX;
		float maxX = boundaries.maxX;
		float minY = boundaries.minY;
		float maxY = boundaries.maxY;
		float minZ = boundaries.minZ;
		float maxZ = boundaries.maxZ;

		float[] boxVertices = { minX, minY, minZ, // 0
				minX, maxY, minZ, // 1
				maxX, maxY, minZ, // 2
				maxX, minY, minZ, // 3
				minX, minY, maxZ, // 4
				minX, maxY, maxZ, // 5
				maxX, maxY, maxZ, // 6
				maxX, minY, maxZ // 7
		};

		int[] boxIndices = {
				// front face
				0, 1, 2, 0, 2, 3,
				// back face
				6, 5, 4, 7, 6, 4,
				// left face
				5, 1, 0, 4, 5, 0,
				// right face
				3, 2, 6, 3, 6, 7,
				// top face
				1, 5, 6, 1, 6, 2,
				// bottom face
				7, 4, 0, 3, 7, 0 };

		ModelData modelData = new ModelData(
				new MeshData(boxVertices, new float[0], new float[0], boxIndices, new int[0], new float[0]), null);

		RawModel rawModel = null;

		if (existingRawModel == null) {
			int vaoID = createVAO();
			int indicesVboID = bindIndicesBuffer(boxIndices);
			int verticesVboID = storeFloatDataInAttributeList(0, 3, boxVertices);
			unbindVAO();
			rawModel = new RawModel(vaoID, boxIndices.length);
			rawModel.setIndicesVboID(indicesVboID);
			rawModel.setVerticesVboID(verticesVboID);
		} else {
			rawModel = existingRawModel;

			int indicesVboID = rawModel.getIndicesVboID();
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesVboID);
			IntBuffer ibuffer = storeDataInIntBuffer(boxIndices);
			GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, ibuffer, GL15.GL_DYNAMIC_DRAW);

			int verticesVboID = rawModel.getVerticesVboID();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, verticesVboID);
			FloatBuffer buffer = BufferUtils.createFloatBuffer(boxVertices.length);
			buffer.rewind();
			buffer.put(boxVertices);
			buffer.flip();
			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		}

		rawModel.setVertices(generateVertices(modelData));
		rawModel.setIndices(generateIndices(modelData));
		rawModel.setWeights(generateWeights(modelData));
		rawModel.setJointIndices(generateJointIndices(modelData));
		rawModel.setBoundaries(new Boundaries(minX, maxX, minY, maxY, minZ, maxZ));

		return rawModel;
	}

	public void refreshBoundingBoxVertices(RawModel boundingBox, List<Vector3f> verticesList) {

		if (boundingBox != null) {

			float[] vertices = new float[verticesList.size() * 3];
			for (int i = 0; i < verticesList.size(); i++) {
				vertices[i * 3] = verticesList.get(i).x;
				vertices[i * 3 + 1] = verticesList.get(i).y;
				vertices[i * 3 + 2] = verticesList.get(i).z;
			}

			int vvboID = boundingBox.getVerticesVboID();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vvboID);
			FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
			buffer.rewind();
			buffer.put(vertices);
			buffer.flip();
			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		}
	}

	public RawModel loadBoundingBox(ModelData bbData) {
		RawModel rawModel = this.loadToVAO(bbData.getVertices(), new float[0], new float[0], bbData.getIndices(),
				bbData.getJointIndices(), bbData.getWeights());
		rawModel.setVertices(generateVertices(bbData));
		rawModel.setIndices(generateIndices(bbData));
		rawModel.setWeights(generateWeights(bbData));
		rawModel.setJointIndices(generateJointIndices(bbData));
		rawModel.setBoundaries(this.generateBoundaries(rawModel.getVertices()));
		return rawModel;
	}

	public Path generatePath(List<Vector2f> waypoints, Path existingPath) {
		int[] indices = new int[waypoints.size()];
		float[] vertices = new float[waypoints.size() * 3];
		for (int i = 0; i < waypoints.size(); i++) {
			indices[i] = i;
			vertices[3 * i] = waypoints.get(i).x;
			vertices[3 * i + 1] = 5;
			vertices[3 * i + 2] = waypoints.get(i).y;
		}

		int vaoID = createVAO();

		int indicesVboID = bindIndicesBuffer(indices);
		int verticesVboID = storeFloatDataInAttributeList(0, 3, vertices);

		GL30.glBindVertexArray(0);
		Path path = new Path(vaoID, indices.length);
		path.setIndicesVboID(indicesVboID);
		path.setVerticesVboID(verticesVboID);

		if (existingPath != null) {
			GL30.glDeleteVertexArrays(existingPath.getVaoID());
			GL15.glDeleteBuffers(existingPath.getIndicesVboID());
			GL15.glDeleteBuffers(existingPath.getVerticesVboID());
		}

		return path;
	}

	public ModelData loadModelDataFromFile(String fileName) {
		String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
		ModelData data = null;
		switch (ext) {
		case "obj":
			data = OBJFileLoader.loadOBJ(fileName);
			break;
		case "dae":
			data = ColladaLoader.loadColladaModel(fileName, 3);
			break;
		default:
			System.out.println("Unsupported file extension of file " + fileName);
			System.exit(0);
			break;
		}
		return data;
	}

}
