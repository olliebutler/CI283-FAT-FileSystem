/*

My extensions 

1. read only and write only flags 
This is a pretty basic addition, when a file is opened it sets a mode flag in the openTable 
telling you if the file is read or write only. The read and write methods then check this to 
make sure you are not trying to do a unauthorized operation. My test attempts to do an 
unauthorized read and write and checks that the return is C.ERROR. This function makes changes to 
the FD class. 

2. Defrag method
This is a much more complex extension. The defrag method organises the data blocks so that
the files are in the same order as the directory table and so that the data blocks of each file 
are in sequential order. My defrag tests (defragTest and defragTest2) create a series of files that are fragmented and then
defragments them and tests that all the data blocks for all the files are in sequential order.
It also prints the file system before and after the defrag so you can see the results. 
I split the defragmention process into 3 different methods:

	1. defrag - this is the main defrag method, it calls the other two methods when needed.
	it cycles through all the directory entries and orders the blocks by calling the swapBlock method 
	2.analyseFileSystem - this method cycles through the blocks and records the FAT record that points to 
	each block. This is needed as when we swap 2 blocks we have to make the previous block
	point to the right place.
	3. swapBlocks this swaps 2 blocks and corrects the FAT record for the previous blocks and current blocks.
	it also corrects the start position of the file in the openTable and directory if neeed.
		
*/

import java.util.Arrays;

/**
 * API calls: open, creat, read, write, unlink.
 */

class FileSystemAPI extends FileSystem_BASE_API
{
  /**
   * Open a file for read/ write
   * <BR><FONT COLOR="red">YOU NEED TO ADD CODE</FONT>
   * @return a fd to the file or (error C.EACCES, C.ENFILE)
   */

// ------------------------------------------------------------------ 
// ***** FILL IN CODE *********************************************** 
// ------------------------------------------------------------------ 
//  Open a file for reading 
//  Needs to 
//   Find if file exists 
//   Return fd (File descriptor) to open file 
//     flags = O_CREATE, O_WRONLY, O_RDONLY 
// ------------------------------------------------------------------ 

  public int open( String name, int flags, int mode )
  {
    DEBUG.trace("<<< open     : open(\"%s\",%04o,%04o)", name, flags, mode);
    	
    //System.out.println(flags);	
    
	  if((flags & C.O_CREAT) == C.O_CREAT){ //checks if create flag has been passed 
		  
		  if((flags & C.O_RDONLY) == C.O_RDONLY){
			  return creat(name, C.O_RDONLY);
		  }
		  else if((flags & C.O_WRONLY) == C.O_WRONLY){
			  return creat(name, C.O_WRONLY);
		  }
		  			
	  }
	  
	  for ( int i=0; i<C.DIR_ENTRIES; i++ ){ //loops through directory
		  
		  Directory de = readEntryDir( i );	//gets directory entry
		  
		  if(de.getName().equals(name)){  	//if its the entry we want adds it to open table
			 int fd =  allocateFD();
			 
			 openTable[fd].dirPos = i;           
		     openTable[fd].curPos = 0;          
		     openTable[fd].bytes  = de.getBytes();           
		     openTable[fd].firstBlock = de.getStart();    
		     openTable[fd].curBlock  = de.getStart();
		     
		     if((flags & C.O_RDONLY) == C.O_RDONLY){		//
		    	 openTable[fd].mode = C.O_RDONLY;			//
		     }												//sends mode to openTable
		     else if((flags & C.O_WRONLY) == C.O_WRONLY){	//
		    	 openTable[fd].mode = C.O_WRONLY;			//
		     }												//
		         
			return fd;
			 
		  } 
	  }
	  
    int res = C.EACCES;
    return res;
  }


  /**
   * Create an empty file in the filing system
   * @return a fd to the file or (error C.EOVERFLOW, C.ENFILE)
   */

// ------------------------------------------------------------------ 
// Create a new file to write to 
//  Create entry for file in master directory 
//  Create an entry in the open table for file 
//   setting contents to show empty file 
//  Return fd (File descriptor) to new file 
// ------------------------------------------------------------------ 

