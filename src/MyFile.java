import java.util.ArrayList;

/*
 * This class provides the layout
 * of a File object
 * 
 * It store the fileName, the contents and
 * the file size in it.
 * 
 * Methods are supporting getters and setters
 * for the above mentioned attributes, and 
 * the constructor
 */

public class MyFile {
	String fileName;
	String contents;
	int fileSize;
	/*
	 * Getters and Setters
	 */
	public int getFileSize() {
		return fileSize;
	}
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
	ArrayList<Integer> usedBlocks;
	boolean isDeleted;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public ArrayList<Integer> getUsedBlocks() {
		return usedBlocks;
	}
	public void setUsedBlocks(ArrayList<Integer> usedBlocks) {
		this.usedBlocks = usedBlocks;
	}
	/*
	 * The constructor.
	 * Takes in the the name of the file, the contents, the file size,
	 * and an ArrayList containing a list of used blocks
	 */
	public MyFile(String fileName, String contents, int fileSize, ArrayList<Integer> usedBlocks) {
		this.fileName = fileName;
		this.contents = contents;
		this.setFileSize(fileSize);
		this.usedBlocks = usedBlocks;
		isDeleted=false;
	}
}
