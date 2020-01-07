package renderEngine;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import game.Game;

public class DisplayManager {

	private static final int FPS_CAP = 120;

	private static int width;
	private static int height;
	private static boolean fullscreen;

	private static long lastFrameTime;
	private static float delta;

	public static void createDisplay() {

		ContextAttribs attribs = new ContextAttribs(3, 2).withForwardCompatible(true).withProfileCore(true);

		try {
			DisplayMode displayMode = null;
			DisplayMode[] modes = Display.getAvailableDisplayModes();
			if (!Game.editorMode) {
				fullscreen = true;
				width = 1920;
				height = 1080;
				for (DisplayMode mode : modes) {
					if (mode.getWidth() == width && mode.getHeight() == height && mode.isFullscreenCapable()) {
						displayMode = mode;
					}
				}

			} else {
				fullscreen = false;
				width = 1280;
				height = 768;
				displayMode = new DisplayMode(width, height);
			}
			Display.setDisplayMode(displayMode);
			Display.setFullscreen(fullscreen);
			Display.create(new PixelFormat().withSamples(8), attribs);
			Display.setTitle("Our First Display!");
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		GL11.glViewport(0, 0, width, height);
		lastFrameTime = getCurrentTime();

	}

	public static void updateDisplay() {
		Display.sync(FPS_CAP);
		Display.update();
		long currentFrameTime = getCurrentTime();
		delta = (currentFrameTime - lastFrameTime) / 1000f;
		lastFrameTime = currentFrameTime;
	}

	public static float getDelta() {
		return delta;
	}

	public static void closeDisplay() {
		Display.destroy();
	}

	private static long getCurrentTime() {
		return Sys.getTime() * 1000 / Sys.getTimerResolution();
	}

}
