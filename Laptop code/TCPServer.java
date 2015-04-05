import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
//TCPServer class
//
//******************ERRORS********************************
//Exceptions may be thrown by ConnectionNew Class

/**
 *Implements an TCP Server Client file transfer application.
 * @author Haridarshan
 */
public class TCPServer { 
	public static void main (String args[]) 
	{ 
		try{ 
			int serverPort = 6111; 
			ServerSocket listenSocket = new ServerSocket(serverPort); 
			System.out.println("server start listening... ... ...");
			while(true) { 
				Socket clientSocket = listenSocket.accept();
				Connectionnew c = new Connectionnew(clientSocket); 
			} 
		} 
		catch(IOException e) {
			System.out.println("Listen :"+e.getMessage());} 
	}
}

class Connectionnew extends Thread { 
	DataInputStream input; 
	DataOutputStream output; 
	Socket clientSocket; 

	/**
	 * Constructor:Creates a input and output data stream for the Server socket
	 * 				and starts the server for transaction of the data
	 */
	public Connectionnew (Socket aClientSocket) { 
		try { 
			clientSocket = aClientSocket; 
			input = new DataInputStream( clientSocket.getInputStream()); 
			output =new DataOutputStream( clientSocket.getOutputStream()); 
			this.start(); 
		} 
		catch(IOException e) {
			System.out.println("Connection:"+e.getMessage());
		} 
	} 
	/**
	 * Run Method:S1:Waits for the client to make a connection and receives the Type of the file that
	 * 				 the Client is intending to send
	 * 			  S2:Receives the Time stamp of the file that is being sent from the client
	 * 			  S3:If the time stamp of the client file is less than that of the server file,
	 * 				 The server send the file to the client.If that's not the case the client sends the file.
	 */
	public void run() { 
		try { 
			
			String strFileType=input.readUTF();
			//System.out.println(strFileType);
			output.writeUTF("FileType Received");
			String name="b."+strFileType;
			String fileLen=input.readUTF();
			int clientFilelength=Integer.parseInt(fileLen);
			File f=new File(name);
			long serverTimeStamp=f.lastModified();
			//System.out.println(f);
			int serverFileLength=0;
			byte[] myArrayImage=null;
			if(f.isFile()==true){
				//System.out.println("File present");
				FileInputStream fis = new FileInputStream(f);
				myArrayImage = new byte[(int) f.length()];
				int len = 0 ;
				int total = 0;
				for (int i = 0; i < myArrayImage.length; i++) {
					myArrayImage[i]=(byte) fis.read();
				}
				fis.close();
				//System.out.println(f.lastModified());
				serverFileLength=myArrayImage.length;	
				
			}
			else{
				f.createNewFile();
			}
			//System.out.println("Before check:"+f.lastModified());
			output.writeUTF("FileLength Received");
			/*System.out.println("serverfilelength:"+serverFileLength);
			System.out.println("clientfilelength:"+clientFilelength);
			*/
			String clientTimeStampStr=input.readUTF();
			long clientTimeStamp=Long.parseLong(clientTimeStampStr);
			//System.out.println("Client Time stamp:"+clientTimeStamp);
			//System.out.println("Server Time stamp:"+serverTimeStamp);
			
			if(serverTimeStamp>clientTimeStamp){
				//server sends the file
				//setclient timestamp to servertimestamp
				System.out.println("Server sends file");
				output.writeUTF("Server Sending File");
				output.writeUTF(String.valueOf(serverTimeStamp));
				output.writeUTF(String.valueOf(serverFileLength));
				output.write(myArrayImage);
			}
			else if(serverTimeStamp<clientTimeStamp){
				//server receives the file
				System.out.println("Server RXS File");
				FileOutputStream stream=new FileOutputStream(f);
				output.writeUTF("Server Receiving File");
				//System.out.println(clientFilelength);
				byte[] dataImage = new byte[clientFilelength];
				for (int i = 0; i < dataImage.length; i++) {
					dataImage[i]=input.readByte();
				}
				stream.write(dataImage);
				//System.out.println("Server"+serverTimeStamp);
				//System.out.println("Client"+clientTimeStamp);
				//f.setLastModified(clientTimeStamp);
				//System.out.println("After modification:Client "+f.lastModified());
				stream.close();
				long l=clientTimeStamp;
				while(l%1000!=0){
					l++;
				}
				f.setLastModified(l);
				output.writeLong(l);
				//System.out.println("After modification:Client "+f.lastModified());
				output.writeUTF("Received\n");
			}
			else{
				output.writeUTF("Doing Nothing");
				//System.out.println("Do Nothing");
			}
		} 
		catch(EOFException e) {
			e.printStackTrace();
			System.out.println("EOF:"+e.getMessage()); } 
		catch(IOException e) {
			System.out.println("IO:"+e.getMessage());}  
		finally { 
			try { 
				output.close();
				input.close();
				clientSocket.close();
			}
			catch (IOException e){/*close failed*/}
		}
	}
}
