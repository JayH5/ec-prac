import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EdgeRecombinationCrossover {

  private final int[][] matrix;
  private final Random rand;

  public EdgeRecombinationCrossover(int len, Random rand) {
    matrix = new int[len][];
    for (int i = 0; i < len; i++) {
      matrix[i] = new int[5];
    }
    this.rand = rand;
  }

  /**
   * Edge recombination! Read the wiki:
   * http://en.wikipedia.org/wiki/Edge_recombination_operator
   * NOTE: parent1.length == parent2.length AND must be greater than 0
   * Implementation detail: -1 is used to signify a deleted or unset node.
   */
  public int[] crossover(int[] parent1, int[] parent2) {
    // Fill with adjacent nodes
    fillAdjacencyMatrix(parent1, parent2);

    // Create union of adjacent nodes by marking duplicates as -1
    removeDuplicateAdjacentNodes();

    // Build the child path
    int len = parent1.length;
    int[] child = new int[len];
    Arrays.fill(child, -1); // Since 0 is a valid node, fill with -1
    int node = parent1[0];
    for (int i = 0; i < len - 1; i++) {
      child[i] = node;
      removeFromAllNeighbours(node);

      if (hasNeighbours(node)) {
        node = neighbourWithFewestNeigbours(node);
      } else {
        node = randomNodeNotInChild(child);
      }
    }
    child[len - 1] = node;

    return child;
  }

  /** Fill the adjacency matrix using two parent genes */
  private void fillAdjacencyMatrix(int[] parent1, int[] parent2) {
    int len = matrix.length;
    for (int i = 0; i < len; i++) {
      int parent1Node = parent1[i];
      int parent2Node = parent2[i];

      int pos = i + 1;
      if (pos >= len) {
        pos -= len;
      }
      matrix[parent1Node][0] = parent1[pos];
      matrix[parent2Node][1] = parent2[pos];

      pos = i - 1;
      if (pos < 0) {
        pos += len;
      }
      matrix[parent1Node][2] = parent1[pos];
      matrix[parent2Node][3] = parent2[pos];

      // Optimization: use the 5th element to store the number of neighbours
      matrix[i][4] = 4;
    }
  }

  /** Mark duplicate adjacent nodes as -1 in the matrix. */
  private void removeDuplicateAdjacentNodes() {
    for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < 4; j++) {
        int node = matrix[i][j];
        if (node > -1) {
          for (int c = j + 1; c < 4; c++) {
            if (matrix[i][c] == node) {
              matrix[i][c] = -1;
              matrix[i][4]--;
            }
          }
        }
      }
    }
  }

  /** Remove every instance of node from the adjacency matrix. */
  private void removeFromAllNeighbours(int node) {
    for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < 4; j++) {
        if (matrix[i][j] == node) {
          matrix[i][j] = -1;
          matrix[i][4]--;
        }
      }
    }
  }

  /** Check if a list of neighbours has any valid ones. */
  private boolean hasNeighbours(int node) {
    return matrix[node][4] > 0;
  }

  /**
   * Find the neighbour with the fewest neighbours. Pick a random one if more
   * than one.
   */
  private int neighbourWithFewestNeigbours(int node) {
    // Let next node be neighbour of current node with fewest neighbors in
    // its list
    List<Integer> neighboursWithFewestNeighbours = new ArrayList<Integer>(4);
    int fewestNeighbours = 4;
    for (int i = 0; i < 4; i++) {
      int neighbour = matrix[node][i];
      if (neighbour > -1) {
        // Get number of neighbours for this neighbour
        int neighbourCount = matrix[neighbour][4];

        if (neighbourCount == fewestNeighbours) {
          neighboursWithFewestNeighbours.add(neighbour);
        } else if (neighbourCount < fewestNeighbours) {
          fewestNeighbours = neighbourCount;
          neighboursWithFewestNeighbours.clear();
          neighboursWithFewestNeighbours.add(neighbour);
        }
      }
    }

    int num = neighboursWithFewestNeighbours.size();
    return neighboursWithFewestNeighbours.get(rand.nextInt(num));
  }

  /**
   * Pick a random node (assuming node values range between 0 - child.length)
   * that is not already in the child gene.
   * NOTE: If the child is already full of valid nodes this will loop forever
   */
  private int randomNodeNotInChild(int[] child) {
    int len = child.length;
    int randomNode;
    do {
      randomNode = rand.nextInt(len);
    } while (contains(child, randomNode));
    return randomNode;
  }

  /** Check if an array contains a value. */
  private static boolean contains(int[] arr, int val) {
    for (int i = 0; i < arr.length; i++) {
      if (arr[i] == val) {
        return true;
      }
    }
    return false;
  }
}