
// Server has the update files to give to client
import java.net.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
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
			if (!data.equals("empty map")) {
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
			System.out.println("Sent OK");
			out.flush();
			return clientDir;

		} catch (IOException e) {
			System.err.println(e.toString());
		}
		return null;
	}

	public void sendAvailableFilesToTransfer(ArrayList<Path> files) {
		// Grab all the files we (the server) have that the client doesn't
		// and convert it to a string with ';' as delimiter
		String fileNamesCollections = "";
		int count = 0;
		for (Path f : files) {
			count++;
			System.out.println(f.getFileName());
			fileNamesCollections += f.getFileName() + ";";
		}
		// Add ok to let the client know that's all of the file difference (really not
		// needed)
		fileNamesCollections += "OK";

		int confirmedFileCount;
		try {
			out.writeUTF(fileNamesCollections);
			out.flush();
			System.out.println("Sent to client: " + fileNamesCollections);
			confirmedFileCount = input.readInt();
			System.out.println("Recieve from Client: " + confirmedFileCount);
			if (confirmedFileCount != count) {
				System.err.printf("UH OH, Client only thinks we're transfering %d when it's actually %d\n",
						confirmedFileCount, count);
				System.out.printf("Error occured before files transfer, telling client to abort\n");
				out.writeInt(-1);
				out.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e.toString());
		}
	}

	public void sendStringToClient(String data) {
		if (data == null || data.isEmpty())
			return;
		try {
			out.write(data.getBytes());
		} catch (IOException e) {
			System.err.println(e.toString());
		}
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
