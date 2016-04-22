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
	ArrayList<EmptyBlock> emptyBlocks;
	
	/*
	 * Getters and Setters
	 */
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

	public ArrayList<EmptyBlock> getSpaces() {
		return emptyBlocks;
	}

	public void setSpaces(ArrayList<EmptyBlock> spaces) {
		this.emptyBlocks = spaces;
	}

	
	/*
	 * The FileSystem constructor (takes in total size of the vfs to create)
	 */
	public FileSystem(int totalSize) {
		this.totalSize = totalSize;
		state = new ArrayList<>(totalSize);
		for (int i = 0; i < totalSize; i++) {
			state.add(false);
		}
		root = new Directory("root");
		emptyBlocks = new ArrayList<>();
		emptyBlocks.add(new EmptyBlock(0, totalSize - 1, false));
	}

	/*
	 * Method to create new file
	 */
	public boolean create(String path, String contents, int fileSize) {
		if (fileSize > this.totalSize - this.usedSpace){
			System.out.println("Memory Full");
			return false;	
		}
		String[] paths = path.trim().split("/");
		Directory i;
		i = findDirectory(root, paths, 0);
		if (i != null) {
			if (iterativeCreate(i, paths[paths.length - 1], contents, fileSize)) {
				this.usedSpace += fileSize;
				return true;
			} else{
				System.out.println("create failed");
				return false;	
			}
		} else
			return false;
	}

	/*
	 * Method to actually iterate over directories, 
	 * find the exact location of the file to be made in, 
	 * and create the file
	 * 
	 * Returns the size of the file created.
	 */
	public boolean iterativeCreate(Directory dir, String name, String contents, int fileSize) {
		for (EmptyBlock blocki : emptyBlocks) {
			if (fileSize <= blocki.getSize() && !blocki.state) {
				ArrayList<Integer> usedBlocks = new ArrayList<>();
				usedBlocks.add(blocki.start);
				usedBlocks.add(blocki.start + fileSize - 1);
				MyFile file = new MyFile(name, contents, fileSize, usedBlocks);
				dir.myFiles.add(file);
				for (int i = blocki.start; i < blocki.start + fileSize; ++i) {
					state.set(i, true);
				}
				if (fileSize < blocki.getSize()) {
					EmptyBlock tempBlock = new EmptyBlock(blocki.start + fileSize, blocki.end,
							false);
					emptyBlocks.add(tempBlock);
					blocki.end = blocki.start + fileSize - 1;
					blocki.size = blocki.getSize();
//					System.out.println(blocki.size);
				}
				blocki.state = true;
				for(EmptyBlock bi: emptyBlocks)
					bi.size = bi.getSize();
				/*
				 * The comparator is invoked to sort the ArrayList.
				 * 
				 * Reversing the comparator will make the code 
				 * implement worst-fit contiguous memory allocation
				 * from best-fit memory allocation
				 */
				Collections.sort(emptyBlocks, new SpaceSizeCmp());
				return true;
			}
		}
		return false;	/*File Creation Failed*/
	}
	
	/*
	 * Method to create new directory
	 */
	public boolean mkdir(String path) {
		String[] paths = path.trim().split("/");
		Directory i;
		i = findDirectory(root, paths, 0);
		if (i != null)
			return iterativeMkdir(i, paths[paths.length - 1]);
		else
			return false;
	}
	

	/*
	 * Method to actually iterate over directories, 
	 * find the exact location of the new directory
	 * to be made in, and create the new directory
	 */
	public boolean iterativeMkdir(Directory dir, String name) {
		dir.subDirectory.add(new Directory(name));
		return true;
	}

	public boolean rm(String path) {
		String[] paths = path.trim().split("/");
		Directory i;
		i = findDirectory(root, paths, 0);
		if (i != null) {
			int fileSize = iterativeRm(i, paths[paths.length - 1]);
			if (fileSize != 0) {
				this.usedSpace -= fileSize;
				return true;
			}
			return false;
		} else
			return false;
	}
	
	/*
	 * Method to actually iterate over directories, 
	 * find the exact location of the file and 
	 * delete the file
	 * 
	 * Returns the size of the file deleted
	 */
	public int iterativeRm(Directory dir, String name) {
		for (MyFile file : dir.myFiles) {
			if (file.fileName.equals(name) && !file.isDeleted) {
				for (EmptyBlock blocki : emptyBlocks) {
					if (blocki.start == file.usedBlocks.get(0) && blocki.state == true) {
						blocki.state = false;
						file.isDeleted = true;
						for (int i = blocki.start; i <= blocki.end; i++) {
							state.set(i, false);
						}
//						System.out.println(blocki.getSize()+" "+file.getFileSize());
						return file.getFileSize();
					}
				}
				return 0;
			}
		}
		return 0;
	}
	
	/*
	 * Method to display the contents of a file
	 * 
	 * Returns false when the file is not found
	 * at the specified path
	 */
	public boolean cat(String path){
		String[] paths = path.trim().split("/");
		Directory iter;
		iter = findDirectory(root, paths, 0);
		if (iter != null) {
			for(MyFile file: iter.myFiles){
				if(file.fileName.equals(paths[paths.length-1]) && !file.isDeleted){
					System.out.println(file.contents);
					return true;
				}
			}
		}
		return false;
	}
	
	/*
	 * Method to remove a directory
	 */
	public boolean rmdir(String path) {
		String[] paths = path.trim().split("/");
		Directory iter;
		iter = findDirectory(root, paths, 0);
		if (iter != null) {
			int filesSize = iterativeRmdir(iter);
			if (filesSize != 0) {
				this.usedSpace -= filesSize;
				return true;
			}
			return false;
		} else
			return false;
	}

	/*
	 * Method to actually iterate over directories, 
	 * find the exact location of the directory
	 * and delete it.
	 * 
	 * Returns the size of the directory deleted.
	 */
	public int iterativeRmdir(Directory dir) {
		int totalspace = 0;
		for (MyFile file : dir.myFiles) {
			for (EmptyBlock blocki : emptyBlocks) {
				if (blocki.start == file.usedBlocks.get(0)) {
					blocki.state = false;
					file.isDeleted = true;
					for (int i = blocki.start; i <= blocki.end; i++) {
						state.set(i, false);
					}
					totalspace += file.getFileSize();
				}
			}
		}
		for (Directory dire1 : dir.subDirectory) {
			totalspace += iterativeRmdir(dire1);
		}
		dir.isDeleted = true;
		return totalspace;
	}

	/*
	 * Method to save the virtual file system in hard disk
	 * so that it may be reloaded into the program even 
	 * after shutting down the program
	 */
	public void write(String filePath) {
		try {
			FileOutputStream fos = new FileOutputStream(new File(filePath));
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeInt(this.totalSize);
			oos.writeInt(this.usedSpace);
			oos.writeInt(this.emptyBlocks.size());
			for (EmptyBlock space : this.emptyBlocks) {
				oos.writeInt(space.start);
				oos.writeInt(space.end);
				oos.writeBoolean(space.state);
			}
			oos.writeInt(this.state.size());
			for (Boolean bool : this.state) {
				oos.writeBoolean(bool);
			}
			String currentPath = "root";
			iterativeWrite(this.root, oos, currentPath);
			oos.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Helper method to write() which iterates over 
	 * all the directories and write the contents 
	 * in the file. 
	 * 
	 * The method happens to be recursive though.
	 */
	private void iterativeWrite(Directory dir, ObjectOutputStream oos,
			String currentPath) throws IOException {
		
		ArrayList<MyFile> templist = new ArrayList<>();
		for(MyFile f: dir.myFiles)
			if(!f.isDeleted)
				templist.add(f);
		dir.setFiles(templist);
		
		oos.writeObject(currentPath);
		oos.writeInt(dir.myFiles.size());
		for (MyFile file : dir.myFiles) {
			if(!file.isDeleted){
				oos.writeObject(currentPath + "/" + file.fileName);
				oos.writeObject(file.contents);
				oos.writeInt(file.getFileSize());
				oos.writeInt(file.usedBlocks.get(0));
				oos.writeInt(file.usedBlocks.get(1));
			}
		}
		ArrayList<Directory> tempdirlist = new ArrayList<>();
		for(Directory direc: dir.subDirectory)
			if(!direc.isDeleted)
				tempdirlist.add(direc);
		dir.setSubDirectory(tempdirlist);
		
		for (Directory dire : dir.subDirectory) {
			iterativeWrite(dire, oos, currentPath + "/" + dire.dirName);
		}
	}
	
	/*
	 * Iteratively read the contents of the saved hard disk
	 * file and load it into the VFS FileSystem object.
	 * 
	 * The method happens to be recursive though.
	 */
	public void iterativeRead(ObjectInputStream ois, int currentSize,
			int totalsize) throws ClassNotFoundException, IOException {
		if (currentSize < totalsize-1) {
			String currentPath = (String) ois.readObject();
			String paths[];
			if (currentPath.equals("root")) {
				paths = new String[1];
				paths[0] = "root";
			} 
			else{
				paths = (currentPath+"/foo").trim().split("/");
				this.mkdir(currentPath);
			}	
			Directory dir = this.findDirectory(root, paths, 0);
			System.out.println(currentPath+" "+dir.dirName);
			int fileListSize = ois.readInt();
			ArrayList<MyFile> files = new ArrayList<>();
			for (int i = 0; i < fileListSize; i++) {
				String temp=(String) ois.readObject();
				String s[] = (temp).split("/");
				String contents = (String) ois.readObject();
				int fileSize = (int) ois.readInt();
				ArrayList<Integer> usedBlocks = new ArrayList<>();
				int start=ois.readInt();
				usedBlocks.add(start);
				usedBlocks.add(ois.readInt());
				int size = usedBlocks.get(1) - usedBlocks.get(0)+1;
//				System.out.println(size);
				MyFile file = new MyFile(s[s.length - 1], contents, fileSize, usedBlocks);
				files.add(file);
				currentSize += size;
			}
//			System.out.println("setting "+files.toArray()+" to "+dir.dirName);
			dir.setFiles(files);
			iterativeRead(ois, currentSize, totalsize);
		}
	}

	/*
	 * Method to find a particular directory, given the path.
	 */
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

	/*
	 * Method to display all the files and folder in the virtual file system
	 */
	public void ls() {
		System.out.println("Directories are names with parenthesis \'()'");
		root.showFileList(0);
	}

	/*
	 * Method to show memory usage.
	 */
	public void showMemUsage() {
		System.out.println("Total Size of Virtual Disk: "+this.totalSize + " Bytes");
		System.out.println("Used Space: " + this.usedSpace + " Bytes");
		System.out.println("Free Space: " + (this.totalSize - this.usedSpace) + " Bytes");
 		for (int i = 0; i < state.size(); i++) {
			System.out.println("Block:[" + i + "] is "
					+ (state.get(i) ? "Allocated" : "Empty"));
		}
	}
}