  public int creat( String name, int mode  )
  {
    DEBUG.trace("<<< creat    : creat(\"%s\",%04o)", name, mode);
    int res = C.EOVERFLOW;                 // No room in directory 
    for ( int i=0; i<C.DIR_ENTRIES; i++ )
    {
     Directory de = readEntryDir( i );     // Read  
     if ( de.getName().length() == 0 )     // ? is free 
     {
       int fd = allocateFD();              // Allocated a File Descriptor 
       if ( fd == C.NO_ITEM )
         return C.ENFILE;                  // Maximum no. of files open 
  
       de.setName( name );                 // File name 
       de.setStart( C.END );               // First block 
       de.setBytes( 0 );                   // Bytes in file 
       writeEntryDir( i, de );             // Update directory 
  
       openTable[fd].dirPos = i;           // Remember directory pos 
       openTable[fd].curPos = 0;           // Current pos in file 
       openTable[fd].bytes  = 0;           // Bytes in file (total)
       openTable[fd].firstBlock=C.FREE;    // Start block not allocated 
       openTable[fd].curBlock  =C.FREE;    //  so no current block 
       
       if((mode & C.O_RDONLY) == C.O_RDONLY){		//sends mode to openTable
			  openTable[fd].mode = C.O_RDONLY;		//
	  }												//
	  else if((mode & C.O_WRONLY) == C.O_WRONLY){	//
		  	openTable[fd].mode = C.O_WRONLY;		//
	  }												//
     	
       res = fd;                           // return File Descriptor 
       break;
     }
    }
    DEBUG.trace( ">>> creat    : result = %d", res );
    return res;                           // result 
  }

  /**
   * Write length bytes from buf[] to the file (associated with the fd)
   * @return the number of bytes written ( or C.ERROR )
   */

  public int write( int fd, byte buf[], int length )
  {
	  
	if(openTable[fd].mode == C.O_RDONLY){
		return C.ERROR;
	}
	  
    DEBUG.trace("<<< write    : " );
    int res = length;
    for( int i=0; i<length; i++ )
    {
      int r = writeByte( fd, buf[i] );
      if ( r !=  C.OK ) 
      {
        res =  C.ERROR;
        break;
      }
    }
    DEBUG.trace( "write %3d - %3d ", res, /*buf,*/ length );
    return res;
  }
  

  /**
   * Read upto length bytes from the file (associated with the fd) into buf[]
   * @return the number of bytes read ( or C.ERROR )
   */

  public int read( int fd, byte buf[], int length )
  {
	  
	if(openTable[fd].mode == C.O_WRONLY){
		return C.ERROR;
	}
	  
    DEBUG.trace("<<< read     : " );
    int bytesRead = 0;
    for( int i=0; i<length; i++ )
    {
      int ch = readByte( fd );
      if ( ch == C.E_EOF ) break;
      if ( ch < 0 )
      {
        bytesRead = C.ERROR;
        break;
      }
      bytesRead++;
      buf[i] = (byte) ch;
    }
    
    DEBUG.trace( " read %3d - %3d ", bytesRead, /*buf,*/ length );
    DEBUG.trace( ">>> read" );
    return bytesRead;
  }

  /**
   * Close a file
   * @return  success (C.OK) or ( error C.ERROR )
   */

  public int close( int fd )
  {
    if ( fd >= C.MAX_OPEN_FILES || fd < 0 )
    {
      return C.ERROR;
    }

    int dirPos = openTable[fd].dirPos;

    DEBUG.trace( "<<< close    : close(%d) [%d]", fd, dirPos );
  
    Directory  de = readEntryDir( dirPos );

    de.setStart( openTable[fd].firstBlock );
    de.setBytes( openTable[fd].curPos );
    writeEntryDir( dirPos, de );
    
    freeFD( fd );
    DEBUG.trace( ">>> close    : res = %d", C.OK );
    return C.OK;
  }

  
  /**
   * Delete a file (unlink)
   * <BR><FONT COLOR="red">YOU NEED TO ADD CODE</FONT>
   * @return success ( C.OK ) or ( error C.EACCES )
   */

// ------------------------------------------------------------------ 
// ***** FILL IN CODE *********************************************** 
// ------------------------------------------------------------------ 
// unlink (delete) a file 
// ------------------------------------------------------------------ 

