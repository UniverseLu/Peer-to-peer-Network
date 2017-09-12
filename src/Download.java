import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


public class Download  extends Thread
{   
	private int totalChunks;
	private int chunkSize;
	private String fileName; 
	private File dir ; 
	private int neighbourPort;
	
	
	public Download(int totalChunks, int chunkSize, String fileName, File dir,int neighbourPort){
		
		this.totalChunks = totalChunks;
		this.chunkSize= chunkSize;
		this.fileName= fileName;
		this.dir= dir;
		this.neighbourPort=neighbourPort;
	}
	
	public void run(){
	  Socket requestPeer = null;  
	  try{
	        //String msg;
	        while(requestPeer==null){
					try{
						requestPeer = new Socket("127.0.0.1",neighbourPort);
						
					}catch(IOException ex)
					{
						
					}
	    	}
	        if (requestPeer!=null){
	        	System.out.println("[Download Thread]: Connect to the client:"+neighbourPort);
	        	ObjectOutputStream out = new ObjectOutputStream(requestPeer.getOutputStream());
	        	out.flush();
	        	ObjectInputStream in = new ObjectInputStream(requestPeer.getInputStream());
	    			
	    	String msg = "true";
			while(!msg.equals("false")){
			System.out.println("RUN Download ");
			Thread.currentThread();
			Thread.sleep(1000);	
			// get all the chunk names that client3 have now  
		    ArrayList<String> filelist = new ArrayList<String>();
		    File[] files = dir.listFiles();
		    for (File file:files){
		    	filelist.add(file.getName());
		    }
		    System.out.println("[Download Thread]: Get the filelists that the client have"+filelist);
		    
		    //get all the chunk names that client do not have
		    ArrayList<String> Sendfilelist = new ArrayList<String>();
			  for(int i=1; i<= totalChunks; i++){
				  Sendfilelist.add("chunk."+i);
			  }
			  
		    int sendfilelistsize = totalChunks;
		    for(int i=0; i<= filelist.size()-1; i++){
		    	for(int j=0; j<= Sendfilelist.size()-1; j++){
		    		if (Sendfilelist.get(j).equals(filelist.get(i)))
		    			Sendfilelist.remove(j);
		    		    //sendfilelistsize= sendfilelistsize - 1;
		    	}	
		    }
		    System.out.println("[Download Thread]: Send the REQUEST of the chunk names that client do NOT have"+Sendfilelist);
		    
		    // send the request of chunks needed 
		    out.writeObject(Sendfilelist);
		    out.flush();
		    
		    
		    // download chunks from neighbours
		    ArrayList<String> infilelist = new ArrayList<String>();
		    infilelist = (ArrayList<String>)in.readObject();
		    System.out.println("[Download Thread]: Get download filelist from neighbour client:" + infilelist);
		    for(int chunkid=0;chunkid<=infilelist.size()-1;chunkid++){
				String chunkName =  infilelist.get(chunkid);
				System.out.println("[Download Thread]: Download chunks from neighbour client:" + chunkName);
				
		            BufferedOutputStream bufOut;
            	    bufOut = new BufferedOutputStream(new FileOutputStream(new File(dir, chunkName)));
            	  
            	    
                    int bytesRead=0;
                    int buf=0;
                    while(bytesRead < chunkSize && (buf=in.read())!=-1)
                    {       
                    	    //int buf= in.read();
                            bufOut.write(buf);
                            bytesRead++;
                            //System.out.println(bytesRead);
                    }
                    System.out.println("[Download Thread]: Write the "+ chunkName+" to the filepath");
                    bufOut.close();
              
	       }   
		   
		    // merge all the chunks
		  ArrayList<String> fullfilelist = new ArrayList<String>();
		  for (File file:files){
			  fullfilelist.add(file.getName());
		    }
		  if (fullfilelist.size() == totalChunks){
		    	 
		    BufferedOutputStream bufOut2;
		    bufOut2 = new BufferedOutputStream(new FileOutputStream(new File(dir, fileName)));
		    try
              {                 
                     for(int i=1;i<=totalChunks;i++)
                     {
                         	 BufferedInputStream bufIn2 = new BufferedInputStream(new FileInputStream("chunk." + i));
                         	 int buf;
                             while((buf=bufIn2.read())!= -1)
                             {  //int buf = bufIn2.read();
                                bufOut2.write(buf);
                             }

                     }
                     bufOut2.close();
                    System.out.println("[Download Thread]: Merge the chunks to the file.");
                    System.out.println("[Download Thread]: This client has received the full file, download thread will be closed.");
                    msg = "false";
             }
		catch(FileNotFoundException ex)
             {
              System.out.println("File Not Found");
             }
        catch(IOException ex)
             {
              System.out.println("IOException occurred");
             }
  		  } 
	  }
	 }
	}catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}