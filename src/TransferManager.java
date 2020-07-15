import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

public class TransferManager {
	private String _src, _dest;

	public TransferManager(String src, String dest) {
		this._dest = dest;
		this._src = src;
	}

	private String getNewFilePath(String filePath) {
		// Edit src's file path to know where to transfer to new destination
		int srcLen = this._src.length();
		String relativePath = filePath.substring(srcLen);
		return this._dest + relativePath;
	}

	public void transferFiles(ArrayList<Path> files) {
		for (Path f : files) {
			String newLocation = getNewFilePath(f.toString());
			try {
				File newPath = new File(newLocation);
				FileUtils.copyFile(f.toFile(), newPath);
				System.out.println("Transfered " + f.getFileName() + " to " + newLocation);
			} catch (Exception e) {
				System.out.printf("Error transferring file %s to %s\n", f.toString(), newLocation);
			}
		}
	}
}
