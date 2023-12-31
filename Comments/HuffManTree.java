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

public class HuffManTree implements IHuffConstants {
	private PriorityQueue<TreeNode> pq;
	private TreeNode root;
	private int size;
	private final int LEAF_NODE = 1;
	private final int INTERNAL_NODE = 0;
	private final int BITS_PER_VALUE = 9;

	//constructor when passed in array of frequencies
	//for compression
	public HuffManTree(int[] frequencies) {
		root = null;
		pq = new PriorityQueue<>();
		buildPQ(frequencies);
		buildHuffManTree();
		size = getSizeRecursively(root);

	}

	//constructor when passed in BitInputStream and size
	//for decompression
	//params: inBits - reads in bits from file, size - size of tree
	public HuffManTree(BitInputStream inBits, int size) throws IOException {
		this.size = size;
		root = buildTreeRecursively(inBits);
	}

	//create the tree
	//params: in - reads in bits from file
	//returns the tree
	private TreeNode buildTreeRecursively(BitInputStream in) throws IOException {
		int intBits = in.readBits(1);
		if (intBits == INTERNAL_NODE) {
			//create internal node
			return new TreeNode(buildTreeRecursively(in), -1, buildTreeRecursively(in));
		} else if (intBits == LEAF_NODE) {
			int value = in.readBits(BITS_PER_VALUE);
			//create leaf node
			return new TreeNode(null, value, null);
		} else {
			throw new IllegalArgumentException("File is formatted incorrectly");
		}
	}

	//creates uncompressed file
	//params: out - writes bits to file, in - reads bits from file
	//Return: amount of bits written
	public int readTreeAndBuildFile(BitOutputStream out, BitInputStream in) throws IOException {
		int bitsWritten = 0;
		boolean done = false;
		TreeNode temp = root;
		while (!done) {
			int bit = in.readBits(1);
			if (bit == -1) {
				throw new IOException("Error reading compressed file. \n"
						+ "unexpected end of input. No PSEUDO_EOF value.");
				// let someone else who knows more about the problem deal with it.
			} else {
				//what happens when root is null?
				if(temp.getValue()!= -1) {
					bitsWritten += BITS_PER_WORD;
				}
				temp = updateNodeAndBuildFile(temp, out, bit);
				if (temp.getValue() == PSEUDO_EOF) {
					done = true;
				}
			}
		}
		return bitsWritten;
	}

	//traverses the tree and writes
	//params: node - current node, out - writes bits to file, bit - current bit
	private TreeNode updateNodeAndBuildFile(TreeNode node, BitOutputStream out, int bit)
			throws IOException {
		if (node.isLeaf()) {
			out.writeBits(IHuffConstants.BITS_PER_WORD, node.getValue());
			node = root;
		}
		if (bit == 0) {
			return node.getLeft();
		} else {
			return node.getRight();
		}
	}

	/**
	 * Prints a vertical representation of this tree. The tree has been rotated
	 * counter clockwise 90 degrees. The root is on the left. Each node is printed
	 * out on its own row. A node's children will not necessarily be at the rows
	 * directly above and below a row. They will be indented three spaces from the
	 * parent. Nodes indented the same amount are at the same depth. <br>
	 * pre: none
	 */
	public void printTree() {
		printTree(root, "");
	}

	private void printTree(TreeNode n, String spaces) {
		if (n != null) {
			printTree(n.getRight(), spaces + "  ");
			System.out.println(spaces + n.getValue());
			printTree(n.getLeft(), spaces + "  ");
		}
	}

	//creates the priority queue
	private void buildPQ(int[] frequencies) {
		if (frequencies == null) {
			throw new IllegalArgumentException(
					"Cannot build priorityqueue! frequencies cannot be null");
		}
		for (int index = 0; index < frequencies.length; index++) {
			if (frequencies[index] != 0) {
				TreeNode node = new TreeNode(index, frequencies[index]);
				pq.enqueue(node);
			}
		}
	}

	//creates an array where the indexes are the values and the data inside is the path
	//returns: the array
	public String[] buildMap() {
		String[] temp = new String[PSEUDO_EOF + 1];
		getAllRecursively(root, "", temp);
		return temp;
	}

	//recursively get the paths
	//params: n - current node, path - path to value, temp - array that contains the paths
	private void getAllRecursively(TreeNode n, String path, String[] temp) {
		if (n != null) {
			getAllRecursively(n.getLeft(), path + "0", temp);
			if (n.getValue() != -1) {
				temp[n.getValue()] = path;
			}
			getAllRecursively(n.getRight(), path + "1", temp);
		}

	}

	//creates the huffman tree from a priority queue
	private void buildHuffManTree() {
		if (pq.size() < 1) {
			throw new IllegalArgumentException("Cannot build HuffManTree! pq size is 0!");
		}
		while (pq.size() > 1) {
			TreeNode newNode = new TreeNode(pq.dequeue(), -1, pq.dequeue());
			pq.enqueue(newNode);
		}
		root = pq.peek();
	}

	//returns the amount of leaf nodes in the tree
	public int getLeafNodes() {
		return recursivelyGetLeafNodes(root);
	}

	//recursively traverses through the tree to get amount of leaf nodes
	//params: node - current node
	//return amount of leaf nodes currently found
	private int recursivelyGetLeafNodes(TreeNode node) {
		if (node != null) {
			if (node.getLeft() == null && node.getRight() == null) {
				return 1;
			}
			return recursivelyGetLeafNodes(node.getLeft())
					+ recursivelyGetLeafNodes(node.getRight());
		}
		return 0;
	}

	//returns the size of the tree
	public int size() {
		return size;
	}

	//gets size of the tree from starting node
	//params: node - current node
	//returns the size of the tree
	private int getSizeRecursively(TreeNode node) {
		if (node != null) {
			return getSizeRecursively(node.getLeft()) +
					getSizeRecursively(node.getRight()) + 1;
		}
		return 0;
	}

	//write the tree count header in the compressed file
	//params: outBits - write bits to file
	public void getAndSetTreeCountHeader(BitOutputStream outBits) {
		getTreeCountHeaderRecurs(root, outBits);
	}

	//writes the tree count header recursively
	//params: node - current node, outBits - writes bits to a file
	private void getTreeCountHeaderRecurs(TreeNode node, BitOutputStream outBits) {
		if (node != null) {
			if (node.getLeft() == null && node.getRight() == null) {
				outBits.writeBits(1, LEAF_NODE);
				outBits.writeBits(BITS_PER_WORD + 1, node.getValue());
			} else {
				outBits.writeBits(1, INTERNAL_NODE);
				getTreeCountHeaderRecurs(node.getLeft(), outBits);
				getTreeCountHeaderRecurs(node.getRight(), outBits);
			}
		}
	}
}
