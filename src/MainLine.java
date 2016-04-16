import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class MainLine {

	static Scanner sc;
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		FileSystem vfs;
		String filePath = "MyDisk.sys";
		sc = new Scanner(System.in);
		
		try{
			FileInputStream is;
			is = new FileInputStream(new File(filePath));
			System.out.print("Existing VFS found!...");
			vfs = read(filePath);
			System.out.println("Loaded");
			cmd(vfs);
			vfs.write(vfs, filePath);
		}
		catch(FileNotFoundException e){
			System.out.println("Enter size of new VFS: ");
			int totalSize = sc.nextInt(); 
			vfs = new FileSystem(totalSize);
			System.out.println("New File System Initialized");
			cmd(vfs);
			vfs.write(vfs, filePath);
		}
		System.out.println("");
	}
	
	public static void cmd(FileSystem vfs){
		String cmd = "";
		sc = new Scanner(System.in);
		while(!cmd.equalsIgnoreCase("exit")){
			System.out.print(">>>");
			cmd = sc.nextLine().trim();
			String input[] = cmd.split(" ");
			switch(input[0]){
				case "touch":
					if(input.length != 2){
						System.out.println("touch - create new file");
						System.out.println("Usage: create <filename>");
						break;
					}
					System.out.print("Enter Size: ");
					int size = sc.nextInt();
					if(vfs.createFile(input[1], size))
						System.out.println("File Created");
					else
						System.out.println("Error Creating File");
					break;
				case "rm":
					if(input.length != 2){
						System.out.println("rm - remove file");
						System.out.println("Usage: rm <filename>");
						break;
					}
					if(vfs.deleteFile(input[1]))
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
					vfs.createFolder(input[1]);
					System.out.println("->folder has been created");
					break;
				case "rmdir":
					if(input.length != 2){
						System.out.println("rmdir - remove directory (similar to 'rm -r' on bash)");
						System.out.println("Usage: rmdir <foldername>");
						break;
					}
					vfs.deleteFolder(input[1]);
					System.out.println("->folder has been deleted");
					break;
				case "ls":
					if(input.length != 1){
						System.out.println("ls - list all files");
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
				default:
					System.out.println("Command Not Found");
					System.out.println("Available Commands:");
					System.out.println("create <filename>, rm <filename>, ls, showmemusage");
					break;
			}
		}
		System.out.println("Saving System... Exiting Program...");
		System.out.println("Program exited gracefully");
	}
	
	public static void Main(FileSystem sys) {
		String command = "";
		while (!command.equals("exit")) {
			command = new Scanner(System.in).nextLine();
			String str[] = command.trim().split(" ");
			switch (str[0]) {
			case "createfile":
				sys.createFile(str[1], Integer.parseInt(str[2]));
				System.out.println("->file has been created");
				break;
			case "createfolder":
				sys.createFolder(str[1]);
				System.out.println("->folder has been created");
				break;
			case "deletefolder":
				sys.deleteFolder(str[1]);
				System.out.println("->folder has been deleted");
				break;
			case "deletefile":
				sys.deleteFile(str[1]);
				System.out.println("->file has been deleted");
				break;
			case "status":
//				sys.DisplayDiskStatus();
				break;
			case "structure":
//				sys.DisplayDiskStructure();
				break;
			case "exit":
				break;
			default:
				System.out.println("->No match command");
			}
		}
	}
	
	public static FileSystem read(String filePath) throws Exception {
		FileInputStream is;
		ObjectInputStream os;
		FileSystem sys;
		is = new FileInputStream(new File(filePath));
		os = new ObjectInputStream(is);
		int sizeKB, allspace = 0;
		int t;
		Directory root;
		ArrayList<Boolean> state = new ArrayList<>();
		ArrayList<Block> spaces = new ArrayList<>();
		int currentSize = 0;
		sizeKB = os.readInt();
		allspace = os.readInt();
//		t = os.readInt();
//		if (t == 1)
//			tech1 = new Contiguous();
//		else
//			tech1 = new Indexed();
		int spa = os.readInt();
		for (int i = 0; i < spa; i++) {
			int start = os.readInt();
			int end = os.readInt();
			boolean b = os.readBoolean();
			spaces.add(new Block(start, end, b));
		}
		spa = os.readInt();
		for (int i = 0; i < spa; i++) {
			boolean b = os.readBoolean();
			state.add(b);
		}
		Directory dir = new Directory();
		sys = new FileSystem(sizeKB);
		sys.readTree(sys, os, 0, allspace);
		sys.setAllspace(allspace);
		sys.setSpaces(spaces);
		sys.setState(state);
		// sys.setRoot(dir);
		return sys;
	}

	
}
