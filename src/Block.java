
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
