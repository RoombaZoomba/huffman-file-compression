import java.io.IOException;
import java.util.Arrays;

public class HuffUncompress implements IHuffConstants {

	private HuffManTree ht;
	private int[] frequencies;
	private int uncompressedAmount;

	// default constructor
	public HuffUncompress() {
		ht = null;
		frequencies = new int[ALPH_SIZE + 1];
		uncompressedAmount = 0;
	}

	// create the uncompressed file
	// params: header - type of header, outBits - write bits to file, inBits - read
	// bits from file,
	// viewer - writes to GUI
	public void buildFile(int header, BitOutputStream outBits, BitInputStream inBits,
			IHuffViewer viewer) throws IOException {

		//creating tree either from store counts or store tree
		if (header == STORE_COUNTS) {
			for (int indexFreq = 0; indexFreq < IHuffConstants.ALPH_SIZE; indexFreq++) {
				int freq = inBits.readBits(BITS_PER_INT);

				frequencies[indexFreq] = freq;
			}
			frequencies[PSEUDO_EOF]++;
			ht = new HuffManTree(frequencies);

		} else if (header == STORE_TREE) {
			int size = inBits.readBits(BITS_PER_INT);

			ht = new HuffManTree(inBits, size);
		}else {
			viewer.showError("Could not get header!.");
			throw new IOException("Not valid header"); 
		}

		uncompressedAmount = ht.readTreeAndBuildFile(outBits, inBits);
	}

	//returns the amount of bits in the uncompressed file
	public int getUncompressedAmount() {
		return uncompressedAmount;
	}

}
