import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
//TCPClient class
//
//******************ERRORS********************************
//Exceptions may be thrown by ConnectionNew Class

/**
 *Implements an TCP Server Client file transfer application.
 * @author Haridarshan
 */
public class TCPClient {
	/**
	 * Run Method:Creates a input and output data stream for the Server socket
	 * 				and starts the server for transaction of the data
	 * 				S1:Waits for the client to make a connection and receives the Type of the file that
	 * 					 the Client is intending to send
	 * 			  	S2:Receives the Time stamp of the file that is being sent from the client
	 * 			  	S3:If the time stamp of the client file is less than that of the server file,
	 * 				 	The server send the file to the client.If that's not the case the client sends the file.
	 */
	public static void main (String args[]) throws Exception
	{
		Socket s = null;
		try{
			int serverPort = 123;
			String ip = "192.168.0.10";
			String data="";
			File file=new File("b.txt");
			boolean a=true;
			while(a==true){
				s = new Socket(ip, serverPort);
				DataInputStream input = new DataInputStream( s.getInputStream());
				DataOutputStream output = new DataOutputStream( s.getOutputStream());
				String fileType=(String) file.getPath().subSequence(file.getPath().length()-3, file.getPath().length());
				//System.out.println(fileType);
				output.writeUTF(fileType);
				String rxdData=input.readUTF();
				//System.out.println(rxdData);
				while(!(rxdData.contentEquals("FileType Received"))){
					rxdData=input.readUTF();
				}
				//System.out.println("File type RXD");
				FileInputStream fis = new FileInputStream(file);
				//System.out.println(file.length());
				byte[] myArrayImage = new byte[(int) file.length()];
				int len = 0 ;
				int total = 0;
				for (int i = 0; i < myArrayImage.length; i++) {
					myArrayImage[i]=(byte) fis.read();
				}
				fis.close();


				int dataLen=myArrayImage.length;
				//System.out.println("Client data len"+dataLen);
				String s1=String.valueOf(dataLen);
				output.writeUTF(s1);//file length sent
				if(input.readUTF().contentEquals("FileLength Received")){
					//System.out.println("File Length RXD");
					output.writeUTF(String.valueOf(file.lastModified()));
					String str=input.readUTF();
					if(str.contentEquals("Server Sending File")){
						System.out.println("Server sending file");
						String srvrTimeStamp=input.readUTF();
						FileOutputStream stream=new FileOutputStream(file);
						String fileLen=input.readUTF();
						int clientFilelength=Integer.parseInt(fileLen);
						//System.out.println("Server file length"+clientFilelength);
						byte[] dataImage = new byte[clientFilelength];
						for (int i = 0; i < dataImage.length; i++) {
							dataImage[i]=input.readByte();
						}
						stream.write(dataImage);
						stream.close();
						file.setLastModified(Long.valueOf(srvrTimeStamp));
						//System.out.println(file.lastModified());
						output.writeUTF("Received\n");
					}
					else if(str.contentEquals("Server Receiving File")){
						System.out.println("Server Receiving file");
						output.write(myArrayImage);
						//System.out.println(file.lastModified());
						//System.out.println("Writing Complete"); 
						Long l=input.readLong();
						file.setLastModified(l);
						String st=input.readUTF();
						//System.out.println("Received: "+ st);
					}
					else{
						//System.out.println("Doing Nothing");
					}
				}
				//System.out.println(file.lastModified());
			}
		}
		catch (UnknownHostException e){
			System.out.println("Sock:"+e.getMessage());}
		catch (EOFException e){
			e.printStackTrace();
			System.out.println("EOF:"+e.getMessage()); }
		catch (IOException e){
			System.out.println("IO:"+e.getMessage());}
		finally {
			if(s!=null)
				try {
					s.close();
				}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
