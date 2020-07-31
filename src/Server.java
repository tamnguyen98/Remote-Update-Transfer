
// Server has the update files to give to client
import java.net.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
			System.out.println("Client accepted");
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
				System.out.println(data.replace(";", "\n"));
				int i = 0;
				for (String s : data.split(";")) {
					if (i == 0) {
						System.out.println("Client wants to recieve update for " + s);
						i++;
					} else {
						String[] kv = s.split(":"); // Split for key-value
						clientDir.put(kv[0], Long.parseLong(kv[1]));
					}
				}
			}

			out.write("OK".getBytes());
			out.flush();
			System.out.println("Client would like to recieve files for " + data.substring(0, data.indexOf(";E")));
			return clientDir;

		} catch (IOException e) {
			System.err.println(e.toString());
		}
		return null;
	}

	public int sendAvailableFileNamesToTransfer(ArrayList<Path> files) {
		// Grab all the files we (the server) have that the client doesn't
		// and convert it to a string with ';' as delimiter
		String fileNamesCollections = ":"; // Encase the client gets random characters
		int count = 0;
		for (Path f : files) {
			count++;
			fileNamesCollections += f.getFileName() + ";";
		}
		// Add ok to let the client know that's all of the file difference (really not
		// needed)
		fileNamesCollections += "OK";

		int confirmedFileCount;
		try {
			out.writeUTF(fileNamesCollections);
			out.flush();
			confirmedFileCount = input.readInt();
			System.out.println("\rREADY\nVerifying with client... " + confirmedFileCount);
			if (confirmedFileCount != count) {
				System.err.printf("UH OH, Client only thinks we're transfering %d when it's actually %d\n",
						confirmedFileCount, count);
				System.out.printf("Error occured before files transfer, telling client to abort\n");
				out.writeInt(-1);
				out.flush();
				return -1;
			} else {
				System.out.println("Client's value matches. Clear to proceed.");
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
				System.out.println("Client's ready, starting transfer...");
				Iterator<Path> listIterator = files.iterator();
				byte[] buf = new byte[4092]; // buffer read from socket
				int n = 0; // how much we've read

				while (listIterator.hasNext()) {
					String fName = listIterator.next().toString();
					System.out.print("Uploading " + fName);
					File f = new File(fName);
					out.writeLong(f.length()); // send the filesize first
					out.flush();
					System.out.print(" with byte size of " + f.length() + "... ");
					FileInputStream fis = new FileInputStream(f);
					while ((n = fis.read(buf)) != -1) {
						out.write(buf, 0, n);
						out.flush();

					}
					System.out.println("DONE");
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
