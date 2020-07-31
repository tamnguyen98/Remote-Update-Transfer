import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

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

	public DataOutputStream sender() {
		return out;
	}

	public DataInputStream receiver() {
		return input;
	}

	public void sendDirectoryInfo(String dir, HashMap<String, Long> dirContent) {
		String data = dir + ";";
		if (dirContent.isEmpty()) {
			data += "E;";// E for empty
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
		System.out.print("Verifying difference count with server... ");
		int code = input.readInt();
		if (code == -1)
			return true;
		return false;
	}

	// Check to see if there is any new files we dont have
	public ArrayList<String> recieveUpdateStatus(String toDir) {
		ArrayList<String> incomingFiles = new ArrayList<String>();
		try {

			String newFilesCollections = "";
			// Only reason I'm using a buffer instead of readUTF is encase there is a large
			// quantity of files difference
			byte[] buf = new byte[4092];
			String bToString = "";
			int size = input.readInt();
			int n = 0; // read count
			while (size > 0 && (n = input.read(buf, 0, Math.min(buf.length, size))) != -1) {
				size -= n;
				bToString += new String(buf, 0, n);
			}
			newFilesCollections += bToString.substring(bToString.indexOf(':') + 1);

			System.out.println("Initializing a temp file of all files to be downloaded to " + toDir + "\\.incoming");
			FileOutputStream tmpListFile = new FileOutputStream(toDir + "\\.incoming");
			int filesCount = 0;
			for (String s : newFilesCollections.split(";")) {
				filesCount++;
				incomingFiles.add(s);
				tmpListFile.write((s + "\n").getBytes());
			}
			System.out.printf("There is %d files to be transfered. Check the .incoming file for the list\n",
					filesCount);
			tmpListFile.close();
			out.writeInt(filesCount); // let the server know how much new files (names) it got
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e.toString());
			e.printStackTrace();
		}
		return incomingFiles;
	}

	public void startDownload(String toDestination, ArrayList<String> files) {
		try {
			out.writeUTF("S"); // Send a ready signal
			out.flush();
			Iterator<String> listIterator = files.iterator();
			byte[] buf = new byte[4092]; // buffer read from socket
			int n = 0; // how much we've read
			while (listIterator.hasNext()) {
				String fileName = listIterator.next();
				System.out.printf("Downloading %s ", fileName);
				long fileSize = input.readLong();
				System.out.print("with byte size " + fileSize + "... ");
				try {
					fileName = FilenameUtils.normalize(toDestination + "\\" + fileName);
					String tmpName = fileName + ".tmp";
					File newFile = new File(tmpName);
					if (!newFile.exists()) {
						newFile.getParentFile().mkdirs();
					}

					FileOutputStream fos = new FileOutputStream(tmpName);
					while (fileSize > 0 && (n = input.read(buf, 0, (int) Math.min(buf.length, fileSize))) != -1) {
						fos.write(buf, 0, n);
						fileSize -= n;
					}
					fos.close();

					File destinationFile = new File(fileName);
					boolean renameStatus = newFile.renameTo(destinationFile);// rename the file without the tmp
					if (renameStatus == false) { // Fail to rename file
						if (destinationFile.exists() && newFile.exists()) {
							destinationFile.delete();
							newFile.renameTo(destinationFile);
						}
					}
					System.out.println("Done!");
				} catch (IOException e) {
					System.err.printf("Error downloading %s: %s\n", fileName, e.toString());
					File destinationFile = new File(fileName);
					if (destinationFile.exists())
						destinationFile.delete();
				}
			}
			System.out.println("All Download Complete");
		} catch (IOException e) {
			System.err.println(e.toString());
		}
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
