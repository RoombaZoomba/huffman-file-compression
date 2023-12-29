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
import java.io.InputStream;
import java.io.OutputStream;

public class HuffCompress implements IHuffConstants {

	private int uncompressedAmount;
	private int compressedAmount;
	private BitInputStream bits;
	private int[] frequencies;
	private String[] map;
	private HuffManTree ht;
	private int headerFormat;

	//constructor for compressing the file
	//takes in parameters of an input stream which reads in bits
	//and the header format
	public HuffCompress(InputStream in, int headerFormat) throws IOException {
		if (in == null) {
			throw new IllegalArgumentException(
					"Cannot make HuffCompress object! in should not be null!");
		}
		this.headerFormat = headerFormat;
		uncompressedAmount = 0;
		compressedAmount = 0;
		bits = new BitInputStream(in);
		frequencies = new int[PSEUDO_EOF + 1];
		getFreqsAndUncompressedAmount();
		ht = new HuffManTree(frequencies);
		map = ht.buildMap();
	}

	//creates the frequency array and gets the number of bits in the actual file
	private void getFreqsAndUncompressedAmount() throws IOException {
		int intBits = bits.readBits(IHuffConstants.BITS_PER_WORD);
		while (intBits != -1) {
			frequencies[intBits]++;
			uncompressedAmount += BITS_PER_WORD;
			intBits = bits.readBits(IHuffConstants.BITS_PER_WORD);
		}
		frequencies[PSEUDO_EOF]++;
	}

	//set the amount of bits if storeCounts is called.
	//create the header
	//compress the file
	//add extra padding
	//post: update the amount of bits that a store counts file would return
	public void setStoreCountsAmount() {
		compressedAmount = BITS_PER_INT * 2 + ALPH_SIZE * BITS_PER_INT;
		for (int freqIndex = 0; freqIndex < frequencies.length; freqIndex++) {
			if (frequencies[freqIndex] != 0) {
				compressedAmount += frequencies[freqIndex] * map[freqIndex].length();
			}
		}
	}

	//set the amount of bits if storeTree is called.
	//create the header
	//compress the file
	//add extra padding
	//post: update the amount of bits that a store counts file would return
	public void setStoreTreeAmount() {
		compressedAmount = BITS_PER_INT * 2;
		final int LEAFNODE_VALUE_CONSTANT = 9;
		compressedAmount += BITS_PER_INT + ht.size() +
				ht.getLeafNodes() * LEAFNODE_VALUE_CONSTANT;
		for (int freqIndex = 0; freqIndex < ALPH_SIZE + 1; freqIndex++) {
			if (frequencies[freqIndex] != 0) {
				compressedAmount += frequencies[freqIndex] * map[freqIndex].length();
			}
		}
	}

	//create the compressed file
	public void buildFile(InputStream in, OutputStream out) throws IOException {
		BitOutputStream outBits = new BitOutputStream(out);
		outBits.writeBits(BITS_PER_INT, MAGIC_NUMBER);
		outBits.writeBits(BITS_PER_INT, headerFormat);
		//creates the store counts type header
		if (headerFormat == STORE_COUNTS) {
			for (int indexFreq = 0; indexFreq < IHuffConstants.ALPH_SIZE; indexFreq++) {
				outBits.writeBits(BITS_PER_INT, frequencies[indexFreq]);
			}
		} else if (headerFormat == STORE_TREE) {
			getStoreTreeHeader(outBits);
		}
		bits = new BitInputStream(in);
		int intBits = bits.readBits(BITS_PER_WORD);
		//compresses the file
		while (intBits != -1) {
			//writes the path out bit by bit
			for(int index = 0; index < map[intBits].length(); index++) {
				outBits.writeBits(1,
						(Character.getNumericValue(map[intBits].charAt(index))));
			}
			intBits = bits.readBits(IHuffConstants.BITS_PER_WORD);
		}
		//writes the path out bit by bit
		for(int index = 0; index < map[PSEUDO_EOF].length(); index++) {
			outBits.writeBits(1,
					(Character.getNumericValue(map[PSEUDO_EOF].charAt(index))));
		}
		//pads the compressed file
		outBits.flush();
	}

	//creates the store tree type header
	private void getStoreTreeHeader(BitOutputStream outBits) {
		final int LEAFNODE_VALUE_CONSTANT = 9;
		int numOfBits = ht.size() + ht.getLeafNodes() * LEAFNODE_VALUE_CONSTANT;
		outBits.writeBits(BITS_PER_INT, numOfBits);
		ht.getAndSetTreeCountHeader(outBits);
	}

	//returns amount of bits in uncompressed file
	public int getUncompressedAmount() {
		return uncompressedAmount;
	}

	//returns amount of bits in compressed file
	public int getCompressedAmount() {
		return compressedAmount;
	}
}
