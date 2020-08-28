import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class ItemTracker {
	private String _src, _dest;
	public HashMap<String, Long> _newFiles;
	private ArrayList<Path> _filesToTransfer;

	public ItemTracker(String src, String dest) {
		this._src = src;
		this._dest = dest;
		println(src + "->" + dest);
		this._newFiles = new HashMap<String, Long> ();
		_filesToTransfer = new ArrayList<Path> ();
	}

	public void setNewDestination(String src, String dest) {
		if (!src.isEmpty())
			this._src = src;
		if (!dest.isEmpty())
			this._dest = dest;
	}

	public boolean localDetectDifference() throws IOException {
		// Traverse through and get a copy of all files' md5
		System.out.println("Collecting item in source directory...");
		// NOTE: Src contains "new" files and Dest contains "old" file
		// so we want to check what new files Src has that Dest doesnt have
		Traverser dest = new Traverser(null, this._dest);
		Files.walkFileTree(Paths.get(this._dest), dest);

		this._newFiles = dest.getHashMap();
		//		System.out.printf("HashMap size: %d\n", this._newFiles.size());

		System.out.println("Finding changes...");
		
		Traverser src = new Traverser(this._newFiles, this._src);
		Files.walkFileTree(Paths.get(this._src), src);

		this._filesToTransfer = src.getNewFiles();

		System.out.printf("Difference marked (%d files)...\n", this._filesToTransfer.size());

		return !_filesToTransfer.isEmpty();
	}

	public boolean remoteDetectDiffernce(HashMap<String, Long> checkWith) throws IOException {
		System.out.println("Finding changes...");
		
		Traverser src = new Traverser(checkWith, this._src);
		Files.walkFileTree(Paths.get(this._src), src);

		this._filesToTransfer = src.getNewFiles();

		System.out.printf("Difference marked (%d files)...\n", this._filesToTransfer.size());

		return !_filesToTransfer.isEmpty();
	}

	public ArrayList<Path> getFilesToTransfer() {
		return this._filesToTransfer;
	}

	public void printNewFiles() {
		if (_filesToTransfer.isEmpty()) {
			println("No new files");
			return;
		}
		printNewFiles(this._filesToTransfer);
	}

	public void printNewFiles(ArrayList<Path> toPrint) {
		int i = 0;
		System.out.print(GlobalTools.ANSI_CYAN);
		for (Path s: toPrint) {
			System.out.println(s.toString());
			i++;
		}
		System.out.println(GlobalTools.ANSI_RESET + "Total fail count: " + i);
	}

	private void println(String s) {
		System.out.println(s);
	}

}