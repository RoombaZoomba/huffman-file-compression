
/*  Student information for assignment:
 *
 *  On <OUR> honor, <Mohammad Hakim> and <Nathan Cheng>, this programming assignment is <OUR>
 *  own work and <WE> have not provided this code to any other student.
 *
 *  Number of slip days used:1
 *
 *  Student 1 (Student whose turnin account is being used)
 *  UTEID:nyc278
 *  email address:nathanchengus@gmail.com
 *  Grader name:David K
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

	// constructor for compressing the file
	// takes in parameters of an input stream which reads in bits
	// and the header format
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

	// creates the frequency array and gets the number of bits in the actual file
	// pre: none
	// post: update frequencies to get right amount of occurances for each
	// characters
	private void getFreqsAndUncompressedAmount() throws IOException {
		int intBits = bits.readBits(IHuffConstants.BITS_PER_WORD);

		while (intBits != -1) {
			frequencies[intBits]++;

			uncompressedAmount += BITS_PER_WORD;
			intBits = bits.readBits(IHuffConstants.BITS_PER_WORD);
		}
		frequencies[PSEUDO_EOF]++;
	}

	// set the amount of bits if storeCounts is called.
	// create the header
	// compress the file
	// add extra padding
	// post: update the amount of bits that a store counts file would return
	// pre: none
	// post: update the amount of bits that a store counts file would return
	public void setStoreCountsAmount() {
		compressedAmount = BITS_PER_INT * 2 + ALPH_SIZE * BITS_PER_INT;

		for (int freqIndex = 0; freqIndex < frequencies.length; freqIndex++) {
			if (frequencies[freqIndex] != 0) {

				compressedAmount += frequencies[freqIndex] * map[freqIndex].length();
			}
		}
	}

	// set the amount of bits if storeTree is called.
	// create the header
	// compress the file
	// add extra padding
	// pre: none
	// post: update the amount of bits that a store counts file would return
	public void setStoreTreeAmount() {
		compressedAmount = BITS_PER_INT * 2;

		final int LEAFNODE_VALUE_CONSTANT = 9;

		compressedAmount += BITS_PER_INT + ht.size() + ht.getLeafNodes() * LEAFNODE_VALUE_CONSTANT;

		for (int freqIndex = 0; freqIndex < ALPH_SIZE + 1; freqIndex++) {
			if (frequencies[freqIndex] != 0) {

				compressedAmount += frequencies[freqIndex] * map[freqIndex].length();
			}
		}

	}

	// create the compressed file based on header format
	// pre: none, called privately
	// post: writes the compressed file
	public void buildFile(InputStream in, OutputStream out, IHuffViewer viewer) throws IOException {

		BitOutputStream outBits = new BitOutputStream(out);
		outBits.writeBits(BITS_PER_INT, MAGIC_NUMBER);
		outBits.writeBits(BITS_PER_INT, headerFormat);

		// Creating the specified headerformat
		if (headerFormat == STORE_COUNTS) {
			for (int indexFreq = 0; indexFreq < IHuffConstants.ALPH_SIZE; indexFreq++) {
				outBits.writeBits(BITS_PER_INT, frequencies[indexFreq]);
			}
		} else if (headerFormat == STORE_TREE) {

			getStoreTreeHeader(outBits);
		} else {
			viewer.showError("Could not get header!.");
			outBits.close();
		}

		bits = new BitInputStream(in);

		// getting the new huffcode and writing bits, one by one
		int intBits = bits.readBits(BITS_PER_WORD);
		while (intBits != -1) {
			String str = map[intBits];
			for (int index = 0; index < str.length(); index++) {
				outBits.writeBits(1, (int) str.charAt(index));
			}
			intBits = bits.readBits(IHuffConstants.BITS_PER_WORD);
		}

		String str = map[PSEUDO_EOF];
		for (int index = 0; index < str.length(); index++) {
			outBits.writeBits(1, (int) str.charAt(index));
		}

		outBits.flush();
		outBits.close();
		bits.close();

	}

	// creates the store tree type header, writes size and representation of tree
	// pre: none, called privately
	// post: write out tree header
	private void getStoreTreeHeader(BitOutputStream outBits) {

		final int LEAFNODE_VALUE_CONSTANT = 9;
		int numOfBits = ht.size() + ht.getLeafNodes() * LEAFNODE_VALUE_CONSTANT;

		outBits.writeBits(BITS_PER_INT, numOfBits);
		ht.getAndSetTreeCountHeader(outBits);

	}

	// reset amount of compressed file
	// pre: none
	// post: set compressed amount to 0
	public void resetCompressedAmount() {
		compressedAmount = 0;
	}

	// returns amount of bits in uncompressed file
	// pre:none
	// post: return uncompressed amount
	public int getUncompressedAmount() {
		return uncompressedAmount;
	}

	// returns amount of bits in compressed file
	// pre:none
	// post: return compressed amount
	public int getCompressedAmount() {
		return compressedAmount;
	}

}
