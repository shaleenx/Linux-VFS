import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class FileSystem {
	int sizeKB, allspace = 0;
	Directory root;
	ArrayList<Boolean> state;
	public int getAllspace() {
		return allspace;
	}

	public void setAllspace(int allspace) {
		this.allspace = allspace;
	}

	public Directory getRoot() {
		return root;
	}

	public void setRoot(Directory root) {
		this.root = root;
	}

	public ArrayList<Boolean> getState() {
		return state;
	}

	public void setState(ArrayList<Boolean> state) {
		this.state = state;
	}

	public ArrayList<Block> getSpaces() {
		return spaces;
	}

	public void setSpaces(ArrayList<Block> spaces) {
		this.spaces = spaces;
	}

	ArrayList<Block> spaces;

	public FileSystem(int sizeKB) {
		this.sizeKB = sizeKB;
		state = new ArrayList<>(sizeKB);
		for (int i = 0; i < sizeKB; i++) {
			state.add(false);
		}
		root = new Directory("root");
		spaces = new ArrayList<>();
		spaces.add(new Block(0, sizeKB - 1, false));
	}

	public boolean createFile(String path, String contents, int sizeKB) {
		if (sizeKB > this.sizeKB - this.allspace)
			return false;
		String[] paths = path.trim().split("/");
		Directory iter;
		iter = getDire(root, paths, 0);
		if (iter != null) {
			
			if (creatFile(iter, paths[paths.length - 1], contents, sizeKB, spaces,
					state)) {
				this.allspace += sizeKB;
				return true;
			} else
				return false;
		} else
			return false;
	}

	public boolean creatFile(Directory dir, String name, String contents, int sizeKB,
			ArrayList<Block> spaces, ArrayList<Boolean> state) {
		for (Block space : spaces) {
			if (sizeKB <= space.size && !space.state) {
				ArrayList<Integer> allocatedBlocks = new ArrayList<>();
				allocatedBlocks.add(space.start);
				allocatedBlocks.add(space.start + sizeKB - 1);
				MyFile file = new MyFile(name, contents, allocatedBlocks);
				dir.files.add(file);
				for (int i = space.start; i < space.start + sizeKB; i++) {
					state.set(i, true);
				}
				if (sizeKB < space.size) {
					Block newS = new Block(space.start + sizeKB, space.end,
							false);
					spaces.add(newS);
					space.end = space.start + sizeKB - 1;
				}
				space.state = true;
				Collections.sort(spaces, new SpaceSizeCmp());
				return true;
			}
		}
		return false;
	}

	public boolean createFolder(String path) {
		String[] paths = path.trim().split("/");
		Directory iter;
		iter = getDire(root, paths, 0);
		if (iter != null)
			return createDir(iter, paths[paths.length - 1]);
		else
			return false;
	}
	
	public boolean createDir(Directory dir, String name) {
		dir.subDirectory.add(new Directory(name));
		return true;
	}

	public boolean deleteFile(String path) {
		String[] paths = path.trim().split("/");
		Directory iter;
		iter = getDire(root, paths, 0);
		if (iter != null) {
			int fileSize = deleteFile(iter, paths[paths.length - 1],
					spaces, state);
			if (fileSize != 0) {
				this.allspace -= fileSize;
				return true;
			}
			return false;
		} else
			return false;
	}
	
	public int deleteFile(Directory dir, String name, ArrayList<Block> spaces,
			ArrayList<Boolean> state) {
		for (MyFile file : dir.files) {
			if (file.name.equals(name)) {
				for (Block space : spaces) {
					if (space.start == file.allocatedBlocks.get(0)) {
						space.state = false;
						file.deleted = true;
						for (int i = space.start; i < space.end; i++) {
							state.set(i, false);
						}
						return space.size;
					}
				}
				return 0;
			}
		}
		return 0;
	}
	
	public boolean deleteFolder(String path) {
		String[] paths = path.trim().split("/");
		Directory iter;
		iter = getDire(root, paths, 0);
		if (iter != null) {
			int filesSize = deleteDir(iter, spaces, state);
			if (filesSize != 0) {
				this.allspace -= filesSize;
				return true;
			}
			return false;
		} else
			return false;
	}

	public int deleteDir(Directory dir, ArrayList<Block> spaces,
			ArrayList<Boolean> state) {
		int totalspace = 0;
		for (MyFile file : dir.files) {
			for (Block space : spaces) {
				if (space.start == file.allocatedBlocks.get(0)) {
					space.state = false;
					file.deleted = true;
					for (int i = space.start; i < space.end; i++) {
						state.set(i, false);
					}
					totalspace += space.size;
				}
			}
		}
		for (Directory dire1 : dir.subDirectory) {
			totalspace += deleteDir(dire1, spaces, state);
		}
		dir.deleted = true;
		return totalspace;
	}

	public void write(FileSystem sys, String filePath) {
		try {
			FileOutputStream os = new FileOutputStream(new File(filePath));
			ObjectOutputStream ob = new ObjectOutputStream(os);
			ob.writeInt(sys.sizeKB);
			ob.writeInt(sys.allspace);
//			ob.writeInt(1);
			ob.writeInt(sys.spaces.size());
			for (Block space : sys.spaces) {
				ob.writeInt(space.start);
				ob.writeInt(space.end);
				ob.writeBoolean(space.state);
			}
			ob.writeInt(sys.state.size());
			for (Boolean bool : sys.state) {
				ob.writeBoolean(bool);
			}
			String currentPath = "root";
			writeTree(sys.root, ob, currentPath);
			ob.close();
			os.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeTree(Directory dir, ObjectOutputStream ob,
			String currentPath) throws IOException {
		ob.writeObject(currentPath);
		ob.writeInt(dir.files.size());
		for (MyFile file : dir.files) {
			ob.writeObject(currentPath + "/" + file.name);
			ob.writeInt(file.allocatedBlocks.get(0));
			ob.writeInt(file.allocatedBlocks.get(1));
		}
		for (Directory dire : dir.subDirectory) {
			writeTree(dire, ob, currentPath + "/" + dire.name);
		}
	}
	
	public void readTree(FileSystem sys, ObjectInputStream os, int currentSize,
			int sizeKB) throws ClassNotFoundException, IOException {
		if (currentSize < sizeKB-1) {
			String currentPath = (String) os.readObject();
			String contents = (String) os.readObject();
			String paths[];
			if (currentPath.equals("root")) {
				paths = new String[1];
				paths[0] = "root";
			} else
				paths = currentPath.trim().split("/");
			sys.createFolder(currentPath);
			Directory dir = sys.getDire(sys.root, paths, 0);
			int fileListSize = os.readInt();
			ArrayList<MyFile> files = new ArrayList<>();
			for (int i = 0; i < fileListSize; i++) {
				String temp=(String) os.readObject();
				String s[] = (temp).split("/");
				ArrayList<Integer> allocatedBlocks = new ArrayList<>();
				int start=os.readInt();
				allocatedBlocks.add(start);
				allocatedBlocks.add(os.readInt());
				int size = allocatedBlocks.get(1) - allocatedBlocks.get(0) + 1;
				MyFile file = new MyFile(s[s.length - 1], contents, allocatedBlocks);
				files.add(file);
				currentSize += size;
//				System.out.println(currentSize);
			}
			dir.setFiles(files);
			readTree(sys, os, currentSize, sizeKB);
		}
	}

	public Directory getDire(Directory dir, String[] path, int level) {
		if(path.length==1)
			return dir;
		for (Directory temp : dir.subDirectory) {
			if (path[level+1].equals(temp.name) && level != path.length - 2) {
				return getDire(temp, path, level + 1);
			}
		}
		if (path[level].equals(dir.name) && level == path.length - 2) {
			return dir;
		}
		return null;
	}

	public void ls() {
		root.printDirectoryStructure(0);
	}

	public void showMemUsage() {
		System.out.println("Empty space:\n" + (sizeKB - allspace) + " KB");
		System.out.println("Allocated space:\n" + allspace + " KB");
		System.out.println("Empty Blocks in the Disk:");
		for (int i = 0; i < state.size(); i++) {
			System.out.println("Block:[" + i + "] is "
					+ (state.get(i) ? "Allocated" : "Empty"));
		}
	}
}
