import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @author Offline
 *
 */
public class Main {
	static Scanner input = new Scanner(System.in);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.print("Local transfer (1) or Remote Transfer (2)?: ");
		int option = input.nextInt();

		switch (option) {
		case 1:
			localTransfer(args);
			break;
		case 2:
			remoteTransfer(args);
			break;
		default:
			System.err.println("Input not recognized!");
			break;
		}

	}

	private static void remoteTransfer(String[] args) {
		System.out.print("Are we recieving the updates (1) or sending (2)? ");
		int option = input.nextInt();

		switch (option) {
		case 1: // Receiving
			System.out.println("Enter the <IP:Port> of the server you're receiving the updates from: ");
			input.nextLine(); // to start capturing input
//			String[] addy = input.nextLine().replaceAll("[<>]*", "").split(":"); // Remove <> brackets encase the user
			// provides them and split it
//			Client serverConnection = new Client(addy[0], Integer.parseInt(addy[1]));
			Client serverConnection = new Client("localhost", 5123); // for testing
			System.out.print("Enter the directory where you want the updates to be downloaded to: ");
//			String workingDir = input.nextLine();
			String workingDir = "F:\\GitHub\\Remote-Update-Transfer\\bin"; // for testing

			// Traverse through the directory and collect all the
			// files' name and last modified date
			Traverser dest = new Traverser(null, workingDir);
			try {
				System.out.println("Collecting item in source directory...");
				Files.walkFileTree(Paths.get(workingDir), dest);
			} catch (IOException e) {
				System.err.println(e.toString());
			}

			// Get the collection as a HashMap since it was working with HM
			HashMap<String, Long> dirContent = dest.getHashMap();
			serverConnection.sendDirectoryInfo(workingDir, dirContent); // send it

			// Check if there are any new files from the server;
			boolean hasUpdate = serverConnection.recieveUpdateStatus(workingDir);
			// Confirm whether we have the right file count
			try {
				if (serverConnection.receiveAbort()) {
					System.out.println("Recieve abort from server (possible do to missing files)");
				} else {
					System.out.println("Recieve OK from server to start file transfer!");
				}
			} catch (IOException e) {
				System.err.println(e.toString());
			}
			serverConnection.closeSocket();
			break;
		case 2: // sending
			System.out.print("Enter a open port to work on: ");
			Server clientConnection = new Server(5123);

			// Get a copy of the client's directory to compare to ours
			HashMap<String, Long> clientDirInfo = clientConnection.recieveClientDirectoryContent();
			// Take in the directory we want to compare with
			String src = input.nextLine();
			ItemTracker it = new ItemTracker(src, null);
			try {
				// Time to compare the two directory content
				boolean hasItemToTransfer = it.remoteDetectDiffernce(clientDirInfo);
				System.out.printf("We %s have stuff to give\n", hasItemToTransfer ? "do" : "don't");
				if (hasItemToTransfer) {
					clientConnection.sendAvailableFilesToTransfer(it.getFilesToTransfer());
				} else {
					System.out.println("Client has latest files.");
				}
			} catch (IOException e) {
				System.err.println(e.toString());
			}
			clientConnection.closeSocket();
			break;
		default:
			System.err.println("Input not recognized!");
			remoteTransfer(args);
			return;
		}
	}

	private static void localTransfer(String[] args) {
		String originalPath = "";
		String transferToPath = "";

		boolean overridePermission = (args.length > 0 && args[0].equals("-y"));
		System.out.println("Override " + overridePermission);

		System.out.println("Please enter the path of the folder which you want to transfer (contains the update): ");
		originalPath = input.nextLine();

		System.out.println("Please enter the path of the transfer destination (outdated directory): ");
		transferToPath = input.nextLine();

		ItemTracker it = new ItemTracker(originalPath, transferToPath);
		try {
			it.localDetectDifference();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}

		if (!it.getFilesToTransfer().isEmpty()) {
			TransferManager tm = new TransferManager(originalPath, transferToPath);
			ArrayList<Path> failedTransfers = tm.transferFiles(it.getFilesToTransfer());

			if (!failedTransfers.isEmpty()) {
				System.out.println("Retrying failed transfer...");
				ArrayList<Path> retries = null;

				if (overridePermission)
					retries = tm.overrideCopy(failedTransfers);
				else
					retries = tm.transferFiles(failedTransfers);

				if (retries != null && failedTransfers.size() == retries.size()) {
					System.out.println("Retries failed to transfer the following files:");
					it.printNewFiles(retries);
					println("Program completed somewhat successfully (not really).");
				} else
					println("Program completed successfully.");
			}
		} else {
			System.out.println("No difference in the two directories.");
		}

		input.close();
	}

	static void println(String s) {
		System.out.println(s);
	}

}
