package com.example.acnandroid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author Haridarshan
 *
 */
public class TCPActivity extends Activity implements OnClickListener{
	EditText tvServerIpAddress;
	EditText tvLaptopIpAddress;
	/**
	 * OnCreate Method:Waits for the Send server and Send Laptop Button to be clicked via a Click event Listener
	 */
	Socket s = null;
	@SuppressLint("NewApi") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
			tvServerIpAddress=(EditText) findViewById(R.id.tvServerAddress);
			tvLaptopIpAddress=(EditText) findViewById(R.id.tvLaptopAddress);
			Button bnSendServer=(Button) findViewById(R.id.bnServerSend);
			Button bnSendLaptop=(Button) findViewById(R.id.bnLaptopSend);
			bnSendLaptop.setOnClickListener(this);
			bnSendServer.setOnClickListener(this);			
		}

	@SuppressLint("NewApi") 
	private void clientCommunication(String address) {
		try{
			StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			int serverPort = 6111;
			String ip = address;
			File file=new File(Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
					+ "/b.txt");
			boolean a=true;
			//while(a==true){
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
					Toast.makeText(getApplicationContext(), "File Transfer",Toast.LENGTH_SHORT).show();
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
					Toast.makeText(getApplicationContext(), "File Transfer",Toast.LENGTH_SHORT).show();
				}
				else{
					//System.out.println("Doing Nothing");
				}
			}
			//System.out.println(file.lastModified());
			//}
		}
		catch (UnknownHostException e){
			Toast.makeText(getApplicationContext(), "Invalid Address",Toast.LENGTH_LONG).show();
			System.out.println("Sock:"+e.getMessage());
			Log.d("asd",e.toString());}
		catch (EOFException e){
			Toast.makeText(getApplicationContext(), "File Not Proper,Recheck the file before Sending",Toast.LENGTH_LONG).show();
			e.printStackTrace();
			System.out.println("EOF:"+e.getMessage());
			Log.d("asd",e.toString());}
		catch (IOException e){
			Toast.makeText(getApplicationContext(), "Invalid Address",Toast.LENGTH_LONG).show();
			System.out.println("IO:"+e.getMessage());
			Log.d("asd",e.toString());}
		catch(Exception e){
			Toast.makeText(getApplicationContext(), "Invalid Address",Toast.LENGTH_LONG).show();
			Log.d("asd",e.toString());
		}
		finally {
			if(s!=null)
				try {
					s.close();
				}
			catch (IOException e) {
				Toast.makeText(getApplicationContext(), "Invalid Address",Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}

		}
	}

	/* 
	 *
	 * OnClick Method:S1:Waits for the client to make a connection and receives the Type of the file that
	 * 				     the Client is intending to send
	 * 			 	  S2:Receives the Time stamp of the file that is being sent from the client
	 * 			 	  S3:If the time stamp of the client file is less than that of the server file,
	 * 				  	 The server send the file to the client.If that's not the case the client sends the file.
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId()==R.id.bnServerSend){
			//server transaction
			String str=(String) tvServerIpAddress.getText().toString();
			Log.d("asd",str);
			//System.out.println(str);
			Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
			
			if(str!=null && (!(str.trim().contentEquals("")))){
				//call program
				if(ipAddressChecker(str))
					clientCommunication(str);
				else{
					Toast.makeText(getApplicationContext(), "Enter a Valid Server IP Address", Toast.LENGTH_LONG).show();
				}
			}
			else{
				Toast.makeText(getApplicationContext(), "Enter a Valid Server IP Address", Toast.LENGTH_LONG).show();
			}
		}
		else{
			//laptop transaction
			String str=(String) tvLaptopIpAddress.getText().toString();
			if(str!=null && (!(str.trim().contentEquals("")))){
				//call program
				if(ipAddressChecker(str))
					clientCommunication(str);
				else{
					Toast.makeText(getApplicationContext(), "Enter a Valid Laptop IP Address", Toast.LENGTH_LONG).show();
				}
			}
			else{
				Toast.makeText(getApplicationContext(), "Enter a Valid Laptop IP Address", Toast.LENGTH_LONG).show();
			}
		}
	}
	/**
	 * ipAddressChecker Method:Checkes the IP address for its correctness
	 * 						   
	 */
	private Boolean ipAddressChecker(String str) {
		//str.indexOf(".");
		if(str.length()>6 && str.length()<16)//xxx.xxx.xxx.xxx
		{
			int initPos=str.indexOf(".");
			int x=0;
			//System.out.println(str);
			for (int i = 0; i < 3; i++) {
				//System.out.println(initPos);
				if(initPos==-1){
					//System.out.println("Invalid");
					return false;
					}
				//System.out.println(str.substring(x, initPos));
				if(str.substring(x, initPos).length()>3 || Integer.parseInt(str.substring(x, initPos))>255){
					//System.out.println("Invalid");
					return false;
				}
				else{
					x=initPos+1;
					initPos=str.indexOf(".",x);
					//System.out.println("valid");
				}
			}
			//System.out.println("");
			//System.out.println(x);
			//System.out.println(initPos);
			if(str.substring(x).length()>3 || Integer.parseInt(str.substring(x))>255){
				//System.out.println("Invalid");
				return false;
			}
			//System.out.println("valid");
			return true;
		}
		else{
			//System.out.println("invalid");
			return false;
		}
	}}