  public int unlink( String name )                 
  {
	  
	  
	  
	  for ( int i=0; i<C.DIR_ENTRIES; i++ ){ //loops through dir
		  
		  int index = i;					// notes index for later when we rewrite to dir
		  
		  Directory de = readEntryDir( i );
		  
		  if(de.getName().equals(name)){ 	//checks name 
			  
			  int currentBlock = de.getStart();				//
			  												//use current block to get next so we don't lose it when overwriting
			  int nextBlock = readEntryFAT(currentBlock);	//
			  
			  int bytes = de.getBytes();
			  
			  for(i=0; i < (bytes/C.BLOCKSIZE)+1; i++){	//loops through number of blocks file is 
				  
				  writeEntryFAT(currentBlock, C.FREE);			 //over writes 
				  currentBlock = nextBlock;						 //move to next block
				  
			  }
			  
			  de.setName("");							//
			  de.setBytes(0);							//over writes the directory entry
			  de.setStart(0);							// 
			  writeEntryDir(index, de);					//rewrites the entry to the table
			  
			  return C.OK;
			  
		  }
		  
	  }
	  
	return C.EACCES;
  }
  
 


// ------------------------------------------------------------------ 
// ***** FILL IN CODE *********************************************** 
// ------------------------------------------------------------------ 
// Write single character to file 
//   Using file descriptor add character to file 
//   Need to (1 of below): 
//     Allocate next block in chain 
//     Read existing data block 
//   Update open file table entry 
//   Add byte to disk block 
//   Write updated disk block back to disk 
// ------------------------------------------------------------------ 

  /**
   * Write a single byte to the file
   * <BR><FONT COLOR="red">YOU NEED TO ADD CODE</FONT>
   * @return success or any error
   */

  private int writeByte( int fd, byte ch )
  {
	  Directory de = readEntryDir( openTable[fd].dirPos );		//gets the directory entry	

	  if(openTable[fd].firstBlock == C.FREE){				 //
		  													 //
		  int nb = getDiskBlock();							 //checks if new file
		  													 //and gets block
		  if(nb < 0){		  //							 //
			  return C.ERROR; //checks if run out of blocks  //
		  }					  //							 //
		  													 //
		  openTable[fd].firstBlock = nb;					 //
		  openTable[fd].curBlock = openTable[fd].firstBlock; //
		  de.setStart(nb); 									 //
	  }														 //
	  
	  int curPos = openTable[fd].curPos;    
	  int modCurPos = openTable[fd].curPos % C.BLOCKSIZE;  
	  
	  if(curPos != 0 && modCurPos == 0){						  // curPos > 1 && modCurPos == 1
		  int cb = openTable[fd].curBlock;					  //
		  int nb = getDiskBlock();							  //checks if block is full and
		  													  //gets a new block if needed
		  if(nb < 0){			//							  //then updates current block 
			  return C.ERROR; 	//checks if run out of blocks //and fat table
		  }						//							  //
		  													  //
		  openTable[fd].curBlock = nb;						  //
		  writeEntryFAT(cb,nb);								  //  
	  }														  //
	  
	  
	  int curBlock = openTable[fd].curBlock;	//gets block no
	  Block block = read(curBlock+C.DATA_START);//gets actual block

	  block.setByte(modCurPos, ch);				//writes to block
	  write(block, (curBlock+C.DATA_START));	//writes block
	  
	  openTable[fd].curPos = curPos+1;			//Increment position on open table
	  openTable[fd].bytes = openTable[fd].bytes + 1; //Increment bytes on open table
	  	
	  int bytes = de.getBytes();
	  de.setBytes(bytes+1);								//increments bytes
	  writeEntryDir(openTable[fd].dirPos, de);					//rewrites the entry to the table not sure if plus one
	  
	  return C.OK;
  }

// ------------------------------------------------------------------ 
// ***** FILL IN CODE *********************************************** 
// ------------------------------------------------------------------ 
// Read single character from file 
//   Using file descriptor  
//   Read data block containing character 
//   Get character from disk block 
//   Update open file table entry 
// ------------------------------------------------------------------ 

  /**
   * Read a single byte from the file
   * <BR><FONT COLOR="red">YOU NEED TO ADD CODE</FONT>
   * @return success or any error
   */

  private int readByte( int fd )
  { 
	  
	if(openTable[fd].curPos == openTable[fd].bytes ){
		return C.E_EOF;
	}
	  
	int curPos = openTable[fd].curPos;    								//
	int modCurPos = curPos % C.BLOCKSIZE;  								//
																		//calculates if at the end of block
	if(curPos != 0 && modCurPos == 0){									//if it is the end gets the next 
																		//block from the fat table
		openTable[fd].curBlock = readEntryFAT(openTable[fd].curBlock);	//and updates the open table
																		//
	  } 																//
	
	
	int curBlock = openTable[fd].curBlock;  	//gets block no	
	Block block = read(curBlock+C.DATA_START);	//gets actual block
	
	byte readByte = block.getByte(modCurPos); 	//read the byte
	    
    openTable[fd].curPos = curPos+1; 			//increment current pos
        
	return readByte;							//return the byte
	
  }
  
