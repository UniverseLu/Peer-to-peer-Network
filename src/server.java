import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.lang.InterruptedException;
import java.io.File;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;




public class server {
	private static int serverPort ;
	public static final int ChunkSize = 1024*100;
	public static int totalChunks;
	public static int offset;
	public static int TotalClient = 5;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try
		{	//read the config to get the serverport
			File config = new File("config.txt");
			BufferedReader creader = new BufferedReader(new FileReader(config));
			String line =null;
			line = creader.readLine();
			serverPort = Integer.parseInt(line);
			//server server = new server();
			ArrayList<Thread> threads = new ArrayList<Thread>();
			ServerSocket ss = new ServerSocket(serverPort);
			System.out.println("Server Listening at Port" + serverPort);
			
			//Split the file into chunks of Size CHUNK_SIZE if the file exists.;
			Scanner sc = new Scanner(System.in);
			System.out.println("Please input the file name");
			String fileName = sc.nextLine();
			//String fileName = "a1.txt"  ;
			File file = new File(fileName);
			if(file.exists()){		
			BufferedOutputStream bufOutput;
			BufferedInputStream bufInput;
			try
			{
				long fileSize = file.length();
				totalChunks =(int) (Math.ceil((double)fileSize/(double)ChunkSize));
				bufInput = new BufferedInputStream(new FileInputStream(fileName));
				int i =1;
				while(i <= totalChunks)
				{
					bufOutput = new BufferedOutputStream(new FileOutputStream("chunk." + i));
					int j=0;
					int buf;
					while(j < ChunkSize && ((buf=bufInput.read())!= -1))
					{
						bufOutput.write(buf);
						j++;
					}
					bufOutput.close();
					i=i+1;
				}
				offset = 1;
				bufInput.close();
				System.out.println("Total Chunks: "+ totalChunks);
			 }
			   catch(IOException ex)
			   {
				System.out.println("Chunks are missing.");
			    }	
		   }	
			else
				throw (new FileNotFoundException());
			
			int connectClient =1;	
			while(connectClient<=TotalClient)
			 {
				//For each connection start a send thread 
				Socket clientSocket = ss.accept();
				
				int nextChunkID = offset + totalChunks/TotalClient;
				int restChunks = totalChunks % TotalClient ;
				if(connectClient == TotalClient){
					nextChunkID = nextChunkID + restChunks ;
				 }
				SendThread sendthread = new SendThread(clientSocket, offset, nextChunkID, fileName, totalChunks,ChunkSize);
				Thread sends = new Thread(sendthread);
				threads.add(sends);
				sends.start();
				offset = nextChunkID;
				connectClient++;
			}
			//Wait till all threads end.
			for(Thread thread:threads)
			{
				thread.join();
			}
			
			ss.close();
		}catch(NumberFormatException ex)
		{
			System.out.println("Please choose a proper Port Number between 1024-65536");
		}
		catch(FileNotFoundException ex)
		{
			System.out.println("FileNotFoundException: "+ ex.getMessage());
		}
		catch(IOException ex)
		{
			System.out.println("IOException: "+ ex.getMessage());
		}
		catch(InterruptedException ex)
		{
			System.out.println("Interrupted Exception: " + ex.getMessage());
		}
		
	}
}


class SendThread implements Runnable
{
	private Socket clientSocket;
	private String fileName;
	private int totalChunks;
	private int initialChunkID ;
	private int lastChunkIDPlus1 ;
	private int chunkSize;
	
	public SendThread(Socket clientSocket, int initialChunkID, int nextChunkID, String fileName, int totalChunks, int chunkSize)
	{
		this.clientSocket = clientSocket;
		this.initialChunkID = initialChunkID;
		this.lastChunkIDPlus1 = nextChunkID;
		this.fileName = fileName;
		this.chunkSize = chunkSize;
		this.totalChunks= totalChunks;
	}
	
	public void run()
	{
		ObjectOutputStream out;         
		try
		{
			System.out.println("[Thread: "+ Thread.currentThread().getId() + "]: Get connection from client " + clientSocket.getPort());
			System.out.println("[Thread: "+ Thread.currentThread().getId() + "]: Address of connection Client is " + clientSocket.getRemoteSocketAddress());
			
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			out.flush();
			
			//Send filename 
			out.writeObject(fileName);
			out.flush();
			
			//Send total number of chunks 
			out.writeObject(totalChunks);
			out.flush();
			
			//Send size of each chunk.
			out.writeObject(chunkSize);
			out.flush();
			
			//Send the initial ChunkID
			out.writeObject(initialChunkID);
			out.flush();
			
			//Send the last chunk ID
			out.writeObject(lastChunkIDPlus1-1);
			out.flush();
			
			//loop will iterate for sending chunk from index initialChunkID inclusive and lastChunkID exclusive.
			System.out.println("[Thread: "+Thread.currentThread().getId() + "]: Send chunks to this client from id " + initialChunkID + " to " + (lastChunkIDPlus1-1));
			for(int i=initialChunkID;i<=lastChunkIDPlus1-1;i++)
			{
			    //Send each chunk 
			    String chunkName= "chunk." + i;
			    BufferedInputStream bufInput;
				byte[] byteToSend = new byte[chunkSize];
				try
				{
					bufInput = new BufferedInputStream(new FileInputStream(chunkName));
					bufInput.read(byteToSend,0,chunkSize);
					out.write(byteToSend,0,chunkSize);
					//out.flush();
					bufInput.close();
				}
				catch(FileNotFoundException ex)
				{
					System.out.println("File Chunk Not found Exception.");
				}
				catch(IOException ex)
				{
					System.out.println("IOException.");
				}		    
			    out.flush();
			    System.out.println("[Thread: "+Thread.currentThread().getId() + "]:Send chunk "+i+" to client"+clientSocket.getPort());
			}
			System.out.println("[Thread: "+Thread.currentThread().getId() + "]: Sending is over, close the connection.");
			System.out.println();
			out.close();

		}
		catch(FileNotFoundException ex)
		{
			System.out.println(" File Not Found Exception: " + ex.getMessage());
		}
		catch(IOException ex)
		{
			System.out.println("IOException: "+ ex.getMessage());
		}
	}
}



