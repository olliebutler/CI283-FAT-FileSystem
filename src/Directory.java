/**
 * Directory entry:
 * Contains File name, Start block of file, size of file in bytes
 */

class Directory
{
  private String theName;   // Name of file 
  private int theStart;      // Start block number 
  private int theBytes;      // Bytes in file 

  /*** Create a populated directory entry */
  public Directory( String name, int start, int bytes )
  {
    theName = name; theStart = start; theBytes = bytes;
  }

  /*** Create an empty directory entry */
  public Directory()
  {
    theName = ""; theStart = 0; theBytes = 0;
  }

  /*** set the name of the file */
  public void setName ( String name ) { theName = name; }  
  /*** set the start block of the file */
  public void setStart( int start   ) { theStart = start; } 
  /*** set the no of bytes in the file */
  public void setBytes( int bytes   ) { theBytes = bytes; } 

  /*** return the name of the file */
  public String getName () { return theName; }
  /*** return the start block of the file */
  public int    getStart() { return theStart; }
  /*** return the no of bytes in the file */
  public int    getBytes() { return theBytes; }

}
