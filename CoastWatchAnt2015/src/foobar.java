
import java.io.*;

import java.nio.channels.*;



public class foobar {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try
		{
			RandomAccessFile lock=new RandomAccessFile("/tmp/"+"foobar.lck","rw");
			FileChannel fChannel=lock.getChannel();//fos.getChannel();
			FileLock fLock=fChannel.tryLock();
			if(fLock==null)
			{
				System.out.println("locked!");
				return;
			}
			System.in.read();
			fLock.release();
			lock.close();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
