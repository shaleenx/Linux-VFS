/*
 * This class stores the starting point, ending point
 * and the size of a memory block.
 * 
 * It also stores the state of the block, 
 * which essentially tells whether the block
 * has been deleted or not.
 */
public class Block {
	public int start,end,size;
	boolean state;
	public Block(int start, int end, boolean state) {
		super();
		this.start = start;
		this.end = end;
		this.state = state;
		size=(end-start);
	}
	
}
