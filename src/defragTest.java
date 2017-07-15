import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class defragTest {

	FileSystemAPI S;

	@Before
	public void setUp() throws Exception {
		S = new FileSystemAPI();
	}
	

		
	@Test
	public void deFragTest() {
		FileSystemAPI S = new FileSystemAPI();
	    S.mkfs( "sda1.dsk" );

	    //Test mount FS" );
	    S.mount( "sda1.dsk" );

	    int fd, res;
	    final int MAX_BUF = 100;
	    byte buf[] = new byte[MAX_BUF];
	    //File system test

	    // ---------------------------------- 
		  
	    //Create File_1 write MAX_BUF characters
	  
	    for ( int i=0; i< MAX_BUF; i++ ) buf[i] = c(i);
	  
	    fd = S.open( "File_1", C.O_WRONLY|C.O_CREAT, 0777 );
	    assertTrue("open(\"File_1\", O_WRONLY|O_CREAT, 0777) -> " + fd, fd >= 0);
	    res = S.write( fd, buf, MAX_BUF );
	    assertTrue("write( fd, buf, MAX_BUF ) --> " + res, res == MAX_BUF);
	    res = S.close( fd );
	    assertTrue("S.close( fd ) --> " + res, res == C.OK);
	    
	   

	    
	    // ---------------------------------- 
	  
	    //Create File_2 write MAX_BUF characters
	  
	    for ( int i=0; i< MAX_BUF; i++ ) buf[i] = c(i);
	  
	    fd = S.open( "File_2", C.O_WRONLY|C.O_CREAT, 0777 );
	    assertTrue("open(\"File_1\", O_WRONLY|O_CREAT, 0777) -> " + fd, fd >= 0);
	    res = S.write( fd, buf, MAX_BUF );
	    assertTrue("write( fd, buf, MAX_BUF ) --> " + res, res == MAX_BUF);
	    res = S.close( fd );
	    assertTrue("S.close( fd ) --> " + res, res == C.OK);
	    

	    // ---------------------------------- 
		  
	    //Delete File_1
	   
	    
	    
	    res = S.unlink( "File_1" );
	    assertTrue("unlink(File_1) -> " + res, res == C.OK);
	    fd = S.open( "File_1", C.O_RDONLY, 0 );
	    assertTrue("open(\"File_1\", O_RDONLY, 0) should not exist fd -> " + fd, fd < 0);
	    
	    
	    

	  
	    // ---------------------------------- 

	    //Create File_3 write characters till fill disk
	  
	    for ( int i=0; i< MAX_BUF; i++ ) buf[i] = c(i);
	  
	    fd = S.open( "File_3", C.O_CREAT|C.O_WRONLY, 0 );
	    assertTrue("S.open( \"File_3\", O_CREAT|O_WRONLY, 0) -> " + fd, fd >= 0);
	    for (;;)
	    {
	      res = S.write( fd, buf, MAX_BUF );
	      if ( res != MAX_BUF ) break;
	    }
	    res = S.write( fd, buf, MAX_BUF );
	    assertTrue("File system should be FULL -> " + res, res == C.ERROR);
	    res = S.close( fd );
	    assertTrue("close(fd)  -> " + res, res == C.OK);
	    
	        
		  // --------------------------------
		    
		  //prints the state of the system before the defrag
		    
		   S.printStateOfFileSystem();		
		   
		   
		   
		  // --------------------------------
		    
		  //run defrag
		   
		   S.defrag();
	   
		   // --------------------------------
		    
		   //test all blocks are in sequential order 
		   
	   
		   for(int i=0; i < C.FAT_ENTRIES; i++){
			   fd = S.readEntryFAT(i);
			   assertTrue("FAT entry == i+1 or C.END - " + fd, fd == i+1 || fd == C.END);
			   
		   }
		   
		   
		   // --------------------------------
		    
		   //prints the state after, as you will see the blocks for the files are all grouped and in order
				
		    
		   S.printStateOfFileSystem();		
	   
	}


	
	public static byte c(int i) {
		char c = (char) i;
		if (c < ' ' || c > '~')
			c = '.';
		return (byte) c;
	}

	public static byte[] toByte(String s) {
		byte res[] = new byte[s.length()];
		for (int i = 0; i < s.length(); i++)
			res[i] = (byte) s.charAt(i);
		return res;
	}

	public static String toString(byte buf[]) {
		String s = "";
		for (int i = 0; i < buf.length; i++)
			s += (char) buf[i];
		return s;
	}

}
