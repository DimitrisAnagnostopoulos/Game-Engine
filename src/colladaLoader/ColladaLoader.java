package colladaLoader;

import dataStructures.ModelData;
import dataStructures.AnimationData;
import dataStructures.MeshData;
import dataStructures.SkeletonData;
import dataStructures.SkinningData;
import xmlParser.XmlNode;
import xmlParser.XmlParser;

public class ColladaLoader {
	
	private static final String RES_LOC = "res/models/";

	public static ModelData loadColladaModel(String fileName, int maxWeights) {
		XmlNode node = XmlParser.loadXmlFile(RES_LOC + fileName);

		SkinLoader skinLoader = new SkinLoader(node.getChild("library_controllers"), maxWeights);
		SkinningData skinningData = skinLoader.extractSkinData();

		SkeletonLoader jointsLoader = new SkeletonLoader(node.getChild("library_visual_scenes"), skinningData.jointOrder);
		SkeletonData jointsData = jointsLoader.extractBoneData();

		GeometryLoader g = new GeometryLoader(node.getChild("library_geometries"), skinningData.verticesSkinData);
		MeshData meshData = g.extractModelData();

		return new ModelData(meshData, jointsData);
	}

	public static AnimationData loadColladaAnimation(String fileName) {
		XmlNode node = XmlParser.loadXmlFile(RES_LOC + fileName);
		XmlNode animNode = node.getChild("library_animations");
		XmlNode jointsNode = node.getChild("library_visual_scenes");
		AnimationLoader loader = new AnimationLoader(animNode, jointsNode);
		AnimationData animData = loader.extractAnimation();
		return animData;
	}

}
