import java.util.ArrayList;

/*
 * The Directory class.
 * 
 * It lays down the attributes as
 * directory name, the list of files it contains,
 * the list of directories it contains, and a 
 * boolean state variable that tells whether 
 * the file has been deleted or not.
 */
public class Directory {
	String dirName;
	public ArrayList<MyFile> myFiles;
	public ArrayList<Directory> subDirectory;
	boolean isDeleted = false;

	/*
	 * The constructor.
	 * Just takes in the directory name
	 */
	public Directory(String dirName) {
		this.dirName = dirName;
		myFiles = new ArrayList<>();
		subDirectory = new ArrayList<>();
	}

	public void setFiles(ArrayList<MyFile> myFiles) {
		this.myFiles = myFiles;
	}
	
	public ArrayList<Directory> getSubDirectory() {
		return subDirectory;
	}

	public void setSubDirectory(ArrayList<Directory> subDirectory) {
		this.subDirectory = subDirectory;
	}


	
	/*
	 * Method to display all the files contained in the directory
	 * by iterating over all the sub-directories and files.
	 */
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
			System.out.println("(" + dirName + ") Deleted");
	}
}