package game;

import java.io.File;

import org.lwjgl.LWJGLUtil;
import org.lwjgl.opengl.Display;

import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;

public class Game {

	public static Loader loader;
	public static MasterRenderer renderer;
	private static Editor editor;
	public static boolean editorMode = false;
	public static boolean running = false;

	public static void main(String[] args) {

		setNatives();

		if (args.length > 0 && args[0] != "play") {
		} else {
			editorMode = true;
			editor = new Editor();
		}

		DisplayManager.createDisplay();
		loader = new Loader();
		renderer = new MasterRenderer();

		Handler.init();
		Handler.load("Room1");

		while (!Display.isCloseRequested()) {
			if (Game.running) {
				update();
				render();
			}
		}

		cleanUp();

		if (args.length > 0 && args[0] != "play") {
		} else {
			editor.destroy();
		}

	}

	private static void setNatives() {
		File JGLLib = null;

		switch (LWJGLUtil.getPlatform()) {
		case LWJGLUtil.PLATFORM_WINDOWS:
			JGLLib = new File("lib/native/windows/");
			break;
		case LWJGLUtil.PLATFORM_LINUX:
			JGLLib = new File("lib/native/linux/");
			break;
		case LWJGLUtil.PLATFORM_MACOSX:
			JGLLib = new File("lib/native/macosx/");
			break;
		}

		System.setProperty("org.lwjgl.librarypath", JGLLib.getAbsolutePath());
	}

	private static void update() {
		Handler.player.move();
		Handler.camera.move();
		Handler.update();
	}

	private static void render() {
		renderer.render(Handler.light, Handler.camera);
		DisplayManager.updateDisplay();
	}

	private static void cleanUp() {
		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();
	}

}
