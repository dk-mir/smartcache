package dk.mir.smartcache.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.RandomUtils;

public class BytesGenerator {
	
	private static final Random randomizer = new Random();
	/**
	 * Creates list of random file data.
	 * @param files how many files to create
	 * @param maxSize maximum size of a file
	 * @return list of byte arrays of data (files' bytes)
	 */
	public static List<byte[]> getRandomBytes(int files, int maxSize){
		List<byte[]> cacheBytes = new ArrayList<>(files);
		while(files>0){
			files --;
			int bytes = (int)(randomizer.nextFloat()*maxSize);
			cacheBytes.add(RandomUtils.nextBytes(bytes));
		}
		return cacheBytes;
	}

}
