import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class FileSystem {
	int totalSize, usedSpace = 0;
	Directory root;
	ArrayList<Boolean> state;
	public int getUsedSpace() {
		return usedSpace;
	}

	public void setUsedSpace(int usedSpace) {
		this.usedSpace = usedSpace;
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

	public FileSystem(int totalSize) {
		this.totalSize = totalSize;
		state = new ArrayList<>(totalSize);
		for (int i = 0; i < totalSize; i++) {
			state.add(false);
		}
		root = new Directory("root");
		spaces = new ArrayList<>();
		spaces.add(new Block(0, totalSize - 1, false));
	}

	public boolean create(String path, String contents, int fileSize) {
		if (fileSize > this.totalSize - this.usedSpace){
			System.out.println("Memory Full");
			return false;	
		}
		String[] paths = path.trim().split("/");
		Directory i;
		i = findDirectory(root, paths, 0);
		if (i != null) {
			if (iterativeCreate(i, paths[paths.length - 1], contents, fileSize, spaces,
					state)) {
				this.usedSpace += fileSize;
				return true;
			} else
				return false;
		} else
			return false;
	}

	public boolean iterativeCreate(Directory dir, String name, String contents, int totalSize,
			ArrayList<Block> blocks, ArrayList<Boolean> state) {
		for (Block blocki : blocks) {
			if (totalSize <= blocki.size && !blocki.state) {
				ArrayList<Integer> usedBlocks = new ArrayList<>();
				usedBlocks.add(blocki.start);
				usedBlocks.add(blocki.start + totalSize - 1);
				MyFile file = new MyFile(name, contents, usedBlocks);
				dir.myFiles.add(file);
				for (int i = blocki.start; i < blocki.start + totalSize; ++i) {
					state.set(i, true);
				}
				if (totalSize < blocki.size) {
					Block tempBlock = new Block(blocki.start + totalSize, blocki.end,
							false);
					blocks.add(tempBlock);
					blocki.end = blocki.start + totalSize - 1;
				}
				blocki.state = true;
				/*
				 * The comparator is invoked to sort the ArrayList.
				 * 
				 * Reversing the comparator will make the code 
				 * implement worst-fit contiguous memory allocation
				 * from best-fit memory allocation
				 */
				Collections.sort(blocks, new SpaceSizeCmp());
				return true;
			}
		}
		return false;	/*File Creation Failed*/
	}
	
	public boolean mkdir(String path) {
		String[] paths = path.trim().split("/");
		Directory i;
		i = findDirectory(root, paths, 0);
		if (i != null)
			return iterativeMkdir(i, paths[paths.length - 1]);
		else
			return false;
	}
	
	public boolean iterativeMkdir(Directory dir, String name) {
		dir.subDirectory.add(new Directory(name));
		return true;
	}

	public boolean rm(String path) {
		String[] paths = path.trim().split("/");
		Directory i;
		i = findDirectory(root, paths, 0);
		if (i != null) {
			int fileSize = iterativeRm(i, paths[paths.length - 1],
					spaces, state);
			if (fileSize != 0) {
				this.usedSpace -= fileSize;
				return true;
			}
			return false;
		} else
			return false;
	}
	
	public int iterativeRm(Directory dir, String name, ArrayList<Block> spaces,
			ArrayList<Boolean> state) {
		for (MyFile file : dir.myFiles) {
			if (file.fileName.equals(name)) {
				for (Block space : spaces) {
					if (space.start == file.usedBlocks.get(0)) {
						System.out.println("size deleted: " + file.getFileSize());
						space.state = false;
						file.isDeleted = true;
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
	
	public boolean cat(String path){
		String[] paths = path.trim().split("/");
		Directory iter;
		iter = findDirectory(root, paths, 0);
		if (iter != null) {
			for(MyFile file: iter.myFiles){
				if(file.fileName.equals(paths[paths.length-1])){
					System.out.println(file.contents);
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean rmdir(String path) {
		String[] paths = path.trim().split("/");
		Directory iter;
		iter = findDirectory(root, paths, 0);
		if (iter != null) {
			int filesSize = iterativeRmdir(iter, spaces, state);
			if (filesSize != 0) {
				this.usedSpace -= filesSize;
				return true;
			}
			return false;
		} else
			return false;
	}

	public int iterativeRmdir(Directory dir, ArrayList<Block> spaces,
			ArrayList<Boolean> state) {
		int totalspace = 0;
		for (MyFile file : dir.myFiles) {
			for (Block space : spaces) {
				if (space.start == file.usedBlocks.get(0)) {
					space.state = false;
					file.isDeleted = true;
					for (int i = space.start; i < space.end; i++) {
						state.set(i, false);
					}
					totalspace += space.size;
				}
			}
		}
		for (Directory dire1 : dir.subDirectory) {
			totalspace += iterativeRmdir(dire1, spaces, state);
		}
		dir.isDeleted = true;
		return totalspace;
	}

	public void write(FileSystem vfs, String filePath) {
		try {
			FileOutputStream fos = new FileOutputStream(new File(filePath));
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeInt(vfs.totalSize);
			oos.writeInt(vfs.usedSpace);
			oos.writeInt(vfs.spaces.size());
			for (Block space : vfs.spaces) {
				oos.writeInt(space.start);
				oos.writeInt(space.end);
				oos.writeBoolean(space.state);
			}
			oos.writeInt(vfs.state.size());
			for (Boolean bool : vfs.state) {
				oos.writeBoolean(bool);
			}
			String currentPath = "root";
			iterativeWrite(vfs.root, oos, currentPath);
			oos.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void iterativeWrite(Directory dir, ObjectOutputStream oos,
			String currentPath) throws IOException {
		oos.writeObject(currentPath);
		oos.writeInt(dir.myFiles.size());
		for (MyFile file : dir.myFiles) {
			oos.writeObject(currentPath + "/" + file.fileName);
			oos.writeObject(file.contents);
			oos.writeInt(file.usedBlocks.get(0));
			oos.writeInt(file.usedBlocks.get(1));
		}
		for (Directory dire : dir.subDirectory) {
			iterativeWrite(dire, oos, currentPath + "/" + dire.dirName);
		}
	}
	
	public void iterativeRead(FileSystem vfs, ObjectInputStream ois, int currentSize,
			int totalsize) throws ClassNotFoundException, IOException {
		if (currentSize < totalsize-1) {
			String currentPath = (String) ois.readObject();
			String paths[];
			if (currentPath.equals("root")) {
				paths = new String[1];
				paths[0] = "root";
			} else
				paths = currentPath.trim().split("/");
			vfs.mkdir(currentPath);
			Directory dir = vfs.findDirectory(vfs.root, paths, 0);
			int fileListSize = ois.readInt();
			ArrayList<MyFile> files = new ArrayList<>();
			for (int i = 0; i < fileListSize; i++) {
				String temp=(String) ois.readObject();
				String s[] = (temp).split("/");
				String contents = (String) ois.readObject();
				ArrayList<Integer> usedBlocks = new ArrayList<>();
				int start=ois.readInt();
				usedBlocks.add(start);
				usedBlocks.add(ois.readInt());
				int size = usedBlocks.get(1) - usedBlocks.get(0)+1;
//				System.out.println(size);
				MyFile file = new MyFile(s[s.length - 1], contents, usedBlocks);
				files.add(file);
				currentSize += size;
			}
			dir.setFiles(files);
			iterativeRead(vfs, ois, currentSize, totalsize);
		}
	}

	public Directory findDirectory(Directory dir, String[] path, int level) {
		if(path.length==1)
			return dir;
		for (Directory temp : dir.subDirectory) {
			if (path[level+1].equals(temp.dirName) && level != path.length - 2) {
				return findDirectory(temp, path, level + 1);
			}
		}
		if (path[level].equals(dir.dirName) && level == path.length - 2) {
			return dir;
		}
		return null;
	}

	public void ls() {
		System.out.println("Directories are names with parenthesis \'()'");
		root.showFileList(0);
	}

	public void showMemUsage() {
		System.out.println("Total Size of Virtual Disk: "+this.totalSize + " Bytes");
		System.out.println("Used Space: " + this.usedSpace + " Bytes");
		System.out.println("Free Space: " + (this.totalSize - this.usedSpace) + " Bytes");
		System.out.println("*************Memory Blocks**************");
		for (int i = 0; i < state.size(); i++) {
			if(state.get(i))
				System.out.println("Block["+i+"]: " + "<Filled>");
			else
				System.out.println("Block["+i+"]: " + "<Empty>");
		}
		System.out.println("*************Memory Blocks**************");
	}
}
