package sig.jam.challenge;

public class Block {
	BlockColor col;
	int field_numb = 0; //Where this block came from.
	public Block(BlockColor col) {
		this.col = col;
	}
}
