package org.glc.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.glc.Logger;
import org.glc.xmlconfig.LogLevel;

public class Encryption {
	public static byte[] MD5(String passwd)
	{
		byte[] result=null;
		if(passwd!=null)
		{
			try {
				MessageDigest md=MessageDigest.getInstance("MD5");
				md.reset();
				md.update(passwd.getBytes());
				result=md.digest();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				Logger.writeLog(e.getMessage(),LogLevel.SEVERE);
			} 
		}
		return result;
	}
}
