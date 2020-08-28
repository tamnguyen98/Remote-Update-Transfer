import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

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

	public ArrayList<Path> transferFiles(ArrayList<Path> files) {
		int i = 1;
		ArrayList<Path> failedTransfer = new ArrayList<Path>();
		
		for (Path f : files) {
			String newLocation = getNewFilePath(f.toString());
			
			System.out.printf("(%d/%d) Transfering %s to %s... ", i++, files.size(), f.getFileName(), newLocation);
			
			try {
				Path newPath = Paths.get(newLocation);
				
				if (!newPath.toFile().exists()) {
					newPath.toFile().getParentFile().mkdirs();
					newPath.toFile().createNewFile();
				}

				if (newPath.toFile().canWrite()) {
					c2cTransfer(f, newPath);
					
					System.out.printf("Complete!\n");
				} else {
					System.err.printf("Failed!\nYou do not have permission to write to: %s\n", newLocation);
					
					failedTransfer.add(f);
				}
			} catch (Exception e) {
				System.err.printf("Failed\n");
				System.err.println("\tError msg: " + e.getMessage());
				System.err.println("\t" + e.toString());
				
				failedTransfer.add(f);
			}
		}
		return (failedTransfer.size() == 0) ? null : failedTransfer;
	}

	// Only Call this if the top function fails
	public ArrayList<Path> overrideCopy(ArrayList<Path> files) {
		int i = 1;
		ArrayList<Path> failedTransfer = new ArrayList<Path>();
		
		for (Path f : files) {
			String newLocation = getNewFilePath(f.toString());
			
			System.out.printf("(%d/%d) Transfering (%s) %s to %s%s%s... ", i++, files.size(),
					GlobalTools.byteConversionSI(f.toFile().length()), f.getFileName(), 
					GlobalTools.ANSI_CYAN, newLocation, GlobalTools.ANSI_RESET);
			
			try {
				Path newPath = Paths.get(newLocation);
				generateDestination(newPath);

				newPath.toFile().delete();
				generateDestination(newPath);
				c2cTransfer(f, newPath);
				
				System.out.printf("Override Complete!\n");
			} catch (Exception e) {
				System.err.printf("%sOverride Failed\n", GlobalTools.ANSI_RED);
				System.err.println("\tError msg: " + e.getMessage());
				System.err.println("\t" + e.toString() + GlobalTools.ANSI_RESET);
				
				failedTransfer.add(f);
			}
		}
		return (failedTransfer.size() == 0) ? null : failedTransfer;
	}

	public void generateDestination(Path newPath) throws IOException {
		if (!newPath.toFile().exists()) {
			newPath.toFile().getParentFile().mkdirs();
			newPath.toFile().createNewFile();
		}
	}

	// https://howtodoinjava.com/java7/nio/java-nio-2-0-how-to-transfer-copy-data-between-channels/
	private void c2cTransfer(Path source, Path dest) throws Exception {
		// Get channel for output file
		FileOutputStream fos = new FileOutputStream(dest.toFile());
		WritableByteChannel targetChannel = fos.getChannel();

		// Get channel for input files
		FileInputStream fis = new FileInputStream(source.toString());
		FileChannel inputChannel = fis.getChannel();

		System.out.print("Closing all IO readers...\t");
		// Transfer data from input channel to output channel
		inputChannel.transferTo(0, inputChannel.size(), targetChannel);

		// close the input channel
		inputChannel.close();
		fis.close();

		// finally close the target channel
		targetChannel.close();
		fos.close();
		System.out.println("Done!");
	}
}
