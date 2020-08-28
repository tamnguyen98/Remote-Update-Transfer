import java.net.*;
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
			System.out.printf("%sConnected%s\n", GlobalTools.ANSI_GREEN, GlobalTools.ANSI_RESET);
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
			System.out.printf("Server response to directory info: %s%s%s\n", GlobalTools.ANSI_GREEN, new String(buf),
					GlobalTools.ANSI_RESET);
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
			String filelist = FilenameUtils.normalize(toDir + "\\.newfiles");
			// Only reason I'm using a buffer instead of readUTF is encase there is a large
			// quantity of files difference
			byte[] buf = new byte[4092];
			String bToString = "";
			int size = input.readInt();
			int n = 0; // read count

			while (size > 0 && (n = input.read(buf, 0, Math.min(buf.length, size))) > -1) {
				size -= n;
				bToString += new String(buf, 0, n);
			}
			newFilesCollections += bToString.substring(bToString.indexOf(':') + 1);

			System.out.printf("Initializing a temp file of all files to be downloaded to %s%s%s\n",
					GlobalTools.ANSI_CYAN, filelist, GlobalTools.ANSI_RESET);
			FileOutputStream tmpListFile = new FileOutputStream(filelist);
			int filesCount = 0;
			for (String s : newFilesCollections.split(";")) {
				filesCount++;
				incomingFiles.add(s);
				tmpListFile.write((s + "\n").getBytes());
			}
			System.out.printf("There is %d files to be transfered. Check the %s.incoming%s file for the list\n",
					filesCount, GlobalTools.ANSI_CYAN, GlobalTools.ANSI_RESET);
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
				long fileSize = input.readLong();
				out.writeInt(1);
				out.flush();
				System.out.printf("Downloading %s(%s)%s %s ", GlobalTools.ANSI_BLUE,
						GlobalTools.byteConversionSI(fileSize), GlobalTools.ANSI_RESET, fileName);
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
					System.out.println(GlobalTools.ANSI_GREEN + "Done!" + GlobalTools.ANSI_RESET);
				} catch (IOException e) {
					System.err.printf("%sError downloading %s: %s%s\n", GlobalTools.ANSI_RED, fileName, e.toString(),
							GlobalTools.ANSI_RESET);
					File destinationFile = new File(fileName);
					if (destinationFile.exists())
						destinationFile.delete();
				}
			}
			System.out.printf("%sAll Download Complete%s\n", GlobalTools.ANSI_GREEN, GlobalTools.ANSI_RESET);
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
