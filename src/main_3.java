
public class main_3 {

	public static void main(String[] args) {
		
		
		FileSystemAPI FS;
		
		FS = new FileSystemAPI();
		
		FS.mount("sda1.dsk");
		
		System.out.println("Free blocks:");
		
		int count = 1;
		
		for(int i=0; i< C.TOTAL_FAT_ENTRIES; i++){
			
			if(FS.readEntryFAT(i) == -1){
				
				
				if(count % 5 == 0){
					System.out.println(i);
				}
				else{
					System.out.print(i + " ");
				}
				
				count++;
			
			}
			
		}

	}

}
