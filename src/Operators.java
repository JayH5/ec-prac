public final class Operators {

  public static void swap(int[] arr, int i, int j) {
    int tmp = arr[i];
    arr[i] = arr[j];
    arr[j] = tmp;
  }

  public static void invert(int[] arr, int start, int end) {
    int half = (end - start) / 2;
    for (int i = 0; i < half; i++) {
      swap(arr, start + i, end - i);
    }
  }

  /**
   * Order crossover (OX-1)
   * A portion of one parent is mapped to a portion of the other parent. From
   * the replaced portion on, the rest is filled up by the remaining genes,
   * where already present genes are omitted and the order is preserved.
   * NOTE: parent1.length == parent2.length AND startPos <= endPos
   */
  public static int[] orderCrossover(int[] parent1, int[] parent2, int startPos, int endPos) {
    final int len = parent1.length;
    // Copy in the points of the segment from parent 1
    int[] child = new int[len];
    for (int i = startPos; i < endPos; i++) {
      child[i] = parent1[i];
    }

    // Copy in remaining points from parent 2
    int insertPos = 0;
    outer:
    for (int i = 0; i < len; i++) {
      // Skip past those points copied from parent 1
      if (insertPos == startPos) {
        insertPos = endPos;
      }

      // Try copy in a point from parent 2
      int element = parent2[i];
      // Check if point is already in those copied from parent 1
      for (int j = startPos; j < endPos; j++) {
        if (child[j] == element) {
          continue outer;
        }
      }

      // If point not in child, add it and increment pointer to next insertion
      child[insertPos] = element;
      insertPos++;
    }
    return child;
  }

}
