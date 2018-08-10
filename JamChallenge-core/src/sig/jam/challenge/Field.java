package sig.jam.challenge;

import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Field {
	int rows = 13;
	int cols = 9;
	double fieldSpd = 0.01;
	double fieldOffset = 0;
	List<Block> block_data = new ArrayList<Block>();
	boolean horizontal_cursor = true; //If false, uses vertical cursor. Vertical cursor is 1x2 with the second block being above the initial position.
	Point cursor_pos = new Point(0,0);
	int player_numb = 0; //AI Controlled.
	//ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	List<Point> position_update_list = new ArrayList<Point>();
	int scheduledTasks=0;
	BlockColor last_selected_col = BlockColor.INVISIBLE;
	int same_select_count = 0;
	public int ID = 0;
	
	public Field() {
		ID = JamChallenge.FIELD_IDENTIFIER++;
		CreateNewRow();
	}
	
	public Field setHorizontalCursor(boolean horizontal_cursor) {
		this.horizontal_cursor=horizontal_cursor;
		return this;
	}
	
	public Field setPlayerNumber(int numb) {
		this.player_numb=numb;
		return this;
	}
	
	public void run() {
		if (scheduledTasks>0) {
			//System.out.prinln("Field "+player_numb+" scheduled tasks: "+scheduledTasks);
			for (int i=0;i<position_update_list.size();i++) {
				Point p = position_update_list.get(i);
				CheckAndRemoveMatchingBlocks(p);
				scheduledTasks--;
				/*try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}*/
			}
			position_update_list.clear();
		}
		if (!RowsAreFilled() /*&& 
				scheduledTasks==0*/) {
			fieldOffset += fieldSpd;
		}
		if (fieldOffset>=1) {
			fieldOffset=0;
			if (cursor_pos.y<rows-((horizontal_cursor)?0:1)-1) {
				cursor_pos.y++;
			}
		}
		if (fieldOffset==0) {
			////System.out.prinln("TEST " + fieldOffset +"("+fieldOffset%1+")");
			if (RowsAreFilled()) {
				//TODO Game over scenario
				
			} else {
				CreateNewRow();
			}
		}
		if (IsRealPlayer()) {
			if (Gdx.input.isKeyJustPressed(JamChallenge.KEYSET_MAP.get(player_numb)[JamChallenge.KEYCODE_LEFT])) {
				if (cursor_pos.x>0) {
					cursor_pos.x--;
				}
			}
			if (Gdx.input.isKeyJustPressed(JamChallenge.KEYSET_MAP.get(player_numb)[JamChallenge.KEYCODE_UP])) {
				if (cursor_pos.y<rows-((horizontal_cursor)?0:1)-1) {
					cursor_pos.y++;
				}
			}
			if (Gdx.input.isKeyJustPressed(JamChallenge.KEYSET_MAP.get(player_numb)[JamChallenge.KEYCODE_RIGHT])) {
				if (cursor_pos.x<cols-((horizontal_cursor)?1:0)-1) {
					cursor_pos.x++;
				}
			}
			if (Gdx.input.isKeyJustPressed(JamChallenge.KEYSET_MAP.get(player_numb)[JamChallenge.KEYCODE_DOWN])) {
				if (cursor_pos.y>0) {
					cursor_pos.y--;
				}
			}
			if (Gdx.input.isKeyJustPressed(JamChallenge.KEYSET_MAP.get(player_numb)[JamChallenge.KEYCODE_ACTIONKEY])) {
				Point cursor_pos2 = cursor_pos.getLocation();
				if (horizontal_cursor) {
					cursor_pos2.x++;
				} else {
					cursor_pos2.y++;
				}
				////System.out.prinln("POS1 Matches: "+DetectMatchedBlocks(this,cursor_pos).size()+" / POS2 Matches: "+DetectMatchedBlocks(this,cursor_pos2).size());
				int b1 = ConvertCursorPositionToBlockID(this,cursor_pos);
				int b2 = ConvertCursorPositionToBlockID(this,cursor_pos2);
				Block block1 = block_data.get(b1);
				Block block2 = block_data.get(b2);
				if (block1.col!=BlockColor.INVISIBLE && 
						block2.col!=BlockColor.INVISIBLE) {
					block_data.set(b2, block1);
					block_data.set(b1, block2);
	
					CheckAndRemoveMatchingBlocks(cursor_pos);
					CheckAndRemoveMatchingBlocks(cursor_pos2);
				} else
				if (block1.col!=BlockColor.INVISIBLE ^
					block2.col!=BlockColor.INVISIBLE) {
					block_data.set(b2, block1);
					block_data.set(b1, block2);
					
					if (block1.col==BlockColor.INVISIBLE) {
						DropDownBlocksAbove(this,cursor_pos2);
						DropBlockAllTheWayDown(this,cursor_pos);
					} else 
					if (block2.col==BlockColor.INVISIBLE) {
						DropDownBlocksAbove(this,cursor_pos);
						DropBlockAllTheWayDown(this,cursor_pos2);
					}
				}
			}
		}
	}

	private void DropBlockAllTheWayDown(Field field, Point blockPos) {
		Point fallPos = blockPos.getLocation();
		fallPos.y--;
		int blockID2 = ConvertCursorPositionToBlockID(field, fallPos);
		Block b2 = block_data.get(blockID2);
		//System.out.prinln("In here. b2 is "+b2.col);
		do {
			/*File f = new File("debug.txt");
			try {
				FileWriter fw = new FileWriter(f,true);
				fw.write("Inside do loop.\n");
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
			if (blockID2<block_data.size() && blockID2>=0 && b2.col==BlockColor.INVISIBLE) {
				fallPos.y--;
				blockID2 = ConvertCursorPositionToBlockID(field, fallPos);
				if (blockID2<block_data.size() && blockID2>=0) {
					b2 = block_data.get(blockID2);	
				} else {
					break;
				}
			} else {
				fallPos.y++;
				blockID2 = ConvertCursorPositionToBlockID(field, fallPos);
				if (blockID2<block_data.size() && blockID2>=0) {
					b2 = block_data.get(blockID2);	
				} else {
					break;
				}
				break;
			}
		} while (true);
		//System.out.prinln("Swapping to "+fallPos);
		SwapBlocks(blockPos,fallPos,true);
	}

	private void DropDownBlocksAbove(Field field, Point fallPos) {
		Point targetPos = fallPos.getLocation();
		List<Point> updatePoints = new ArrayList<Point>(); 
		for (int y=targetPos.y+1;y<rows;y++) {
			Point blockPos = targetPos.getLocation();
			blockPos.y=y;
			SwapBlocks(blockPos,targetPos,false);
			updatePoints.add(blockPos.getLocation());
			updatePoints.add(targetPos.getLocation());
			targetPos.y++;
		}
		for (Point p : updatePoints) {
			scheduledTasks++;
			position_update_list.add(p);
		}
	}

	private void SwapBlocks(Point pos1, Point pos2, boolean checkmatches) {
		int pos1_id = ConvertCursorPositionToBlockID(this,pos1);
		int pos2_id = ConvertCursorPositionToBlockID(this,pos2);
		if (pos1_id<block_data.size() && pos1_id>=0 &&
				pos2_id<block_data.size() && pos2_id>=0) {
			Block b1 = block_data.get(pos1_id);
			Block b2 = block_data.get(pos2_id);
			block_data.set(pos2_id, b1);
			block_data.set(pos1_id, b2);
			if (checkmatches) {
				scheduledTasks++;
				position_update_list.add(pos1.getLocation());
				scheduledTasks++;
				position_update_list.add(pos2.getLocation());
			}
		}
	}

	private void CheckAndRemoveMatchingBlocks(Point target_pos) {
		// The first integer stores the X position of each cleared area.
		// The second integer stores the highest/lowest Y space of each cleared area. To determine fall distance.
		//
		HashMap<Integer,Integer> HighPOS = new HashMap<Integer,Integer>(); //Blocks above here will fall.
		HashMap<Integer,Integer> LowPOS = new HashMap<Integer,Integer>(); //Blocks will fall to this level.
		
		/*List<Block> matched_blocks = DetectMatchedBlocks(this,cursor_pos);
		for (Block b : matched_blocks) {
			b.col = BlockColor.INVISIBLE;
			AddBlockToGravityList(HighPOS, LowPOS, b);
		}
		if (matched_blocks.size()>0) {
			Block cursorBlock = block_data.get(ConvertCursorPositionToBlockID(this,cursor_pos));
			cursorBlock.col=BlockColor.INVISIBLE;
			AddBlockToGravityList(HighPOS, LowPOS, cursorBlock);
		}*/
		List<Block> matched_blocks = DetectMatchedBlocks(this,target_pos);
		for (Block b : matched_blocks) {
			b.col = BlockColor.INVISIBLE;
			AddBlockToGravityList(HighPOS, LowPOS, b);
		}
		if (matched_blocks.size()>0) {
			Block cursorBlock2 = block_data.get(ConvertCursorPositionToBlockID(this,target_pos));
			cursorBlock2.col=BlockColor.INVISIBLE;
			AddBlockToGravityList(HighPOS, LowPOS, cursorBlock2);
		}
		
		//System.out.prinln(HighPOS.toString());
		//System.out.prinln(LowPOS.toString());
		
		for (Integer i : HighPOS.keySet()) {
			for (int y=HighPOS.get(i)+1;y<rows;y++) {
				Point blockPos = new Point(i,y);
				int blockID = ConvertCursorPositionToBlockID(this,blockPos);
				if (blockID<block_data.size() && blockID>=0) {
					Point fallPos = blockPos.getLocation();
					fallPos.y-=HighPOS.get(i)-LowPOS.get(i);
					////System.out.prinln("Block at position "+blockPos+" moved down by "+(HighPOS.get(i)-LowPOS.get(i)));
					int fallBlockID = ConvertCursorPositionToBlockID(this,fallPos);
					Block b1 = block_data.get(fallBlockID);
					Block b2 = block_data.get(blockID);
					block_data.set(fallBlockID, b2);
					block_data.set(blockID, b1);
					scheduledTasks++;
					position_update_list.add(fallPos.getLocation());
				}
			}
		}
		
	}

	private void AddBlockToGravityList(HashMap<Integer, Integer> HighPOS, HashMap<Integer, Integer> LowPOS, Block b) {
		Point pos = ConvertBlockIDToCursorPosition(this,block_data.indexOf(b));
		if (HighPOS.containsKey(pos.x)) {
			if (pos.y>HighPOS.get(pos.x)) {
				HighPOS.put(pos.x, pos.y);
			}
		} else {
			HighPOS.put(pos.x, pos.y);
		}
		if (LowPOS.containsKey(pos.x)) {
			if (pos.y-1<LowPOS.get(pos.x)) {
				LowPOS.put(pos.x, pos.y-1);
			}
		} else {
			LowPOS.put(pos.x, pos.y-1);
		}
	}
	
	private boolean RowsAreFilled() {
		if (block_data.size()>=(rows+1)*cols) {
			if (TopRowIsCompletelyInvisible()) {
				for (int i=0;i<9;i++) {
					block_data.remove(0);
				}
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	private boolean TopRowIsCompletelyInvisible() {
		for (int i=0;i<9;i++) {
			if (block_data.get(i).col!=BlockColor.INVISIBLE) {
				return false;
			}
		}
		return true;
	}

	private boolean IsRealPlayer() {
		return player_numb>0;
	}

	private void CreateNewRow() {
		for (int i=0;i<cols;i++) {
			BlockColor col = last_selected_col;
			do {
				col = BlockColor.getRandomColor(5);
				//if (player_numb>0) {System.out.println("Selected "+col);}
			} while(same_select_count==1 && col==last_selected_col);
			if (col==last_selected_col) {
				same_select_count++;
			} else {
				same_select_count=0;
				last_selected_col = col;
			}
			block_data.add(new Block(col));
			/*if (block_data.size()>cols) {
				scheduledTasks++;
				position_update_list.add(ConvertBlockIDToCursorPosition(this,block_data.size()-1-cols));
			}*/
		}
		for (int i=0;i<cols;i++) {
			scheduledTasks++;
			position_update_list.add(ConvertBlockIDToCursorPosition(this,block_data.size()-1-i));
		}
	}
	
	public void draw(SpriteBatch batch, int fieldX, int fieldY) {
		int blockRows = block_data.size()/cols;
		for (int y=blockRows-1;y>=0;y--) {
			for (int x=0;x<cols;x++) {
				Block b = block_data.get((blockRows-1-y)*cols+x);
				batch.setColor(b.col.col);
				batch.draw(JamChallenge.onebyone, 
						(int)(fieldX+(x*JamChallenge.BLOCK_WIDTH)), 
						(int)(fieldY+(y*JamChallenge.BLOCK_HEIGHT)+(fieldOffset*JamChallenge.BLOCK_HEIGHT)),
						JamChallenge.BLOCK_WIDTH,
						JamChallenge.BLOCK_HEIGHT);
				if (y==0) {
					batch.setColor(new Color(0.4f,0.4f,0.4f,0.9f));
					batch.draw(JamChallenge.onebyone, 
							(int)(fieldX+(x*JamChallenge.BLOCK_WIDTH)), 
							(int)(fieldY+(y*JamChallenge.BLOCK_HEIGHT)+(fieldOffset*JamChallenge.BLOCK_HEIGHT)),
							JamChallenge.BLOCK_WIDTH,
							JamChallenge.BLOCK_HEIGHT);
				}
			}
		}
		RenderCursor(batch,fieldX,fieldY);
		batch.setColor(Color.WHITE);
	}

	private void RenderCursor(SpriteBatch batch, int fieldX, int fieldY) {
		batch.setColor(new Color(1f,1f,1f,0.6f));
		if (horizontal_cursor) {
			batch.draw(JamChallenge.onebyone,fieldX+cursor_pos.x*JamChallenge.BLOCK_WIDTH,(int)((fieldOffset*JamChallenge.BLOCK_HEIGHT)+fieldY+JamChallenge.BLOCK_HEIGHT+cursor_pos.y*JamChallenge.BLOCK_HEIGHT),JamChallenge.BLOCK_WIDTH*2,JamChallenge.BLOCK_HEIGHT);
		} else {
			batch.draw(JamChallenge.onebyone,fieldX+cursor_pos.x*JamChallenge.BLOCK_WIDTH,(int)((fieldOffset*JamChallenge.BLOCK_HEIGHT)+fieldY+JamChallenge.BLOCK_HEIGHT+16+cursor_pos.y*JamChallenge.BLOCK_HEIGHT),JamChallenge.BLOCK_WIDTH,JamChallenge.BLOCK_HEIGHT*2);
		}
	}
	
	/***
	 * 
	 * @param field The field to check for matches on.
	 * @param targetBlockPos The X and Y coord of the block in the grid to check for matches.
	 * @return
	 */
	public static List<Block> DetectMatchedBlocks(Field field, Point targetBlockPos) {
		List<Block> blocklist = field.block_data;
		
		List<Block> vertical_matches = new ArrayList<Block>();
		List<Block> horizontal_matches = new ArrayList<Block>();
		
		int blockID = ConvertCursorPositionToBlockID(field, targetBlockPos);
		
		List<Block> matched_blocks = new ArrayList<Block>();
		if (blockID<field.block_data.size() && blockID>=0) {
			Block targetblock = blocklist.get(blockID);
			
			int markerX=0;
			int markerY=1;
			//Vertical Check
			CheckForConnectingBlocks(field, targetBlockPos, blocklist, vertical_matches, targetblock, 0, 1); //Checks for connecting blocks going up.
			CheckForConnectingBlocks(field, targetBlockPos, blocklist, vertical_matches, targetblock, 0, -1); //Checks for connecting blocks going down.
			CheckForConnectingBlocks(field, targetBlockPos, blocklist, horizontal_matches, targetblock, -1, 0); //Checks for connecting blocks going left.
			CheckForConnectingBlocks(field, targetBlockPos, blocklist, horizontal_matches, targetblock, 1, 0); //Checks for connecting blocks going right.
			
			if (vertical_matches.size()>=2) {
				matched_blocks.addAll(vertical_matches);
			}
			if (horizontal_matches.size()>=2) {
				matched_blocks.addAll(horizontal_matches);
			}
		}
		return matched_blocks;
	}

	private static void CheckForConnectingBlocks(Field field, Point targetBlockPos, List<Block> blocklist,
			List<Block> matches_list, Block targetblock, int markerX, int markerY) {
		boolean match=true;
		Point targetPoint = targetBlockPos.getLocation();
		while (match) {
			/*File f = new File("debug.txt");
			try {
				FileWriter fw = new FileWriter(f,true);
				fw.write("Inside while loop.\n");
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
			match=false;
			targetPoint.x+=markerX;
			targetPoint.y+=markerY;
			int checkBlockID = ConvertCursorPositionToBlockID(field,targetPoint);
			if (checkBlockID<blocklist.size() && checkBlockID>=0 &&
					targetPoint.x>=0 && targetPoint.x<field.cols &&
					targetPoint.y>=-1 && targetPoint.y<field.rows) {
				Block checkblock = blocklist.get(checkBlockID);
				if (checkblock.col!=BlockColor.INVISIBLE &&
						targetblock.col == checkblock.col) {
					match=true;
					matches_list.add(checkblock);
				}
			} else {
				System.out.println(checkBlockID+": "+targetPoint+" out of bounds!");
			}
		}
	}

	private static int ConvertCursorPositionToBlockID(Field field, Point targetBlockPos) {
		return targetBlockPos.x+((((field.block_data.size()/field.cols)-targetBlockPos.y-2)*field.cols));
	}
	
	private static Point ConvertBlockIDToCursorPosition(Field field, int blockID) {
		return new Point(blockID%field.cols,(field.block_data.size()/field.cols)-(blockID/field.cols)-2);
	}
}
