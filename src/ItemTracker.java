import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.security.MessageDigest;

public class ItemTracker {
	private String _src, _dest;
	public HashMap<String, Path> _newFiles;
	public ArrayList<Path> filesToTransfer;
	
	
	public ItemTracker(String src, String dest) {
		this._src = src;
		this._dest = dest;
		println(src + "|" + dest);
		this._newFiles = new HashMap<String, Path>();
		filesToTransfer = new ArrayList<Path>();
	}
	
	public void setNewDestination(String src, String dest) {
		if (!src.isEmpty())
			this._src = src;
		if (!dest.isEmpty())
			this._dest = dest;
	}
	
	public boolean detectDifference() throws IOException {
		// Traverse through and get a copy of all files' md5
		
		// NOTE: Src contains "new" files and Dest contains "old" file
		// so we want to check what new files Src has that  Dest doesnt have
		Traverser dest = new Traverser(null);
		Files.walkFileTree(Paths.get(this._dest), dest);
		
		this._newFiles = dest.getHashMap();
		System.out.printf("HashMap size: %d\n", this._newFiles.size());
		
		Traverser src = new Traverser(this._newFiles);
		Files.walkFileTree(Paths.get(this._src), src);
		
		this.filesToTransfer = src.getNewFiles();
		
		System.out.printf("Destination file count: %d\t Src file count: %d\nList size: %d\n", dest.getFileCount(), src.getFileCount(), this.filesToTransfer.size());
		
		printNewFiles();
		
		return !filesToTransfer.isEmpty();
	}
	
	public void printNewFiles() {
		if (filesToTransfer.isEmpty()) {
			println("No new files");
			return;
		}
		int i = 0;
		for(Path s: filesToTransfer) {
			println(i++ + ": " + s.toString());
		}
	}
	
	// https://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java#answer-26670214
//	private byte[] getMD5(String path) throws Exception {
//		MessageDigest md = MessageDigest.getInstance("MD5");
//		md.update(Files.readAllBytes(Paths.get(path)));
//		return md.digest();
//	}
	
	private void println(String s) {
		System.out.println(s);
	}
	
}
