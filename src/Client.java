import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

// Client wants to get updated files from the server

public class Client {

	// initialize socket and input output streams
	private Socket socket = null;
	private DataInputStream input = null;
	private DataOutputStream out = null;

	// constructor to put ip address and port
	public Client(String address, int port) {
		// establish a connection
		try {
			socket = new Socket(address, port);
			System.out.println("Connected");
			input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

		} catch (UnknownHostException u) {
			System.out.println(u);
		} catch (IOException i) {
			System.out.println(i);
		}
	}

	public void sendDirectoryInfo(String dir, HashMap<String, Long> dirContent) {
		String data = dir + ";";
		if (dirContent.isEmpty()) {
			System.err.println("ITS EMPTY");
			data = "empty map";
		} else {
			for (Map.Entry<String, Long> element : dirContent.entrySet()) {
				String tmp = element.getKey() + ":" + element.getValue() + ";";
				data += tmp;
				System.out.println(tmp);
			}
		}

		try {
			out.writeUTF(data);
			out.flush();
			byte[] buf = new byte[3]; // should be enough to receive OK
			input.read(buf);
			System.out.println("Server response to directory info: " + new String(buf));
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}

	public boolean receiveAbort() throws IOException {
		int code = input.readInt();
		if (code == -1)
			return true;
		return false;
	}

	// Check to see if there is any new files we dont have
	public boolean recieveUpdateStatus(String toDir) {
		try {

			String newFilesCollections = "";
			byte[] buf = new byte[4092];
			input.read(buf, 0, 4092);
			String bToS = new String(buf);
			while (!bToS.contains(";OK")) {
				newFilesCollections += bToS;
				input.read(buf, 0, 4092);
				bToS = new String(buf);
			}
			newFilesCollections += bToS.substring(0, bToS.indexOf(";OK") + 3);

			System.out.println("From server: " + newFilesCollections);
			System.out.println("Initializing a temp file to " + toDir);
			FileOutputStream tmpListFile = new FileOutputStream(toDir + "\\.incoming");
			int filesCount = 0;
			boolean recievedOK = false;
			for (String s : newFilesCollections.split(";")) {
				if (!s.equals("OK")) {
					filesCount++;
					tmpListFile.write((s + "\n").getBytes());
				} else {
					recievedOK = true;
				}
			}

			tmpListFile.close();
			out.writeInt(filesCount); // let the server know how much new files (names) it got
			out.flush();
			if (recievedOK || filesCount > 0)
				return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public void closeSocket() {
		try {
			if (socket != null)
				socket.close();
			if (input != null)
				input.close();
			if (out != null)
				out.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}

}
