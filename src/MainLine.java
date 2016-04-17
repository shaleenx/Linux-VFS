import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class MainLine {

	/*
	 * Author: Shaleen Kumar Gupta
	 * Date: April 15, 2016
	 * 
	 * A Linux-like Virtual File System for storing files and directories
	 * in an hierarchical directory structure.
	 * 
	 * Implementation Type: Best-fit Contiguous Memory Allocation
	 * 
	 * Undertook as part of Operating Systems Course Project
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
			vfs = read(filePath);
			System.out.println("Loaded");
			/*Start CLI Shell*/
			cmd(vfs);
			System.out.println("Saving System...");
			/*Writing the VFS to hard disk (in MyDisk.sys)*/
			vfs.write(vfs, filePath);
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
			vfs.write(vfs, filePath);
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
						System.out.println("Usage: echo <filename>");
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
				case "cat":
					if(input.length != 2){
						System.out.println("cat - print file on standard output");
						System.out.println("Usage: cat <filename>");
						break;
					}
					if(!vfs.cat(input[1]))
						System.out.println("Incorrect path or File does not exists");
					break;
				case "rm":
					if(input.length != 2){
						System.out.println("rm - remove file");
						System.out.println("Usage: rm <filename>");
						break;
					}
					if(vfs.rm(input[1]))
						System.out.println("File Deleted");
					else
						System.out.println("Error Deleting File");
					break;
				case "mkdir":
					if(input.length != 2){
						System.out.println("mkdir - make new directory");
						System.out.println("Usage: mkdir <foldername>");
						break;
					}
					vfs.mkdir(input[1]);
					System.out.println("Directory created");
					break;
				case "rmdir":
					if(input.length != 2){
						System.out.println("rmdir - remove directory (similar to 'rm -r' on bash)");
						System.out.println("Usage: rmdir <foldername>");
						break;
					}
					vfs.rmdir(input[1]);
					System.out.println("Directory Deleted");
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
					System.out.println("echo, cat, rm, mkdir, rmdir, ls, top");
					break;
				case "exit":
					break;
				default:
					System.out.println("Command Not Found");
					System.out.println("Available Commands:");
					System.out.println("echo, cat, rm, mkdir, rmdir, ls, top");
					break;
			}
		}
	}
	
	/*
	 * Method to read existing VFS from hard disk
	 * Input Params: Path to the '.vfs' file in hard disk
	 * Output Params: A FileSystem object loaded with contents read from the '.vfs' file specified
	 */
	public static FileSystem read(String filePath) throws Exception {
		FileInputStream fis;
		ObjectInputStream ois;
		FileSystem vfs;
		fis = new FileInputStream(new File(filePath));
		ois = new ObjectInputStream(fis);
		int totalSize, usedSpace = 0;
		ArrayList<Boolean> state = new ArrayList<>();
		ArrayList<Block> spaces = new ArrayList<>();
		totalSize = ois.readInt();
		usedSpace = ois.readInt();
		int numberOfSpaceState = ois.readInt();
		for (int i = 0; i < numberOfSpaceState; ++i) {
			int start = ois.readInt();
			int end = ois.readInt();
			boolean b = ois.readBoolean();
			spaces.add(new Block(start, end, b));
		}
		numberOfSpaceState = ois.readInt();
		for (int i = 0; i < numberOfSpaceState; ++i) {
			boolean b = ois.readBoolean();
			state.add(b);
		}
		vfs = new FileSystem(totalSize);
		vfs.iterativeRead(vfs, ois, 0, usedSpace);
		vfs.setUsedSpace(usedSpace);
		vfs.setSpaces(spaces);
		vfs.setState(state);
		return vfs;
	}
}