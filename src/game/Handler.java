package game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import animation.Animation;
import dataStructures.ModelData;
import entities.Camera;
import entities.Entity;
import entities.Floor;
import entities.Light;
import entities.Player;
import entitySheets.EntitySheet;
import geometry.Triangle;
import models.RawModel;
import models.Model;
import pathfinding.Pathfinder;
import renderEngine.AnimationLoader;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.Maths;

public class Handler {

	static Map<Class<? extends Entity>, List<? extends Entity>> entities = new HashMap<>();

	static Map<String, RawModel> loadedModels = new HashMap<>();
	static Map<String, RawModel> loadedBoundingBoxes = new HashMap<>();
	static Map<String, Animation> loadedAnimations = new HashMap<>();

	public static Camera camera;
	public static Light light;
	public static Terrain terrain;
	public static Player player;

	public static Map<Class<? extends Entity>, List<? extends Entity>> getEntities() {
		return Handler.entities;
	}

	public static List<? extends Entity> getEntities(String className) {
		List<? extends Entity> entities = new ArrayList<>();
		for (Map.Entry<Class<? extends Entity>, List<? extends Entity>> entry : Handler.entities.entrySet()) {
			if (entry.getKey().getSimpleName().equals(className)) {
				entities = entry.getValue();
				break;
			}
		}
		return entities;
	}

	public static void setEntities(Map<Class<? extends Entity>, List<? extends Entity>> entities) {
		Handler.entities = entities;
	}

	public static Map<String, RawModel> getLoadedModels() {
		return loadedModels;
	}

	public static Map<String, RawModel> getLoadedBoundingBoxes() {
		return loadedBoundingBoxes;
	}

	public static Map<String, Animation> getLoadedAnimations() {
		return loadedAnimations;
	}

	public static void assignModelToEntity(Entity entity) {
		RawModel rawModel = null;
		RawModel boundingBox = null;
		RawModel loadedModel = loadedModels.get(entity.getSheet().model);
		RawModel loadedBoundingBox = loadedBoundingBoxes.get(entity.getSheet().model);
		if (loadedModel != null && loadedBoundingBox != null) {
			rawModel = loadedModel;
			boundingBox = loadedBoundingBox;
		} else {
			ModelData data = Game.loader.loadModelDataFromFile(entity.getSheet().model);
			rawModel = Game.loader.loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(),
					data.getIndices(), data.getJointIds(), data.getVertexWeights());
			rawModel.setJoints(data.getJointsData());
			rawModel.setMaterialChangeIndices(data.getMaterialChangeIndices());
			ModelData bbData = null;
			if (entity.getSheet().boundingBox.length() != 0) {
				if (entity.getSheet().boundingBox == entity.getSheet().model) {
					bbData = data;
				} else {
					bbData = Game.loader.loadModelDataFromFile(entity.getSheet().boundingBox);
				}
				boundingBox = Game.loader.loadBoundingBox(bbData);
				boundingBox.setJoints(bbData.getJointsData());
			} else {
				bbData = data;
				boundingBox = Game.loader.generateBoundingBox(
						Game.loader.generateBoundaries(Game.loader.generateVertices(bbData)), null);
			}
			loadedModels.put(entity.getSheet().model, rawModel);
			loadedBoundingBoxes.put(entity.getSheet().model, boundingBox);
		}

