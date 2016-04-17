import java.util.ArrayList;

public class MyFile {
	String fileName;
	String contents;
	int fileSize;
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
	public MyFile(String fileName, String contents, ArrayList<Integer> usedBlocks) {
		this.fileName = fileName;
		this.contents = contents;
		this.usedBlocks = usedBlocks;
		isDeleted=false;
	}
}
