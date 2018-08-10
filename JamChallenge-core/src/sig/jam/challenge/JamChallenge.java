package sig.jam.challenge;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class JamChallenge extends ApplicationAdapter {
	SpriteBatch batch;
	final public static int SCREEN_WIDTH = 1066; 
	final public static int SCREEN_HEIGHT = 600;
	static int targetFPS = 60;
	Viewport view;
	Camera cam;
	Calendar lastCheck = Calendar.getInstance();
	int framesPassed=0;
	List<Field> playingFields = new ArrayList<Field>();
	public static Texture onebyone;
	final public static int BLOCK_WIDTH = 16;
	final public static int BLOCK_HEIGHT = 16;
	public static HashMap<Integer,int[]> KEYSET_MAP = new HashMap<Integer,int[]>();
	
	public static Random r = new Random();
	
	public static int FIELD_IDENTIFIER = 0; //Should be 0-3, 0 = First field, 3 = Last field
	
	final static int KEYCODE_LEFT = 0;
	final static int KEYCODE_UP = 1;
	final static int KEYCODE_RIGHT = 2;
	final static int KEYCODE_DOWN = 3;
	final static int KEYCODE_ACTIONKEY = 4;
	final static int KEYCODE_SPEEDUPKEY = 5;
	
	private void FrameCounter() {
		framesPassed++;
		if (lastCheck.getTime().getSeconds()!=Calendar.getInstance().getTime().getSeconds()) {
			System.out.println("FPS: "+framesPassed);
			framesPassed=0;
			lastCheck=Calendar.getInstance();
		}
	}
	
	@Override
	public void create () {
		
		r.setSeed(12839);
		
		cam = new PerspectiveCamera();
		view = new FitViewport(SCREEN_WIDTH,SCREEN_HEIGHT,cam);	
		batch = new SpriteBatch();
		onebyone = new Texture("1x1.png");
		
		KEYSET_MAP.put(1, new int[]{Keys.LEFT,Keys.UP,Keys.RIGHT,Keys.DOWN,Keys.Z,Keys.SHIFT_LEFT});
		KEYSET_MAP.put(2, new int[]{Keys.LEFT,Keys.UP,Keys.RIGHT,Keys.DOWN,Keys.Z,Keys.SHIFT_LEFT});
		KEYSET_MAP.put(3, new int[]{Keys.LEFT,Keys.UP,Keys.RIGHT,Keys.DOWN,Keys.Z,Keys.SHIFT_LEFT});
		KEYSET_MAP.put(4, new int[]{Keys.LEFT,Keys.UP,Keys.RIGHT,Keys.DOWN,Keys.Z,Keys.SHIFT_LEFT});
		
		playingFields.add(new Field()
				.setPlayerNumber(1));
		playingFields.add(new Field());
		playingFields.add(new Field());
		playingFields.add(new Field());
	}
	
	@Override
	public void resize(int width, int height) {
		view.update(width, height);
	}

	@Override
	public void render () {
		try {
			Thread.sleep(1000/targetFPS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		FrameCounter();
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		int fieldSpacing = SCREEN_WIDTH/playingFields.size();
		batch.setColor(Color.WHITE);
		for (int i=0;i<playingFields.size();i++) {
			int centerField = (fieldSpacing*i)+(fieldSpacing/2);
			Field field = playingFields.get(i);
			field.run();
			
			int FULLFIELD = field.cols*BLOCK_WIDTH;
			
			for (int j=0;j<=field.cols;j++) {
				batch.draw(onebyone, 
						centerField-(FULLFIELD/2)+(j*BLOCK_WIDTH)-1,64,1,(field.rows+1)*BLOCK_HEIGHT);
			}
			
			field.draw(batch, centerField-(FULLFIELD/2),64-4);
			
			batch.draw(onebyone, centerField-(FULLFIELD/2)-2,60,4,(field.rows+1)*BLOCK_HEIGHT+4);
			batch.draw(onebyone, centerField+(FULLFIELD/2)-2,60,4,(field.rows+1)*BLOCK_HEIGHT+4);
			
			batch.draw(onebyone, centerField,4,2,10);
			
			batch.draw(onebyone, centerField-
					(FULLFIELD/2),64-4,
					FULLFIELD,20);
		}
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}
