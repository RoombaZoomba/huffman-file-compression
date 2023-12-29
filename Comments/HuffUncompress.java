/*  Student information for assignment:
 *
 *  On <OUR> honor, <Mohammad Hakim> and <Nathan Cheng>, this programming assignment is <OUR>
 *  own work and <WE> have not provided this code to any other student.
 *
 *  Number of slip days used:
 *
 *  Student 1 (Student whose turnin account is being used)
 *  UTEID:
 *  email address:
 *  Grader name:
 *
 *  Student 2
 *  UTEID: msh3573
 *  email address: mohammad_hakim@utexas.edu
 *
 */

import java.io.IOException;

public class HuffUncompress implements IHuffConstants {
	private HuffManTree ht;
	private int[] frequencies;
	private int uncompressedAmount;

	//constructor
	public HuffUncompress() {
		ht = null;
		frequencies = new int[ALPH_SIZE + 1];
		uncompressedAmount = 0;
	}

	//create the uncompressed file
	//params: header - type of header, outBits - write bits to file, inBits - read bits from file,
	// viewer - writes to GUI
	public void buildFile(int header, BitOutputStream outBits, BitInputStream inBits,
						  IHuffViewer viewer) throws IOException {
		//create header
		if (header == STORE_COUNTS) {
			for (int indexFreq = 0; indexFreq < IHuffConstants.ALPH_SIZE; indexFreq++) {
		        int freq = inBits.readBits(BITS_PER_INT);
		        frequencies[indexFreq] = freq;
		    }
		    ht = new HuffManTree(frequencies);
		} else if (header == STORE_TREE) {
			buildTreeByStoreTree(inBits);
		}
		//uncompress data
		uncompressedAmount = ht.readTreeAndBuildFile(outBits,inBits);
	}

	//create store tree format header
	//params: inBits - reads in bits from file
	public void buildTreeByStoreTree(BitInputStream inBits) throws IOException {
		//get size of tree
		int size = inBits.readBits(BITS_PER_INT);
		//create the tree
		ht = new HuffManTree(inBits, size);
	}

	public int getUncompressedAmount() {
		return uncompressedAmount;
	}
}
