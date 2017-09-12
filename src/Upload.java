import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Upload extends Thread
{  
   int clientListeningPort;
   File dir;
   int chunkSize;

   public Upload(int clientListeningPort, File dir, int chunkSize){
	   this.clientListeningPort = clientListeningPort;
	   this.dir = dir;
	   this.chunkSize = chunkSize;
   }
   
   public void run(){
	   try {  
		
		Socket requestsocket;
		ServerSocket cs = new ServerSocket(clientListeningPort);
		System.out.println("[Upload Thread]: Listening at Port" + clientListeningPort);
		requestsocket= cs.accept();
		System.out.println("[Upload Thread]: Get Connection from neighbour client.");
		
		ObjectOutputStream out = new ObjectOutputStream(requestsocket.getOutputStream());
		out.flush();
		ObjectInputStream in = new ObjectInputStream(requestsocket.getInputStream());
		
		while(true){
			System.out.println("RUN Upload");
			Thread.currentThread();
			Thread.sleep(1000);
			// receive the filelist of requested chunks
		    ArrayList<String> recevieFilelist = new ArrayList<String>();
		    recevieFilelist = (ArrayList<String>)in.readObject();
		    System.out.println("[Upload Thread]: Receive the filelist of requested chunks:"+ recevieFilelist );
		    
		    // filelist of chunks that the client have at present
		    ArrayList<String> filelist = new ArrayList<String>();
		    File[] files = dir.listFiles();
			for (File file:files){
			   filelist.add(file.getName());
			    }
			//System.out.println("get the filelists that the client have");
			
			//get the sendfilelist to send to the client that need download chunks
			ArrayList<String> Sendfilelist = new ArrayList<String>();
			for(int i=0; i<=recevieFilelist.size()-1; i++ ){
				for(int j=0; j<=filelist.size()-1; j++){
					if (filelist.get(j).equals(recevieFilelist.get(i)))
					Sendfilelist.add(filelist.get(j));
				}
			}
			System.out.println("[Upload Thread]: Send the uoloadfilelist to the neighbour client" + Sendfilelist);
			out.writeObject(Sendfilelist);
		    out.flush();
			
			// upload the chunks to other client
			if (Sendfilelist.size() > 0){
				for (int i=0; i<=Sendfilelist.size()-1; i++ ){
					BufferedInputStream bufIn;
					byte[] byteToSend = new byte[chunkSize];
					try
					{
						bufIn = new BufferedInputStream(new FileInputStream(Sendfilelist.get(i)));
						bufIn.read(byteToSend,0,chunkSize);
						out.write(byteToSend,0,chunkSize);
						out.flush();
						System.out.println("[Upload Thread]: Upload the chunks to neighbour:"+Sendfilelist.get(i));
						bufIn.close();
					}catch(FileNotFoundException ex)
					{
						System.out.println(" File Not Found");
					}
					catch(IOException ex)
					{
						System.out.println("IOException coour");
					}
				}
				
			} 
			
		}
			
	   } 
	   catch (IOException e) 
	    {
			// TODO Auto-generated catch block
			e.printStackTrace();
	    }
       catch (InterruptedException e) 
	    {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } 
	   catch (ClassNotFoundException e) 
	    {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
  }  
}

