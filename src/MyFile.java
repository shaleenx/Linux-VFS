import java.util.ArrayList;


public class MyFile {
	String name;
	String contents;
	ArrayList<Integer> allocatedBlocks;
	public MyFile() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<Integer> getAllocatedBlocks() {
		return allocatedBlocks;
	}
	public void setAllocatedBlocks(ArrayList<Integer> allocatedBlocks) {
		this.allocatedBlocks = allocatedBlocks;
	}
	boolean deleted;
	public MyFile(String name, String contents, ArrayList<Integer> allocatedBlocks) {
		this.name = name;
		this.contents = contents;
		this.allocatedBlocks = allocatedBlocks;
		deleted=false;
	}
}
