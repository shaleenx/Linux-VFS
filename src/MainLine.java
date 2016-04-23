import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class MainLine {

	/*
	 * Author: Shaleen Kumar Gupta (DA-IICT, India)
	 * Email: shaleenx@gmail.com
	 * Date: April 15, 2016
	 * 
	 * A Linux-like Virtual File System for storing files and directories
	 * in an hierarchical directory structure.
	 * Particular focus has been on improvising the free space allocation 
	 * in the virtual file system. 
	 * 
	 * Implementation Type: Best-fit Contiguous Memory Allocation
	 * 
	 * However, the code can be easily tweaked to implement other 
	 * memory allocation techniques, as described in comments. 
	 * 
	 * The project has been undertaken with the intention of learning how 
	 * files are managed on a Linux like system. The file system implemented 
	 * also implements a Linux Shell-like interactive command like interface 
	 * with functionalities like creating files and folders, deleting files 
	 * and folders, viewing the contents of a file, summarize memory usage, 
	 * and list all files and folders.
	 * 
	 * Undertook as part of IT308 Operating Systems Course Project
	 * 
	 * Credits: Modern Operating Systems (Andrew S. Tannebaum)
	 */
	
	static Scanner sc;
	@SuppressWarnings("unused")
	private static FileInputStream is;
	
	public static void main(String[] args) throws Exception {
		FileSystem vfs;
		String filePath = "MyDisk.sys";	/* The VFS storage in hard disk */
		sc = new Scanner(System.in);
		
		try{
			/* For existing VFS, if found at the specified location*/
			is = new FileInputStream(new File(filePath));
			System.out.print("Existing VFS found!...");
			vfs = readVFS(filePath);
			System.out.println("Loaded");
			/*Start CLI Shell*/
			cmd(vfs);
			System.out.println("Saving System...");
			/*Writing the VFS to hard disk (in MyDisk.sys)*/
			vfs.write(filePath);
			System.out.println("System Saved");
			System.out.println("Program exited gracefully");
		}
		catch(FileNotFoundException e){
			/* If an existing VFS is not found, create a new VFS */
			System.out.print("Enter size of new VFS (in Bytes): ");
			int totalSize = sc.nextInt(); 
			vfs = new FileSystem(totalSize);
			System.out.println("New File System Initialized");
			cmd(vfs);
			System.out.println("Saving System...");
			/*Writing the VFS to hard disk (in MyDisk.sys)*/
			vfs.write(filePath);
			System.out.println("System Saved");
			System.out.println("Program exited gracefully");
		}
		System.out.println("");
	}
	
	/*
	 * Method to run the CLI Shell
	 * 
	 * Input params: A FileSystem object to store data in
	 * Output Params: Void
	 */
	public static void cmd(FileSystem vfs){
		String cmd = "";
		sc = new Scanner(System.in);
		while(!cmd.equalsIgnoreCase("exit")){
			System.out.print("shaleen@localhost:~$ ");	/*That would be me*/
			cmd = sc.nextLine().trim();
			String input[] = cmd.split(" ");
			switch(input[0]){
				case "echo":
					if(input.length != 2){
						System.out.println("echo - write contents from the console into a new file");
						System.out.println("Usage: echo <path-from-root/filename>");
						break;
					}
					System.out.println("Enter Contents: ");
					String contents = sc.nextLine().trim();
					int size = contents.length();
					if(vfs.create(input[1], contents, size))
						System.out.println("File Created");
					else
						System.out.println("Error Creating File");
					break;

				case "edit":
					if(input.length != 2){
						System.out.println("edit - edit contents from the console into an existing file");
						System.out.println("Usage: edit <path-from-root/filename>");
						break;
					}
					System.out.println("Enter New Contents: ");
					String contents1 = sc.nextLine().trim();
					int size1 = contents1.length();
					if(vfs.edit(input[1], contents1, size1))
						System.out.println("File Edited");
					else
						System.out.println("File not found. Error Editing File");
					break;
				case "cat":
					if(input.length != 2){
						System.out.println("cat - print file on standard output");
						System.out.println("Usage: cat <path-to-file-from-root>");
						break;
					}
					if(!vfs.cat(input[1]))
						System.out.println("Incorrect path or File does not exists");
					break;
				case "rm":
					if(input.length != 2){
						System.out.println("rm - remove file");
						System.out.println("Usage: rm <path-to-file-from-root/filename>");
						break;
					}
					if(vfs.rm(input[1]))
						System.out.println("File Deleted");
					else
						System.out.println("Error Deleting File. No such file found.");
					break;
				case "mkdir":
					if(input.length != 2){
						System.out.println("mkdir - make new directory");
						System.out.println("Usage: mkdir <path-from-root/foldername>");
						break;
					}
					if(vfs.mkdir(input[1]))
						System.out.println("Directory created");
					else
						System.out.println("Error creating directory");
					break;
				case "rmdir":
					if(input.length != 2){
						System.out.println("rmdir - remove directory (similar to 'rm -r' on bash)");
						System.out.println("Usage: rmdir <path-from-root/foldername>");
						break;
					}
					if(vfs.rmdir(input[1]))
						System.out.println("Directory Deleted");
					else
						System.out.println("Error deleting directory. No such directory found");
					break;
				case "ls":
					if(input.length != 1){
						System.out.println("ls - list all files and directories");
						System.out.println("Usage: ls");
						break;
					}
					vfs.ls();
					break;
				case "top":
					if(input.length != 1){
						System.out.println("top - show memory usage");
						System.out.println("Usage: top");
						break;
					}
					vfs.showMemUsage();
					break;
				case "help":
					System.out.println("Available Commands:");
					System.out.println("echo, edit, cat, rm, mkdir, rmdir, ls, top");
					break;
				case "exit":
					break;
				default:
					System.out.println("Command Not Found");
					System.out.println("Available Commands:");
					System.out.println("echo, edit, cat, rm, mkdir, rmdir, ls, top");
					break;
			}
		}
	}
	
	/*
	 * Method to read existing VFS from hard disk
	 * Input Params: Path to the '.vfs' file in hard disk
	 * Output Params: A FileSystem object loaded with contents read from the '.vfs' file specified
	 */
	public static FileSystem readVFS(String filePath) throws Exception {
		FileInputStream fis;
		ObjectInputStream ois;
		FileSystem vfs;
		fis = new FileInputStream(new File(filePath));
		ois = new ObjectInputStream(fis);
		int totalSize, usedSpace = 0;
		ArrayList<Boolean> state = new ArrayList<>();
		ArrayList<EmptyBlock> spaces = new ArrayList<>();
		totalSize = ois.readInt();
		usedSpace = ois.readInt();
		int numberOfSpaceState = ois.readInt();
		for (int i = 0; i < numberOfSpaceState; ++i) {
			int start = ois.readInt();
			int end = ois.readInt();
			boolean b = ois.readBoolean();
			spaces.add(new EmptyBlock(start, end, b));
		}
		numberOfSpaceState = ois.readInt();
		for (int i = 0; i < numberOfSpaceState; ++i) {
			boolean b = ois.readBoolean();
			state.add(b);
		}
		vfs = new FileSystem(totalSize);
		vfs.iterativeRead(ois, 0, usedSpace);
		vfs.setUsedSpace(usedSpace);
		vfs.setSpaces(spaces);
		vfs.setState(state);
		return vfs;
	}
}
