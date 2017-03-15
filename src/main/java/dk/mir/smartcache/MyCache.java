package dk.mir.smartcache;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;

import dk.mir.smartcache.exceptions.CacheOverflowException;
/**
 * Cache with the memory limit and if exceeded {@linkplain CacheOverflowException}
 * is thrown. <strong>Cache invalidation is not implemented!</strong>
 * @author Kalinin_DP
 *
 */
public class MyCache {
	
	private AtomicInteger keyCounter = new AtomicInteger();
	
	private final AtomicLong memory;
	
	private ExecutorService exec = Executors.newFixedThreadPool(5);
	
	private final ConcurrentMap<Integer, Future<byte[]>> cache
		= new ConcurrentHashMap<Integer, Future<byte[]>>();
	
	private final Function<String, byte[]> loadFunc;
	private final BiConsumer<String, byte[]> saveFunc;
	/**
	 * Creates cache with default load and save methods and memory of 100 Mb. 
	 */
	public MyCache(){
		this.loadFunc = this::loadFromFile;
		this.saveFunc = this::saveToFile;
		// 100 Mb
		memory = new AtomicLong(100 * 1024 * 1024);
	}
	/**
	 * Constructor to provide custom functions to <code>load</code> and <code>save</code>
	 * data to disk and set custom memory limit; 
	 * @param memoryLimit in bytes
	 */
	public MyCache(long memoryLimit){
		this.loadFunc = this::loadFromFile;
		this.saveFunc = this::saveToFile;
		this.memory = new AtomicLong(memoryLimit);
	}
	/**
	 * Constructor to provide custom function to load and save data to disk
	 * @param saveFunc
	 * @param loadFunc
	 */
	public MyCache(
			BiConsumer<String, byte[]> saveFunc,
			Function<String, byte[]> loadFunc){
		this.loadFunc = loadFunc;
		this.saveFunc = saveFunc;
		memory = new AtomicLong(100 * 1024 * 1024);
	}
	/**
	 * 
	 * @param saveFunc
	 * @param loadFunc
	 * @param memoryLimit in bytes
	 */
	public MyCache(
			BiConsumer<String, byte[]> saveFunc,
			Function<String, byte[]> loadFunc,
			long memoryLimit){
		
		this.loadFunc = loadFunc;
		this.saveFunc = saveFunc;
		memory = new AtomicLong(memoryLimit);
	}
	
	public byte[] loadFromFile(String key){
		//return 10 bytes
		return new BigInteger("FFFFFFFFFFFFFFFFFFFF", 16).toByteArray();
	}
	
	public void saveToFile(String filename, byte[] data){
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException("Failed to save on disk", e.getCause());
		}
	}
	/**
	 * 
	 * @param key
	 * @return
	 * @throws CacheOverflowException if cache is full and can't load the data in memory
	 * @throws Exception
	 */
	public byte[] getFromCache(int key) throws Exception{
		if(memory.get()<=0) throw new CacheOverflowException();
		Future<byte[]> futureBytes = cache.get(key);
		boolean addMemoryCounter = false;
		if(futureBytes == null){//data is not in cache, load from disk
			addMemoryCounter = true; //adjust available memory when data becomes known
			FutureTask<byte[]> ft = new FutureTask<byte[]>( () -> {
				return loadFunc.apply(Integer.toString(key));
			});
			futureBytes = cache.putIfAbsent(key, ft);
			if(futureBytes == null){ //i.e. hasn't been added by other threads yet
				futureBytes = ft;
				ft.run();
			}
		}
		try {
			byte[] data = futureBytes.get();
			if(addMemoryCounter){//the data is loaded from disk, decrease memory
				long memoryRemains = memory.addAndGet(-data.length);
				if(memoryRemains < 0) throw new CacheOverflowException();
			}
			return data;
		} catch (InterruptedException e) {
			cache.remove(key, futureBytes);
			throw new Exception("Interrupted");
		} catch (ExecutionException e) {
			throw new Exception(e.getCause());
		}
	}
	
	public int putToCache(byte[] value) throws CacheOverflowException{
		if(memory.get() - value.length < 0) throw new CacheOverflowException();
		int key = keyCounter.incrementAndGet();
		CompletableFuture<byte[]> savedBytesFuture = CompletableFuture.<byte[]>supplyAsync( ()-> {
				saveFunc.accept(Integer.toString(key), value);
				return value;
			}, exec)
		.handle( (ok, ex) -> {
			if(ok!=null){
				return ok;
			}else{
				System.err.println(ex.getMessage());
				//remove from cache if failed to write on disk
				cache.remove(key);
				memory.addAndGet(value.length);
				return new byte[]{};
			}
		});
		long memoryRemains = memory.addAndGet(-value.length);
		if( memoryRemains < 0 ) throw new CacheOverflowException();
		cache.put(key, savedBytesFuture);
		return key;
	}
	
	public long getAvailableMemory(){
		return memory.get();
	}
}
