import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Offline
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String originalPath = "";
		String transferToPath = "";
		Scanner input = new Scanner(System.in);

		System.out.println("Please enter the path of the folder which you want to transfer (contains the update): ");
		originalPath = input.nextLine();

		System.out.println("Please enter the path of the transfer destination (outdated directory): ");
		transferToPath = input.nextLine();

		ItemTracker it = new ItemTracker(originalPath, transferToPath);
		try {
			it.detectDifference();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.toString());
		}

//		it.printNewFiles();
		if (!it.getFilesToTransfer().isEmpty()) {
			TransferManager tm = new TransferManager(originalPath, transferToPath);
			ArrayList<Path> failedTransfers = tm.transferFiles(it.getFilesToTransfer());
			System.out.println("Retrying failed transfer...");
			ArrayList<Path> retries = tm.transferFiles(failedTransfers);
			if (failedTransfers.size() == retries.size()) {
				System.out.println("Retries failed to transfer the following files:");
				it.printNewFiles(retries);
			}
			println("Progress completed.");
		} else {
			System.out.println("No difference in the two directories.");
		}

		input.close();

	}

	static void println(String s) {
		System.out.println(s);
	}

}
