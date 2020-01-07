package models;

import org.lwjgl.util.vector.Matrix4f;

import animation.Animation;
import animation.Animator;
import dataStructures.JointData;
import textures.ModelTexture;

public class Model {

	private RawModel rawModel;
	private RawModel boundingBox;
	private RawModel absoluteBoundingBox;
	private ModelTexture texture;
	
	private Joint rootJoint = null;
	private int jointCount = 0;

	private Animator animator = null;

	public Model(RawModel rawModel, ModelTexture texture) {
		this.rawModel = rawModel;
		this.texture = texture;
		if (rawModel.getJoints() != null) {
			this.rootJoint = createJoints(rawModel.getJoints().headJoint);
			this.jointCount = rawModel.getJoints().jointCount;
			this.animator = new Animator(this);
			rootJoint.calcInverseBindTransform(new Matrix4f());
		}
	}

	public RawModel getRawModel() {
		return rawModel;
	}

	public void setRawModel(RawModel rawModel) {
		this.rawModel = rawModel;
	}
	
	public RawModel getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(RawModel boundingBox) {
		this.boundingBox = boundingBox;
	}

	public RawModel getAbsoluteBoundingBox() {
		return absoluteBoundingBox;
	}

	public void setAbsoluteBoundingBox(RawModel absoluteBoundingBox) {
		this.absoluteBoundingBox = absoluteBoundingBox;
	}

	public ModelTexture getTexture() {
		return texture;
	}
	
	public Joint getRootJoint() {
		return rootJoint;
	}

	public void doAnimation(Animation animation) {
		if (this.animator != null) {
			animator.doAnimation(animation);
		}
	}

	public void update() {
		if (this.animator != null) {
			animator.update();
		}
	}

	public Matrix4f[] getJointTransforms() {
		Matrix4f[] jointMatrices = new Matrix4f[jointCount];
		addJointsToArray(rootJoint, jointMatrices);
		return jointMatrices;
	}

	private void addJointsToArray(Joint headJoint, Matrix4f[] jointMatrices) {
		jointMatrices[headJoint.index] = headJoint.getAnimatedTransform();
		for (Joint childJoint : headJoint.children) {
			addJointsToArray(childJoint, jointMatrices);
		}
	}
	
	private static Joint createJoints(JointData data) {
		Joint joint = new Joint(data.index, data.nameId, data.bindLocalTransform);
		for (JointData child : data.children) {
			joint.addChild(createJoints(child));
		}
		return joint;
	}

}
