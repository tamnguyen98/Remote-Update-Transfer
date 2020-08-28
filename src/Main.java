import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import org.apache.commons.io.FilenameUtils;

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
		input.nextLine();

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
			actAsClient(option);
			break;
		case 2: // sending
			actAsServer();
			break;
		default:
			System.err.println("Input not recognized!");
			remoteTransfer(args);
			return;
		}
	}

	private static void actAsClient(int option) {
		System.out.println("Enter the <IP:Port> of the server you're receiving the updates from: ");
		input.nextLine(); // to start capturing input
		String[] addy = input.nextLine().replaceAll("[<>]*", "").split(":"); // Remove <> brackets encase the user
		// provides them and split it
		Client serverConnection = new Client(addy[0], Integer.parseInt(addy[1]));
//		Client serverConnection = new Client("localhost", 5123); // for testing
		DataOutputStream cOut = serverConnection.sender(); // connection output

		System.out.print("Enter the directory where you want the updates to be downloaded to: ");
		String workingDir = FilenameUtils.normalize(input.nextLine());
//		String workingDir = "F:\\GitHub\\Remote-Update-Transfer\\bin\\tmp"; // for testing
		File dir = new File(workingDir);
		while (!dir.canWrite()) {
			System.out.print(GlobalTools.ANSI_RED + "Sorry, you don't have write permission to this directory. "
					+ GlobalTools.ANSI_RESET
					+ "\nEither enter a new directory or gain write access to this directory: ");
			workingDir = FilenameUtils.normalize(input.nextLine());
			dir = new File(workingDir);
		}
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
		ArrayList<String> incomingFiles = serverConnection.recieveUpdateStatus(workingDir);
		// Confirm whether we have the right file count
		try {
			if (serverConnection.receiveAbort()) {
				// Fail in communication
				System.out.printf(
						"%sRecieve abort from server (possible due to missing files).%s\nWould you like to retry (1) or no(0)? ",
						GlobalTools.ANSI_RED, GlobalTools.ANSI_RESET);
				option = input.nextInt();
				input.nextLine(); // To clear the input
				if (option == 1) { // Check to see if user want to retry
					try {
						cOut.writeInt(option);
						cOut.flush();
						if (!serverConnection.receiveAbort()) // If we got the OK from server
							serverConnection.startDownload(workingDir, incomingFiles);
						else
							System.out.printf("%sRetry failed again. Closing connection...%s\n", GlobalTools.ANSI_RED,
									GlobalTools.ANSI_RESET);
					} catch (IOException e) {
						System.err.printf("Error retrying to recieve update status: %s%s\n", GlobalTools.ANSI_RED,
								e.toString(), GlobalTools.ANSI_RESET);
					}
				}

			} else {
				System.out.printf("%sRecieve OK from server to start file transfer!%s\n", GlobalTools.ANSI_GREEN,
						GlobalTools.ANSI_RESET);
				serverConnection.startDownload(workingDir, incomingFiles);
			}
		} catch (IOException e) {
			System.err.println("Huh?:" + e.toString());
		}
		serverConnection.closeSocket();
	}

	private static void actAsServer() {
		System.out.print("Enter a open port to work on (enter 0 for default): ");
		int port = input.nextInt();
		input.nextLine(); // clear the input
//		int port = 5123; // Debug
		Server clientConnection = new Server((port == 0) ? 5123 : port);

		// Get a copy of the client's directory to compare to ours
		HashMap<String, Long> clientDirInfo = clientConnection.recieveClientDirectoryContent();
		// Take in the directory we want to compare with
		System.out.println("Enter the path of the directory you want to compare with"
				+ " (directory that contains the update the client is looking for)." + GlobalTools.ANSI_CYAN);
		String src = FilenameUtils.normalize(input.nextLine());
//		String src = FilenameUtils.normalize("F:\\Learning Videos\\ASPNET\\Building a RESTful API with ASP.NET Core 3\\03. Structuring and Implementing the Outer Facing Contract"); // Debug
		ItemTracker it = new ItemTracker(src, null);
		try {
			// Time to compare the two directory content
			System.out.println(GlobalTools.ANSI_RESET + "Checking directory for update.");
			boolean hasItemToTransfer = it.remoteDetectDiffernce(clientDirInfo);
			System.out.printf("%s", hasItemToTransfer ? "Preparing files to upload..." : "Nothing to give!");
			if (hasItemToTransfer) {
				int verifiedStatus = clientConnection.sendAvailableFileNamesToTransfer(src, it.getFilesToTransfer());
				if (verifiedStatus == 0)
					clientConnection.startUploading(it.getFilesToTransfer());
				else if (verifiedStatus == -1) {
					/// If client data test sent failed
					if (clientConnection.expectFromClient(1)) {
						System.out.println("Trying again... ");
						verifiedStatus = clientConnection.sendAvailableFileNamesToTransfer(src,
								it.getFilesToTransfer());
						if (verifiedStatus == 1)
							clientConnection.startUploading(it.getFilesToTransfer());
					} else {
						System.out.println("Aborting connection (client does not want to retry)");
					}
				}
				clientConnection.closeSocket();
			} else {
				System.out.println("Client has latest files.");
			}
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		clientConnection.closeSocket();
	}

	private static void localTransfer(String[] args) {
		String originalPath = "";
		String transferToPath = "";

		boolean overridePermission = (args.length > 0 && args[0].equals("-y"));
		System.out.printf("Override %s\n", GlobalTools.boolToStatus(overridePermission));

		System.out.printf("Please enter the path of the folder which you want to transfer (contains the update): \n%s",
				GlobalTools.ANSI_CYAN);
		originalPath = input.nextLine();

		System.out.printf("%sPlease enter the path of the transfer destination (outdated directory): \n%s",
				GlobalTools.ANSI_RESET, GlobalTools.ANSI_CYAN);
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
					System.out.printf("%sProgram completed somewhat successfully (not really).%s\n",
							GlobalTools.ANSI_RED, GlobalTools.ANSI_RESET);
				} else
					System.out.printf("%sProgram completed successfully.\n%s", GlobalTools.ANSI_GREEN,
							GlobalTools.ANSI_RESET);
			}
		} else {
			System.out.println("No difference in the two directories.");
		}

		input.close();
	}

}
