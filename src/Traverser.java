import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;

public class Traverser implements FileVisitor<Path> {
	private int fileCount;
	private boolean _emptyMap;
	private HashMap<String, Long> _compareToFiles;
	private ArrayList<Path> _newFiles;
	private String _rootPath;

	public Traverser(HashMap<String, Long> compareToFiles, String rootpath) {
		this.fileCount = 0;
		this._emptyMap = compareToFiles == null;
		this._compareToFiles = compareToFiles;
		if (compareToFiles == null)
			this._compareToFiles = new HashMap<String, Long>();
		this._newFiles = new ArrayList<Path>();
		_rootPath = rootpath;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		// TODO Auto-generated method stub
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		// TODO Auto-generated method stub
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		try {
			// Get only the relative path (incase there are more than one file the same
			// name)
			String relativePath = file.toString().substring(_rootPath.length());
			long creationTime = file.toFile().lastModified();
			if (!this._emptyMap) {
				// Check to see if we're comparing files (destination to source (update))
				// Check to see if the directory where we're transferring to contains this files
				// (if it does is it newer?)
				if (!this._compareToFiles.containsKey(relativePath) || (this._compareToFiles.containsKey(relativePath)
						&& this._compareToFiles.get(relativePath) < creationTime)) {
					// If not, mark the item
					this.fileCount++;
					this._newFiles.add(file);
//					System.out.printf("Detect difference: %s.\n", file.toString());
				}
			} else {
				// If not, consider every file as new
				this.fileCount++;
				this._compareToFiles.put(relativePath, creationTime);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.printf("Error with visit to file %s: %s\n", file.toFile().getName(), e.toString());
		}

		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		// TODO Auto-generated method stub
		return FileVisitResult.CONTINUE;
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public HashMap<String, Long> getHashMap() {
		if (this._emptyMap)
			System.err.println("Directory is empty...");
		return this._compareToFiles;
	}

	public ArrayList<Path> getNewFiles() {
		if (this._newFiles.isEmpty())
			System.err.println("ArrayList empty");
		return this._newFiles;
	}

}
