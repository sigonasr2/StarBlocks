package sig.jam.challenge.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import sig.jam.challenge.JamChallenge;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = JamChallenge.SCREEN_WIDTH;
		config.height = JamChallenge.SCREEN_HEIGHT;
		new LwjglApplication(new JamChallenge(), config);
	}
}
