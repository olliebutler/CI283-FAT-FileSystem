
public class main_2 {

	public static void main(String[] args) {
		
		
		FileSystemAPI FS;
		
		FS = new FileSystemAPI();
		
		FS.mount("sda1.dsk");
		
		Directory DI = new Directory();
		
		int filesize;
		String filename;
		
		
		
		int count = 0;
		
		for(int i = 0; i<C.DIR_ENTRIES; i++){
			if((FS.readEntryDir(i)).getBytes() != 0){
				
				count++;
			}
			
		}
		
		System.out.printf("Directory %d entries\n", count );
		
		for(int i = 0; i<C.DIR_ENTRIES; i++){
			if((FS.readEntryDir(i)).getBytes() != 0){
				
				DI = FS.readEntryDir(i);
				filename = DI.getName();
				filesize = DI.getBytes();
				
				System.out.printf("%-15s", filename);
				System.out.printf("%d", filesize);
				System.out.printf(" bytes\n");
			}
			
		}
	
	}

}
