import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.lang.InterruptedException;
import java.util.ArrayList;

public class client4 
{
	static String serverIP = "127.0.0.1";
	static int serverPort ;//= Integer.parseInt("5555");
	static int neighbourPort;// = Integer.parseInt("5005");
	static int clientListeningPort;// = Integer.parseInt("5004");
    static int totalChunks;
	static String fileName;
	static File dir;
	static int chunkSize;
	
	
	public static void main(String[] args) throws IOException
	{
			try
			{   //read the config 
				File config = new File("config.txt");
				BufferedReader creader = new BufferedReader(new FileReader(config));
				//String line =null;
				//line = creader.readLine();
				//serverPort = Integer.parseInt(line);
				String textline = null;
				for(int i=1;i<=5;i++){
				textline=creader.readLine();
				}
				String[] txtArray=textline.split(" ");
				serverPort = Integer.parseInt(txtArray[0]);
				clientListeningPort= Integer.parseInt(txtArray[1]);
				neighbourPort= Integer.parseInt(txtArray[2]);
				
				Socket peerSocket = new Socket(serverIP,serverPort);
				if(peerSocket != null)
				{
					System.out.println("Client Connected to Server Running on Port :"+ serverPort);
					ObjectInputStream in = new ObjectInputStream(peerSocket.getInputStream());
					
					//Get the name of file first.
					fileName = (String)in.readObject();
					System.out.println("FileName recieved : " + fileName);
					
					//Get the total number of chunks of the file.
					totalChunks = (int)in.readObject();
					System.out.println("Total Chunks for file: " + totalChunks);
					
					//Get the size of every chunks.
				    chunkSize = (int)in.readObject();
					System.out.println("Size of each of chunks:" + chunkSize);
					
					//Get initial chunkID 
					int initialChunkID =(int)in.readObject();
					System.out.println("Client will receive Initial ChunkID : " + initialChunkID);
					
					//Get last Chunkid 
					int lastChunkID = (int)in.readObject();
					System.out.println("Client will receieve Last ChunkID : " + lastChunkID);
					

//				    // Write the chunks to the client file path.  
				    for(int chunkid=initialChunkID;chunkid<=lastChunkID;chunkid++){
						String chunkName = "chunk." + chunkid;
						
		                try
		                {
		                	dir = new File("client4");
		                	dir.mkdirs();	                       
		                	BufferedOutputStream bufOutput = new BufferedOutputStream(new FileOutputStream(new File(dir,chunkName)));
		                        
		                	 int bytesRead=0;
		                        int buf;
		                        while(bytesRead < chunkSize && (buf=in.read())!=-1)
		                        {       //int buf= in.read();
		                                bufOutput.write(buf);
		                                bytesRead++;
		                        }
		                       
		                        bufOutput.close();
		                }
		                catch(IOException ex)
		                {
		                        System.out.println("Exception While Saving Chunk");
		                }

						System.out.println("Client receives  " + chunkid + " from server");
						
					}
					peerSocket.close();
				}
			}
			catch(ClassNotFoundException ex)
			{
				System.out.println(ex.getMessage());
			}
			catch(IOException io)
			{
				System.out.println(io.getMessage()); 
			}
		
		
		//start the download thread
		Download download= new Download(totalChunks, chunkSize, fileName, dir,neighbourPort);
		download.start();
		System.out.println("downloadThread");
		
		// start the upload thread	
		Upload upload = new Upload( clientListeningPort, dir, chunkSize);
		upload.start();
		System.out.println("uploadThread");
			
		try {
			upload.join();
			download.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
}


