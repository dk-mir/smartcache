package dk.mir.smartcache;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import dk.mir.smartcache.exceptions.CacheOverflowException;
import dk.mir.smartcache.generators.BytesGenerator;
import dk.mir.smartcache.params.Parallelized;

@RunWith(Parallelized.class)
public class MyCacheTest {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MyCacheTest.class);
	private static MyCache myCache;
	private static final Random randomizer = new Random();
	
	@Parameterized.Parameters
	public static Collection<byte[]> getTestData(){
		int files = 200;
		int maxSize = 100000;
		return BytesGenerator.getRandomBytes(files, maxSize);
	}
	
	private final byte[] putCacheBytes;
	
	public MyCacheTest(byte[] bytes){
		putCacheBytes = bytes;
	}
	
	@BeforeClass
	public static void createCache(){
		myCache = new MyCache(
			(key, data) -> {
				if(data.length>50000){
					throw new RuntimeException("No space on disk! File is too big ["+
							FileUtils.byteCountToDisplaySize(data.length)+"]");
				}else{
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						throw new RuntimeException("Saving on disk has been interrupted");
					}
				}
			},
			key -> {
				return new BigInteger("FFFFFFFFFFFFFFFFFFFF", 16).toByteArray();
			}
			,1*1024*1024);
		LOG.info("Memory assigned: {}", FileUtils.byteCountToDisplaySize( myCache.getAvailableMemory() ));
	}
	@AfterClass
	public static void printStats(){
		LOG.info("Memory remains: {}", FileUtils.byteCountToDisplaySize( myCache.getAvailableMemory() ));
	}
	
	@Test
	public void saveFile(){
		try {
			myCache.putToCache(putCacheBytes);
		} catch (CacheOverflowException e) {
			LOG.warn("Cache is full, remains {}", 
					FileUtils.byteCountToDisplaySize( myCache.getAvailableMemory()));
		}
	}
	
	@Test
	public void getFile() throws Exception{
		try{
			myCache.getFromCache(randomizer.nextInt(2000000));
		}catch(CacheOverflowException e){}
	}
}
