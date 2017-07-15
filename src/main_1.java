public class main_1 {

	public static void main(String[] args) {
		
		FileSystemAPI FS;
		
		FS = new FileSystemAPI();
		
		FS.mount("sda1.dsk");
		
		int fd, res;
	    final int MAX_BUF = 100;
	    byte buf[] = new byte[MAX_BUF];
	    
	    fd = FS.open( "mas.java", C.O_RDONLY, 0 );
	    
	    res = FS.read( fd, buf, 21 );
	    
	    String fnl = "";
	    
	    
	    for ( int i=10; i<21; i++ )
	    {
	    	fnl = fnl + ((char) buf[i]);
	    }
	    
	    System.out.println(fnl);
	    
		

	}

}
