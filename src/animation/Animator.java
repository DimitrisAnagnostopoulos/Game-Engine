package animation;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.util.vector.Matrix4f;

import models.Model;
import renderEngine.DisplayManager;
import models.Joint;

public class Animator {

	private final Model model;

	private Animation currentAnimation;

	private Map<String, Matrix4f> currentPose;

	private float animationTime = 0;

	public Animator(Model model) {
		this.model = model;
	}

	public Animation getCurrentAnimation() {
		return currentAnimation;
	}

	public void doAnimation(Animation animation) {
		this.animationTime = 0;
		this.currentAnimation = animation;
	}

	public void stopAnimation() {
		if (this.currentAnimation != null) {
			this.animationTime = 0;
			applyPoseToJoints(null, model.getRootJoint(), new Matrix4f());
			this.currentAnimation = null;
		}
	}

	public void update() {
		if (currentAnimation == null) {
			return;
		}
		increaseAnimationTime();
		Map<String, Matrix4f> currentPose = calculateCurrentAnimationPose();
		applyPoseToJoints(currentPose, model.getRootJoint(), new Matrix4f());
	}

	private void increaseAnimationTime() {
		animationTime += DisplayManager.getDelta();
		if (animationTime > currentAnimation.getLength()) {
			this.animationTime %= currentAnimation.getLength();
		}
	}

	private Map<String, Matrix4f> calculateCurrentAnimationPose() {
		KeyFrame[] frames = getPreviousAndNextFrames();
		float progression = calculateProgression(frames[0], frames[1]);
		return interpolatePoses(frames[0], frames[1], progression);
	}

	private void applyPoseToJoints(Map<String, Matrix4f> currentPose, Joint joint, Matrix4f parentTransform) {
		Matrix4f currentTransform = new Matrix4f();
		if (currentPose == null) {
			for (Joint childJoint : joint.children) {
				applyPoseToJoints(currentPose, childJoint, new Matrix4f());
			}
		} else {
			Matrix4f currentLocalTransform = currentPose.get(joint.name);
			currentTransform = Matrix4f.mul(parentTransform, currentLocalTransform, null);
			for (Joint childJoint : joint.children) {
				applyPoseToJoints(currentPose, childJoint, currentTransform);
			}
			Matrix4f.mul(currentTransform, joint.getInverseBindTransform(), currentTransform);
		}
		joint.setAnimationTransform(currentTransform);
	}

	private KeyFrame[] getPreviousAndNextFrames() {
		KeyFrame[] allFrames = currentAnimation.getKeyFrames();
		KeyFrame previousFrame = allFrames[0];
		KeyFrame nextFrame = allFrames[0];
		for (int i = 1; i < allFrames.length; i++) {
			nextFrame = allFrames[i];
			if (nextFrame.getTimeStamp() > animationTime) {
				break;
			}
			previousFrame = allFrames[i];
		}
		return new KeyFrame[] { previousFrame, nextFrame };
	}

	private float calculateProgression(KeyFrame previousFrame, KeyFrame nextFrame) {
		float totalTime = nextFrame.getTimeStamp() - previousFrame.getTimeStamp();
		float currentTime = animationTime - previousFrame.getTimeStamp();
		return currentTime / totalTime;
	}

	private Map<String, Matrix4f> interpolatePoses(KeyFrame previousFrame, KeyFrame nextFrame, float progression) {
		Map<String, Matrix4f> currentPose = new HashMap<String, Matrix4f>();
		for (String jointName : previousFrame.getJointKeyFrames().keySet()) {
			JointTransform previousTransform = previousFrame.getJointKeyFrames().get(jointName);
			JointTransform nextTransform = nextFrame.getJointKeyFrames().get(jointName);
			JointTransform currentTransform = JointTransform.interpolate(previousTransform, nextTransform, progression);
			currentPose.put(jointName, currentTransform.getLocalTransform());
		}
		this.currentPose = currentPose;
		return currentPose;
	}

	public Map<String, Matrix4f> getCurrentPose() {
		return currentPose;
	}

}
