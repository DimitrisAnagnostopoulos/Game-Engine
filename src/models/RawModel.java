package models;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import dataStructures.SkeletonData;

public class RawModel {

	private int vaoID;
	private int indicesVboID;
	private int verticesVboID;
	private int vertexCount;
	private List<Vector3f> vertices = new ArrayList<Vector3f>();
	private List<Integer> indices = new ArrayList<Integer>();
	private List<Float> weights = new ArrayList<Float>();
	private List<Integer> jointIndices = new ArrayList<Integer>();
	private Boundaries boundaries;
	public Vector3f basicColour;
	private SkeletonData joints;

	public RawModel(int vaoID, int vertexCount) {
		this.vaoID = vaoID;
		this.vertexCount = vertexCount;
	}

	public int getVaoID() {
		return vaoID;
	}

	public int getVerticesVboID() {
		return verticesVboID;
	}

	public void setVerticesVboID(int verticesVboID) {
		this.verticesVboID = verticesVboID;
	}

	public int getIndicesVboID() {
		return indicesVboID;
	}

	public void setIndicesVboID(int indicesVboID) {
		this.indicesVboID = indicesVboID;
	}

	public int getVertexCount() {
		return vertexCount;
	}

	public void setVertexCount(int vertexCount) {
		this.vertexCount = vertexCount;
	}

	public Boundaries getBoundaries() {
		return boundaries;
	}

	public void setBoundaries(Boundaries boundaries) {
		this.boundaries = boundaries;
	}

	public List<Vector3f> getVertices() {
		return vertices;
	}

	public void setVertices(List<Vector3f> vertices) {
		this.vertices = vertices;
	}

	public List<Integer> getIndices() {
		return indices;
	}

	public void setIndices(List<Integer> indices) {
		this.indices = indices;
	}
	
	public List<Float> getWeights() {
		return weights;
	}

	public void setWeights(List<Float> weights) {
		this.weights = weights;
	}
	
	public List<Integer> getJointIndices() {
		return jointIndices;
	}

	public void setJointIndices(List<Integer> jointIndices) {
		this.jointIndices = jointIndices;
	}
	
	public SkeletonData getJoints() {
		return joints;
	}

	public void setJoints(SkeletonData joints) {
		this.joints = joints;
	}

}
