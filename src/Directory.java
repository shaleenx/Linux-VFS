import java.util.ArrayList;

public class Directory {
	String dirName;
	public ArrayList<MyFile> myFiles;
	public ArrayList<Directory> subDirectory;
	boolean isDeleted = false;

	public Directory(String dirName) {
		this.dirName = dirName;
		myFiles = new ArrayList<>();
		subDirectory = new ArrayList<>();
	}

	public void setFiles(ArrayList<MyFile> myFiles) {
		this.myFiles = myFiles;
	}
	
	public void showFileList(int level) {
		for (int i = 0; i < level; i++) {
			System.out.print(" ");
		}
		if (!this.isDeleted) {
			System.out.println("(" + dirName + ")");
			for (MyFile file : myFiles) {
				for (int i = 0; i < level + 5; i++)
					System.out.print(" ");
				System.out.println(file.getFileName()
						+ (file.isDeleted ? " (Deleted)" : ""));
			}
			for (int i = 0; i < subDirectory.size(); i++) {
				subDirectory.get(i).showFileList(level + 6);
			}
		} 
		else
			System.out.print("(" + dirName + ") Deleted");
	}
}