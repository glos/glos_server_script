package org.glc;

import java.util.HashMap;

import org.glc.Logger;
import org.glc.xmlconfig.ConfigManager;
import org.glc.xmlconfig.LogLevel;

public class DefaultLiteralProvider implements ILiteralProvider {

	static final String KEY_SEPARATOR="KEY_SEPARATOR";
	static final String KEYS="KEYS";
	HashMap<String,String> map;
	private static DefaultLiteralProvider provider=null;
	static
	{
		String temp=null;
		temp=ConfigManager.getProviderParam(DefaultLiteralProvider.class.getName(),KEY_SEPARATOR);
		if(temp!=null&&!temp.equals(""))
		{
			provider=new DefaultLiteralProvider();
		}
	}
	private DefaultLiteralProvider()
	{
		map=new HashMap<String,String>();
		String sep=ConfigManager.getProviderParam(this.getClass().getName(),KEY_SEPARATOR);
		String keys=ConfigManager.getProviderParam(this.getClass().getName(),KEYS);
		if(keys!=null&&!keys.equals(""))
		{
			String[] temp=keys.split("\\"+sep);
			if(temp!=null&&temp.length>0)
				for(String key:temp)
					if(!key.trim().equals(""))
					{
						key=key.trim().replaceAll("\n", "").replaceAll("\r", "");
						
						this.map.put(key.trim(), ConfigManager.getProviderParam(this.getClass().getName(),key));
					}
			
		}
	}
	@Override
	public String getText(String key) {
		// TODO Auto-generated method stub
		if(provider!=null)
			return provider.map.get(key);
		else
			return null;
	}
	
	public static ILiteralProvider getInstance() {
		// TODO Auto-generated method stub
		return provider;
	}
	
}