		ModelTexture texture = null;
		if (entity.getSheet().texture != "") texture = new ModelTexture(Game.loader.loadTexture(entity.getSheet().texture));
		Model model = new Model(rawModel, texture);
		/* texture specific */
		if (texture != null) {
			texture.setHasTransparency(entity.getSheet().transparency);
			texture.setShineDamper(entity.getSheet().shineDamper);
			texture.setReflectivity(entity.getSheet().reflectivity);
			texture.setUseFakeLighting(entity.getSheet().useFakeLighting);
		}
		/**/
		entity.setModel(model);
		entity.setBoundingBox(boundingBox);
		Handler.setGeometry(entity);
	}

	public static void loadAnimation(String FileName) {
		if (!Handler.getLoadedAnimations().containsKey(FileName)) {
			Animation animation = AnimationLoader.loadAnimation(FileName);
			Handler.getLoadedAnimations().put(FileName, animation);
		}
	}

	public static List<Vector3f> getAbsoluteVertices(Entity entity) {
		RawModel boundingBox = entity.getBoundingBox();
		List<Vector3f> absoluteVertices = new ArrayList<>();
		if (boundingBox != null) {
			Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRotX(),
					entity.getRotY(), entity.getRotZ(), entity.getScale());
			int i = 0;
			for (Vector3f vertex : boundingBox.getVertices()) {
				if (boundingBox.getJoints() != null) {
					Vector3f finalPosition = new Vector3f();
					Matrix4f[] jointTransforms = entity.getModel().getJointTransforms();
					for (int j = 0; j < 3; j++) {
						Matrix4f jointTransform = jointTransforms[boundingBox.getJointIndices().get((i * 3) + j)];
						Float weight = boundingBox.getWeights().get((i * 3) + j);
						finalPosition = Vector3f.add(finalPosition,
								(Vector3f) Maths.transformVectorThroughMatrix(vertex, jointTransform).scale(weight),
								null);
					}
					vertex = finalPosition;
				}
				absoluteVertices.add(Maths.transformVectorThroughMatrix(vertex, transformationMatrix));
				i++;
			}
		}
		return absoluteVertices;
	}

	public static void init() {
		Handler.light = new Light(new Vector3f(0, 100, 0), new Vector3f(1, 1, 1));
		Handler.camera = new Camera();

		TerrainTexture backgroundTexture = new TerrainTexture(Game.loader.loadTexture("grass.png"));
		TerrainTexture rTexture = new TerrainTexture(Game.loader.loadTexture("dirt.png"));
		TerrainTexture gTexture = new TerrainTexture(Game.loader.loadTexture("mud.png"));
		TerrainTexture bTexture = new TerrainTexture(Game.loader.loadTexture("path.png"));

		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		TerrainTexture blendMap = new TerrainTexture(Game.loader.loadTexture("blendMap.png"));

		Handler.terrain = new Terrain(0, 0, texturePack, blendMap);

		EntitySheet playerSheet = new EntitySheet();
		playerSheet.model = "model.dae";
		playerSheet.boundingBox = "model.dae";
		playerSheet.texture = "diffuse.png";
		Handler.player = new Player(playerSheet, new Vector3f(200, 0, 200), 0, 0, 0, 1);
		assignModelToEntity(Handler.player);
		Handler.player.pathfinder = new Pathfinder(Handler.player);
	}

	public static void save(String roomName) {
		Game.running = false;
		try {
			File file = new File("data/" + roomName + ".dat");
			file.createNewFile();
			FileOutputStream saveFile = new FileOutputStream(file);
			ObjectOutputStream save = new ObjectOutputStream(saveFile);
			for (Map.Entry<Class<? extends Entity>, List<? extends Entity>> entry : Handler.getEntities().entrySet()) {
				for (Entity entity : entry.getValue()) {
					entity.setTrianglesToVerticesMap(null);
					entity.setTriangles(null);
					entity.setModel(null);
					entity.setBoundingBox(null);
					entity.setAbsoluteBoundingBox(null);
					entity.pathfinder = null;
					entity.floor = null;
					entity.triangle = null;
				}
			}
			save.writeObject(getEntities());
			save.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Game.running = true;
	}

	@SuppressWarnings("unchecked")
	public static void load(String roomName) {
		Game.running = false;
		clearScene();
		String fileName = "data/" + roomName + ".dat";
		if (!new File(fileName).exists())
			save(roomName);
		try {
			FileInputStream saveFile = new FileInputStream(fileName);
			ObjectInputStream save = new ObjectInputStream(saveFile);
			setEntities((Map<Class<? extends Entity>, List<? extends Entity>>) save.readObject());
			save.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Game.running = true;
	}

	public static void doForEachEntity(Consumer<Entity> function, String className) {
		for (Map.Entry<Class<? extends Entity>, List<? extends Entity>> entry : Handler.entities.entrySet()) {
			if (className == null || entry.getKey().getSimpleName().equals(className)) {
				for (Entity entity : entry.getValue()) {
					function.accept(entity);
				}
			}
		}
	}

	public static void update() {

		// process entities
		Game.renderer.processTerrain(Handler.terrain);
		Game.renderer.processEntity(Handler.player);

		Consumer<Entity> processEntity = (entity) -> {
			if (entity.getModel() == null) {
				assignModelToEntity(entity);
			} else {
				Game.renderer.processEntity(entity);
			}
		};
		Handler.doForEachEntity(processEntity, null);

		// assign stuff
		Handler.doForEachEntity((entity) -> {
			if (entity.pathfinder == null) {
				entity.pathfinder = new Pathfinder(entity);
			}
		}, "Person");

		// update
		Handler.player.update();
		Consumer<Entity> update = (entity) -> {
			entity.update();
		};
		Handler.doForEachEntity(update, null);

	}

	public static void clearScene() {
		entities.clear();
		loadedModels.clear();
		loadedBoundingBoxes.clear();
	}

	public static void addEntity(Entity entity) {
		@SuppressWarnings("unchecked")
		List<Entity> subEntities = (List<Entity>) Handler.getEntities().get(entity.getClass());
		if (subEntities != null) {
			subEntities.add(entity);
		} else {
			List<Entity> newEntities = new ArrayList<Entity>();
			newEntities.add(entity);
			Handler.getEntities().put(entity.getClass(), newEntities);
		}
	}

	public static void setGeometry(Entity entity) {
		List<Vector3f> absoluteVertices = Handler.getAbsoluteVertices(entity);
		entity.setAbsoluteBoundingBox(Game.loader.generateBoundingBox(Game.loader.generateBoundaries(absoluteVertices),
				entity.getAbsoluteBoundingBox()));
		List<Triangle> triangles = null;
		if (entity.getClass().getSimpleName().equals("Floor")) {
			triangles = Game.loader.generateNodes(absoluteVertices, entity);
			Game.loader.populateTrianglesToVerticesMap(triangles, entity);
			Handler.player.floor = (Floor) entity;
		} else {
			triangles = Game.loader.generateTriangles(absoluteVertices, entity);
		}
		entity.setTriangles(triangles);
	}

	public static void refreshGeometry(Entity entity) {
		List<Vector3f> absoluteVertices = Handler.getAbsoluteVertices(entity);
		Game.loader.refreshBoundingBoxVertices(entity.getBoundingBox(), absoluteVertices);
		entity.setAbsoluteBoundingBox(Game.loader.generateBoundingBox(Game.loader.generateBoundaries(absoluteVertices),
				entity.getAbsoluteBoundingBox()));
		List<Triangle> triangles = Game.loader.generateTriangles(absoluteVertices, entity);
		entity.setTriangles(triangles);
	}

}
