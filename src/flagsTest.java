import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class flagsTest {

	FileSystemAPI S;

	@Before
	public void setUp() throws Exception {
		S = new FileSystemAPI();
	}
	
	@Test
	public void testMountDisk(){
		S.mount("sda.dsk");
	}
		
	@Test
	public void testOfAPI() {
		FileSystemAPI S = new FileSystemAPI();
	    S.mkfs( "sda1.dsk" );

	    //Test mount FS" );
	    S.mount( "sda1.dsk" );

	    int fd, res;
	    final int MAX_BUF = 100;
	    byte buf[] = new byte[MAX_BUF];
	    //File system test

	    // ---------------------------------- 
	  
	    //Create File_1 write a single character
	  
	    fd = S.open( "File_1", C.O_WRONLY|C.O_CREAT, 0777 );
	    assertTrue("open(\"File_1\" C.O_WRONLY|C.O_CREAT, 0777) ->" + fd , fd >= 0);
	    res = S.write( fd, toByte("A"), 1 );
	    assertTrue("write(fd, \"A\", 1 )  -> " +res, res == 1);
	    res = S.close( fd );
	    assertTrue("close(fd) -> " + res , res == C.OK);

	    // ---------------------------------- 
	  
	    //Open File_1 read a single character
	  
	    fd = S.open( "File_1", C.O_RDONLY, 0 );
	    assertTrue("S.open( \"File_1\", O_RDONLY, 0) -> " + fd, fd >= 0);
	    res = S.read( fd, buf, 1 );
	    assertTrue("S.read( fd, buf, 1 ) -> " + res, res == 1);
	    assertTrue(String.format("Read File_1 -> Expected %c got %c", 'A', (char) buf[0]), buf[0] == 'A');
	    res = S.close( fd );
	    assertTrue("S.close(fd)  -> " + res, res == C.OK);
	    
	    //------------------------------------
	    
	    //Test flag read only
	    
	    fd = S.open( "File_1", C.O_RDONLY, 0 );
	    assertTrue("S.open( \"File_1\", O_RDONLY, 0) -> " + fd, fd >= 0);
	    res = S.write( fd, toByte("A"), 1 );
	    assertTrue("write(fd, \"A\", 1 )  -> " +res, res == C.ERROR);
	    res = S.close( fd );
	    assertTrue("S.close(fd)  -> " + res, res == C.OK);

	    
	    // ---------------------------------- 
	  
	    //Test flag write only
	  
	    fd = S.open( "File_1", C.O_WRONLY, 0 );
	    assertTrue("S.open( \"File_1\", O_WRONLY, 0) -> " + fd, fd >= 0);
	    res = S.read( fd, buf, 1 );
	    assertTrue("S.read( fd, buf, 1 ) -> " + res, res == C.ERROR);

	    
	    
	}

	@Test
	public void basicTestOfAPI() {

		//Test create partition
		S.fdisk("sda1.dsk");

		//Test mount FS

		S.mount("sda1.dsk");

		Block b = new Block();
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < C.BLOCKSIZE; j++)
				b.setByte(j, (byte) ('A' + i + 2));
			S.write(b, i);
		}

		for (int i = 0; i < 10; i++) {
			b = S.read(i);
			for (int j = 0; j < C.BLOCKSIZE; j++) {
				byte c = b.getByte(j);
				byte ans = (byte) ('A' + i + 2);
				if (c != ans)
					FATAL.message(
							"Block read/write <%d> <%d> Block =%d pos =%d", c,
							ans, i, j);
			}
		}

		int value = -1;

		//Test read/write FAT entry

		b.setFATentry(7, value);
		int ans = b.getFATentry(7);
		if (ans != value)
			FATAL.message("<%d> <%d>", ans, value);

		for (int i = 0; i < 32; i++) {
			value = 1 << i;
			b.setFATentry(7, value);
			ans = b.getFATentry(7);
			if (ans != value)
				FATAL.message("<%d> <%d>", ans, value);
		}

		//Test read/write Directory entries
		for (int i = 0; i < C.DIR_ENTRIES; i++) {
			String name = "mas" + i;
			int start = 10 + i;
			int bytes = 100 + i;
			Directory de = new Directory(name, start, bytes);
			b.setDIRentry(i, de);
			de = b.getDIRentry(i);
		}
		for (int i = 0; i < C.DIR_ENTRIES; i++) {
			String name = "mas" + i;
			int start = 10 + i;
			int bytes = 100 + i;
			Directory de = b.getDIRentry(i);
			if (!name.equals(de.getName()))
				FATAL.message("Directory name <%s> <%s>\n", name, de.getName());
			if (start != de.getStart())
				FATAL.message("Directory start <%d> <%d>\n", bytes,
						de.getStart());
			if (bytes != de.getBytes())
				FATAL.message("Directory bytes <%d> <%d>\n", bytes,
						de.getBytes());
		}
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
