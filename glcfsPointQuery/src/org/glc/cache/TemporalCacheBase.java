package org.glc.cache;

public class TemporalCacheBase<T> {
	protected long cache_expiry_after;
	protected volatile long last_update_time;
	protected T cachedObj;
	
	public TemporalCacheBase()
	{
		cache_expiry_after=1800000;
		last_update_time=0;
		cachedObj=null;
	}
	protected T getCache()
	{
		return this.cachedObj;
	}
	public T getCachedEntry()
	{
		long current=java.util.Calendar.getInstance().getTimeInMillis();
		if(current-last_update_time>cache_expiry_after)
			updateCache(current);
		return getCache();
	}
	protected synchronized void updateCache(final long current)
	{
		if(current-last_update_time>cache_expiry_after)
		{
			try
			{
				run();
			}
			finally
			{
				last_update_time=java.util.Calendar.getInstance().getTimeInMillis();
			}
		}
		else
			return;
	}
	protected synchronized void run()
	{
		
	}
}
