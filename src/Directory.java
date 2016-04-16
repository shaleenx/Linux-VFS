import java.util.ArrayList;

public class Directory {
	String name;
	public ArrayList<MyFile> files;
	public void setFiles(ArrayList<MyFile> files) {
		this.files = files;
	}

	public ArrayList<Directory> subDirectory;
	boolean deleted = false;

	public Directory() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Directory(String name) {
		this.name = name;
		files = new ArrayList<>();
		subDirectory = new ArrayList<>();
	}

	public void printDirectoryStructure(int level) {
		for (int i = 0; i < level; i++) {
			System.out.print(" ");
		}
		if (!this.deleted) {
			System.out.print("<" + name + ">");
			System.out.println();
			for (MyFile file : files) {
				for (int i = 0; i < level + 5; i++) {
					System.out.print(" ");
				}
				System.out.println(file.name
						+ (file.deleted ? " is deleted" : ""));
			}
			for (int i = 0; i < subDirectory.size(); i++) {
				subDirectory.get(i).printDirectoryStructure(level + 6);
			}
		} else
			System.out.print("<" + name + "> is deleted");
	}
}