  /////////////////////////////////////////////
  //
  //analyse the files and return array of previous fat entries
  //
  ////////////////////////////////////////////
  
  public int[] analyseFileSystem(){ 		//returns an array with the previous FAT entry or -1 if the first in file
	  
	  int[] pre = new int[C.FAT_ENTRIES];	 //new array to store the previous FAT entry
	  Arrays.fill(pre, -1);
	  
	  for(int i=0; i < C.DIR_ENTRIES; i++){  //cycle through directory entrys to analyse all files 
		  
		  Directory de = readEntryDir( i ); 
		  int previous = 0;
		  int current = de.getStart(); 		 //set current to start
		  pre[current] = -1;				 //set start block previous to -1
		  
		  if(de.getBytes() > C.BLOCKSIZE ){	//check if file is multiple blocks
			  
			  for(int i2 = 0; i2 < (de.getBytes()/C.BLOCKSIZE); i2++){		//for every subsequent block
				  previous = current;								//set previous to current
				  current = readEntryFAT(previous);	//add the previous to the array in position current
				  if(current>0){
					  pre[current] = previous;
				  }
				  
			  } 
		  }
	  }
	  
	  return pre;
  }
  
  
  /////////////////////////////////
  //
  //defragment the drive
  //
  //////////////////////////////////
  
  public void defrag(){
	  
	  int nextFreeBlock = 0;
	  
	  for(int i = 0; i<C.DIR_ENTRIES; i++){
		  
		  Directory de = readEntryDir( i ); 
		  
		  if(!de.getName().equals("")){
			  
			  int currentBlock = de.getStart();				//get current and next so not lost 
			  int nextBlock = readEntryFAT(currentBlock);	//
			  
			  for(int i2 = 0; i2 < (de.getBytes()/C.BLOCKSIZE)+1; i2++){
				  
				  if(currentBlock == -2){
					  break;
				  }
				  
				  swapBlocks(currentBlock, nextFreeBlock); //swaps the current block with the next free one
				  				  
				  currentBlock = nextBlock;					//move to next block
				  
				  if(currentBlock>=0){
					  nextBlock = readEntryFAT(currentBlock);
				  }
				  
				  nextFreeBlock++;				//incriment next free block
				  
			  }//end loop through blocks
			    
		  }//end if name not blank
		  
	  } //end for looping directory entires
	  
	  
	  
  }
  
  ///////////////////////////////////
  //
  //swaps 2 blocks position
  //
  ///////////////////////////////////
  
  
  public void swapBlocks(int block1index, int block2index){
	  

	  
	  int[] pre = analyseFileSystem(); //gets previous locations
	  
	  int block1prev = pre[block1index];	///gets pevs for specific blocks
	  int block2prev = pre[block2index];

	  int block1next = readEntryFAT(block1index);	//gets nexts for specific blocks
	  int block2next = readEntryFAT(block2index);
	  
	  
	  Block block1 = read(block1index + C.DATA_START);	//read the blocks	
	  Block block2 = read(block2index + C.DATA_START);	//
	  
	  write(block1, block2index + C.DATA_START);		//write block to other location
	  write(block2, block1index + C.DATA_START);		//
	  
	  writeEntryFAT(block1index, block2next);	//overwrite the FAT entries
	  writeEntryFAT(block2index, block1next);
	  
	  if(block1prev >= 0){							//if not the start of file change prev FAT entry
		  writeEntryFAT(block1prev, block2index); 
	  }
	 
	  if(block2prev >= 0){
		  writeEntryFAT(block2prev, block1index);	
	  }  
	 
	  for(int i = 0 ; i < C.DIR_ENTRIES; i++){	//if start of file update start on directory 
		  Directory de = readEntryDir(i);
		  
		  if(de.getStart() == block1index){
			  de.setStart(block2index);
			  writeEntryDir(i, de);
		  }
		  else if(de.getStart() == block2index){
			  de.setStart(block1index);
			  writeEntryDir(i, de);
		  }
	  } 
  }
  
}
