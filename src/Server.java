
// Server has the update files to give to client
import java.net.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.io.FilenameUtils;

import java.io.*;

public class Server {

	// initialize socket and input stream
	private Socket socket = null;
	private ServerSocket server = null;
	private DataInputStream input = null;
	private DataOutputStream out = null;

	// constructor with port
	public Server(int port) {
		// starts server and waits for a connection
		try {
			server = new ServerSocket(port);
			System.out.println("Server started on port " + server.getLocalPort());

			System.out.println("Waiting for a client ...");

			socket = server.accept();
			System.out.printf("%sClient accepted%s\n", GlobalTools.ANSI_GREEN, GlobalTools.ANSI_RESET);
			input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

		} catch (IOException i) {
			System.out.println(i);
		}
	}

	public HashMap<String, Long> recieveClientDirectoryContent() {
		try {
			HashMap<String, Long> clientDir = new HashMap<String, Long>();
			String data = input.readUTF();
			if (!data.contains(";E;")) {
//				System.out.println(data.replace(";", "\n"));
				int i = 0;
				for (String s : data.split(";")) {
					if (i == 0) {
						i++;
					} else {
						String[] kv = s.split(":"); // Split for key-value
						clientDir.put(kv[0], Long.parseLong(kv[1]));
					}
				}
			}

			out.write("OK".getBytes());
			out.flush();
			System.out.printf("Client would like to recieve files for %s%s%s\n", GlobalTools.ANSI_CYAN,
					data.substring(0, data.indexOf(";")), GlobalTools.ANSI_RESET);
			return clientDir;

		} catch (IOException e) {
			System.err.println(e.toString());
		}
		return null;
	}

	public int sendAvailableFileNamesToTransfer(String dir, ArrayList<Path> files) {
		// Grab all the files we (the server) have that the client doesn't
		// and convert it to a string with ';' as delimiter
		String fileNamesCollections = ":"; // Encase the client gets random characters
		int count = 0;
		for (Path f : files) {
			count++;
			fileNamesCollections += FilenameUtils.normalize(f.toString().substring(dir.length() + 1)) + ";";
		}
		// Add ok to let the client know that's all of the file difference (really not
		// needed)

		int confirmedFileCount;
		try {
			out.writeInt(fileNamesCollections.length() + 2);
			out.writeUTF(fileNamesCollections);
			out.flush();
			confirmedFileCount = input.readInt();
			System.out.printf("%sREADY\nRecieved verfication from client.%s\n", GlobalTools.ANSI_GREEN,
					GlobalTools.ANSI_RESET);
			if (confirmedFileCount != count) {
				System.err.printf("%sUH OH, Client only thinks we're transfering %d when it's actually %d\n",
						GlobalTools.ANSI_RED, confirmedFileCount, count);
				System.out.printf("Error occured before files transfer, telling client to abort%s\n",
						GlobalTools.ANSI_RESET);
				out.writeInt(-1);
				out.flush();
				return -1;
			} else {
				System.out.printf("Client's value matches. %sClear to proceed.%s\n", GlobalTools.ANSI_GREEN, GlobalTools.ANSI_RESET);
				out.writeInt(confirmedFileCount);
				out.flush();
				return 0;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e.toString());
		}
		return 1;
	}

	public void startUploading(ArrayList<Path> files) {
		try {
			if (input.readUTF().equals("S")) { // Wait until client gets ready
				System.out.printf("Client's ready. %sStarting transfer...%s", GlobalTools.ANSI_GREEN, GlobalTools.ANSI_RESET);
				Iterator<Path> listIterator = files.iterator();
				byte[] buf = new byte[4092]; // buffer read from socket
				int n = 0; // how much we've read

				while (listIterator.hasNext()) {
					String fName = listIterator.next().toString();
					System.out.printf("Uploading %s%s%s", GlobalTools.ANSI_CYAN, fName, GlobalTools.ANSI_RESET);
					File f = new File(fName);
					long fsize = f.length();
					out.writeLong(fsize);
					out.flush();
					input.readInt();
					System.out.printf(" with size of %s%s%s...", GlobalTools.ANSI_BLUE, GlobalTools.byteConversionSI(fsize), GlobalTools.ANSI_RESET);
					FileInputStream fis = new FileInputStream(f);
					while ((n = fis.read(buf)) != -1) {
						out.write(buf, 0, n);
						out.flush();

					}

					System.out.println(GlobalTools.ANSI_GREEN + "DONE" + GlobalTools.ANSI_RESET);
					fis.close();
				}
			}
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}

	public boolean expectFromClient(int value) {
		try {
			int recieve = input.readInt();
			if (recieve == value)
				return true;
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		return false;

	}

	public void closeSocket() {
		try {
			if (socket != null)
				socket.close();
			if (server != null)
				server.close();
			if (server != null)
				server.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}

}
