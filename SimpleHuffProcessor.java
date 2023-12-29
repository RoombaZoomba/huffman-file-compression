
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SimpleHuffProcessor implements IHuffProcessor {

	private IHuffViewer myViewer;
	private HuffCompress compresser;

	/**
	 * Preprocess data so that compression is possible --- count characters/create
	 * tree/store state so that a subsequent call to compress will work. The
	 * InputStream is <em>not</em> a BitInputStream, so wrap it int one as needed.
	 * 
	 * @param in           is the stream which could be subsequently compressed
	 * @param headerFormat a constant from IHuffProcessor that determines what kind
	 *                     of header to use, standard count format, standard tree
	 *                     format, or possibly some format added in the future.
	 * @return number of bits saved by compression or some other measure Note, to
	 *         determine the number of bits saved, the number of bits written
	 *         includes ALL bits that will be written including the magic number,
	 *         the header format number, the header to reproduce the tree, AND the
	 *         actual data.
	 * @throws IOException if an error occurs while reading from the input file.
	 */
	public int preprocessCompress(InputStream in, int headerFormat) throws IOException {

		compresser = new HuffCompress(in, headerFormat);
		// amount of bits per header
		if (headerFormat == STORE_COUNTS) {
			compresser.setStoreCountsAmount();
		} else if (headerFormat == STORE_TREE) {
			compresser.setStoreTreeAmount();
		} else {
			myViewer.update("Preprocess only handles store counts and store tree");
			return -1;
		}
		int diff = compresser.getUncompressedAmount() - compresser.getCompressedAmount();
		myViewer.update("Preprocess completed!  Saved " + diff + " bits!");
		in.close();
		return diff;
	}

	/**
	 * Compresses input to output, where the same InputStream has previously been
	 * pre-processed via <code>preprocessCompress</code> storing state used by this
	 * call. <br>
	 * pre: <code>preprocessCompress</code> must be called before this method
	 * 
	 * @param in    is the stream being compressed (NOT a BitInputStream)
	 * @param out   is bound to a file/stream to which bits are written for the
	 *              compressed file (not a BitOutputStream)
	 * @param force if this is true create the output file even if it is larger than
	 *              the input file. If this is false do not create the output file
	 *              if it is larger than the input file.
	 * @return the number of bits written.
	 * @throws IOException if an error occurs while reading from the input file or
	 *                     writing to the output file.
	 */
	public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
		if (compresser == null) {
			throw new IllegalArgumentException(
					"Cannot compress! preprocessCompress was never called beforehand!");
		}

		if (force || (compresser.getCompressedAmount() <= compresser.getUncompressedAmount())) {
			// create the file
			compresser.buildFile(in, out, myViewer);
		} else {
			compresser.resetCompressedAmount();
			myViewer.showError("Compressed file has "
					+ (compresser.getCompressedAmount() - compresser.getUncompressedAmount())
					+ " more bits than uncompressed file. Select "
					+ "\"force compression\" option to compress.");
		}

		myViewer.update("Compress completed! Compressed file into "
				+ compresser.getCompressedAmount() + " bits.");
		in.close();
		out.close();
		return compresser.getCompressedAmount();
	}

	/**
	 * Uncompress a previously compressed stream in, writing the uncompressed
	 * bits/data to out.
	 * 
	 * @param in  is the previously compressed data (not a BitInputStream)
	 * @param out is the uncompressed file/stream
	 * @return the number of bits written to the uncompressed file/stream
	 * @throws IOException if an error occurs while reading from the input file or
	 *                     writing to the output file.
	 */
	public int uncompress(InputStream in, OutputStream out) throws IOException {
		BitOutputStream outBits = new BitOutputStream(out);
		BitInputStream inBits = new BitInputStream(in);

		HuffUncompress uncompresser = new HuffUncompress();

		int magic = inBits.readBits(BITS_PER_INT);
		if (magic != MAGIC_NUMBER) {
			myViewer.showError("There is no magic number!.");
			in.close();
			out.close();
			inBits.close();
			outBits.close();
			return -1;

		}

		int header = inBits.readBits(BITS_PER_INT);
		// uncompresses file
		uncompresser.buildFile(header, outBits, inBits, myViewer);

		myViewer.update("Uncompress completed! The number of bits written is "
				+ uncompresser.getUncompressedAmount() + " bits.");
		in.close();
		out.close();
		return uncompresser.getUncompressedAmount();
	}

	// sets viewer with given viewer
	// pre: viewer !=null
	// post: sets myviewer with viewer
	public void setViewer(IHuffViewer viewer) {
		if (viewer == null) {
			throw new IllegalArgumentException("viewer should not be null!");
		}
		myViewer = viewer;
	}

	// Updates viewer with string
	// pre:s!=null
	// post: updates myviewer
	private void showString(String s) {

		if (s == null) {
			throw new IllegalArgumentException("s should not be null!");
		}
		if (myViewer != null) {
			myViewer.update(s);
		}
	}
}
