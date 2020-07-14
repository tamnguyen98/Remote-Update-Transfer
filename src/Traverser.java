import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;


public class Traverser implements FileVisitor<Path> {
	private int fileCount;
	private boolean _emptyMap;
	private HashMap<String, Path> _compareToFiles;
	private ArrayList<Path> _newFiles;
	
	public Traverser(HashMap<String, Path> compareToFiles) {
		this.fileCount = 0;
		this._emptyMap = compareToFiles == null;
		this._compareToFiles = compareToFiles;
		if (compareToFiles == null) {
			System.out.println("No Maps passed = " + this._emptyMap);
			this._compareToFiles = new HashMap<String, Path>();
		}
		else
			System.out.println("Maps passed " + compareToFiles.size());
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
		// TODO Auto-generated method stub
		try {
			String fileHash = getCRC32(file);
			if (!this._emptyMap) {
				// Check to see if we're comparing files
				if (!this._compareToFiles.containsKey(fileHash)) {
					this.fileCount++;
					this._newFiles.add(file);
				} else {
					System.out.printf("already have %s\n", this._compareToFiles.get(fileHash));
				}
			} else {
				// If not, consider every file as new
				this.fileCount++;
				this._compareToFiles.put(fileHash, file);
				System.out.println("Adding "+ fileHash);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.printf("Error with visit to file %s: %s\n", file.toFile().getName() ,e.toString());
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		// TODO Auto-generated method stub
		return FileVisitResult.CONTINUE;
	}
	
	private String getMD5(Path path) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		try (InputStream is = Files.newInputStream(path);
		     DigestInputStream dis = new DigestInputStream(is, md)) 
		{
		  /* Read decorated stream (dis) to EOF as normal... */
		}
//		md.update(Files.readAllBytes(Paths.get(path)));
		
		String encoded = new String(Base64.getEncoder().encode(md.digest()));
		System.out.printf("File: %s (%s)\n", path.toString(), encoded);
		return encoded;
	}
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
			System.out.println("Hashmap empty");
		return this._compareToFiles;
	}
	public ArrayList<Path> getNewFiles() {
		if (this._newFiles.isEmpty())
			System.out.println("ArrayList empty");
		return this._newFiles;
	}

}
