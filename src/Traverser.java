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
	private HashMap<String, Path> _compareToFiles;
	private ArrayList<Path> _newFiles;

	public Traverser(HashMap<String, Path> compareToFiles) {
		this.fileCount = 0;
		this._emptyMap = compareToFiles == null;
		this._compareToFiles = compareToFiles;
		if (compareToFiles == null)
			this._compareToFiles = new HashMap<String, Path>();
		this._newFiles = new ArrayList<Path>();
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
			String fileHash = getCRC32(file);
			if (!this._emptyMap) {
				// Check to see if we're comparing files
				if (!this._compareToFiles.containsKey(fileHash)) {
					this.fileCount++;
					this._newFiles.add(file);
					System.out.printf("Detect difference: %s\n", file.toString());
				}
			} else {
				// If not, consider every file as new
				this.fileCount++;
				this._compareToFiles.put(fileHash, file);
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

	// Using CRC since it's faster and we don't care about security
	private String getCRC32(Path path) throws Exception {
		long crc = Hasher.checksumBufferedInputStream(path.toString());
		return Long.toHexString(crc);
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public HashMap<String, Path> getHashMap() {
		if (this._emptyMap)
			System.err.println("Hashmap empty");
		return this._compareToFiles;
	}

	public ArrayList<Path> getNewFiles() {
		if (this._newFiles.isEmpty())
			System.err.println("ArrayList empty");
		return this._newFiles;
	}

}
