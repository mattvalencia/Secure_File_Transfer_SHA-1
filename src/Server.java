/*
 * 
 */

import java.io.*;
import java.net.*;
import java.nio.file.Files;

public class Server  extends Thread {
	
	public static void main(String[] args) {
		ServerSocket serversocket;
		final int myPort = 14014;
		Server_Thread svr;
		
		try {
			serversocket = new ServerSocket(myPort);
			
			//Start while(true) loop here for unlimited clients
			System.out.println("Waiting for client..."); //DEBUG ONLY
			Socket s = serversocket.accept();
			svr = new Server_Thread(s);
			svr.start();
			System.out.println("Got a client"); //DEBUG ONLY
			//End while(true) loop
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

class Server_Thread extends Thread {
	Socket sock = null;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	File f;
	String fileName;
	byte[] key = {	(byte) Integer.parseInt("11001101", 2), // 1st 8 bits of key
					(byte) Integer.parseInt("01111001", 2), // 2nd 8 bits of key
					(byte) Integer.parseInt("00001010", 2), // 3rd
					(byte) Integer.parseInt("01000100", 2), // 4th
					(byte) Integer.parseInt("10001110", 2), // 5th
					(byte) Integer.parseInt("10001111", 2), // 6th
					(byte) Integer.parseInt("11110010", 2), // 7th
					(byte) Integer.parseInt("01101101", 2), // 8th
					(byte) Integer.parseInt("01010010", 2), // 9th
					(byte) Integer.parseInt("00001011", 2), // 10th
					(byte) Integer.parseInt("11110011", 2), // 11th
					(byte) Integer.parseInt("00111111", 2), // 12th
					(byte) Integer.parseInt("11001111", 2), // 13th
					(byte) Integer.parseInt("01000001", 2), // 14th
					(byte) Integer.parseInt("01111000", 2), // 15th
					(byte) Integer.parseInt("10001010", 2), // 16th
			};
	byte[] tmp, FileBytes;

	public Server_Thread(Socket s) {
		sock = s;
	}
	
	public void run() {
		try {
			oos = new ObjectOutputStream(sock.getOutputStream());
			ois = new ObjectInputStream(sock.getInputStream());
			
			if((ois.readByte() ^ key[0]) == 1) {	//Client wants to send us a file
				fileName = ois.readUTF();			//Get File Name
				f = new File(fileName);
				if(f.exists())						//Delete it if it exists
					f.delete();
				
				tmp = (byte[]) ois.readObject();	//Get the files Encrypted bytes
				FileBytes = new byte[tmp.length];
				
				for(int i = 0; i < tmp.length; i++) {
					FileBytes[i] = (byte) (tmp[i] ^ key[i % 16]);
				}
				
				Files.write(f.toPath(), FileBytes);

			} else {	//Client wants a file
				fileName = ois.readUTF();
				f = new File(fileName);
				FileBytes = Files.readAllBytes(f.toPath());
				tmp = new byte[FileBytes.length];
				for(int i = 0; i < FileBytes.length; i++) {
					tmp[i] = (byte) (FileBytes[i] ^ key[i % 16]);
				}
				oos.writeObject(tmp);
				oos.flush();

			}
			/*
			tmp = (byte[]) ois.readObject();
			
			String tmpString = "";
			
			for(int i = 0; i < tmp.length; i++) {
				tmpString = tmpString + ((char) (tmp[i] ^ key[i % 16]));
			}
			
			System.out.println("Got String : " + tmpString);
			*/
			
			oos.close();
			ois.close();
			sock.close();
			System.out.println("Done");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
