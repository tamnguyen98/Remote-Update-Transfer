import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class ItemTracker {
	private String _src, _dest;
	public HashMap<String, Path> _newFiles;
	private ArrayList<Path> _filesToTransfer;

	public ItemTracker(String src, String dest) {
		this._src = src;
		this._dest = dest;
		println(src + "|" + dest);
		this._newFiles = new HashMap<String, Path>();
		_filesToTransfer = new ArrayList<Path>();
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
		// so we want to check what new files Src has that Dest doesnt have
		Traverser dest = new Traverser(null);
		Files.walkFileTree(Paths.get(this._dest), dest);

		this._newFiles = dest.getHashMap();
		System.out.printf("HashMap size: %d\n", this._newFiles.size());

		Traverser src = new Traverser(this._newFiles);
		Files.walkFileTree(Paths.get(this._src), src);

		this._filesToTransfer = src.getNewFiles();

		System.out.printf("Destination file count: %d\t Src file count: %d\nList size: %d\n", dest.getFileCount(),
				src.getFileCount(), this._filesToTransfer.size());

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
		TransferManager tm = new TransferManager(this._src, this._dest);
		for (Path s : toPrint) {
			println(i++ + ": " + s.toString());
		}
	}

	private void println(String s) {
		System.out.println(s);
	}

}
