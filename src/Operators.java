import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class Operators {

  private static final Random RNG = new Random(System.currentTimeMillis());

  private static int[][] adjacencyMatrix;

  public static void swap(int[] arr, int i, int j) {
    int tmp = arr[i];
    arr[i] = arr[j];
    arr[j] = tmp;
  }

  public static void invert(int[] arr, int x, int y) {
    int start = Math.min(x, y);
    int end = Math.max(x, y);
    int half = (end - start) / 2;
    for (int i = 0; i < half; i++) {
      swap(arr, start + i, end - i);
    }
  }

  /**
   * Rotates the elements in the specified array by the specified distance.
   * Adapted from the method of the same name in java.util.Collections but for
   * int[] rather than List<T>.
   */
  public static void rotate(int[] arr, int distance) {
    int len = arr.length;
    if (len == 0) {
      return;
    }
    distance = distance % len;
    if (distance < 0) {
      distance += len;
    }
    if (distance == 0) {
      return;
    }

    for (int cycleStart = 0, nMoved = 0; nMoved != len; cycleStart++) {
      int displaced = arr[cycleStart];
      int i = cycleStart;
      do {
        i += distance;
        if (i >= len) {
          i -= len;
        }
        int tmp = arr[i];
        arr[i] = displaced;
        displaced = tmp;
        nMoved++;
      } while (i != cycleStart);
    }
  }

  /**
   * Move an element in the array by removing it, shifting up/down the other
   * elements and then placing the element back in the array.
   */
  public static void move(int[] arr, int src, int dest) {
    if (src == dest) {
      return;
    }

    // Grab the element to be moved
    int moved = arr[src];
    // Shift others down or up
    int shift = src < dest ? 1 : -1;
    for (int i = src; i != dest; i+= shift) {
      arr[i] = arr[i + shift];
    }
    // Place element in new position
    arr[dest] = moved;
  }

  public static void multiMove(int[] arr, int srcI, int srcJ, int dest) {
    int srcStart = Math.min(srcI, srcJ);
    int srcEnd = Math.max(srcI, srcJ);
    Boolean movingLeft = null; //boolean my ass :P
    if(dest < srcStart) movingLeft = true;
    else if(dest == srcStart) return;
    else if(dest < srcEnd) throw new IllegalArgumentException("Destination must not be within source interval");
    else if(dest == srcEnd) return;
    else movingLeft = false;

    //interval to be moved
    int movedSize = srcEnd - srcStart;
    int[] moved = new int[movedSize];
    for(int i = 0; i < movedSize; i++) {
      moved[i] = arr[srcStart + i];
    }
    int movedTarget = -1;
    int shiftedTarget = -1;
    int shifted[];
    if(movingLeft) {
      int shiftedSize = srcStart - dest;
      shifted = new int[shiftedSize];
      for(int i = 0; i < shiftedSize; i++) {
       shifted[i] = arr[dest + i];
      }
      movedTarget = dest;
      shiftedTarget = dest + movedSize;
    } else {
      int shiftedSize = dest - srcEnd;
      shifted = new int[shiftedSize];
      for(int i = 0; i < shiftedSize; i++) {
       shifted[i] = arr[srcEnd + i];
      }
      movedTarget = dest - movedSize;
      shiftedTarget = srcStart;
    }
    for(int i = 0; i < moved.length; i++) {
      arr[movedTarget + i] = moved[i];
    }
    for(int i = 0; i < shifted.length; i++) {
      arr[shiftedTarget + i] = shifted[i];
    }
    /*
    if(movingLeft) {
      for(int i = 0; i < srcStart - dest; i++) {
        arr[srcEnd -i] = arr[srcStart - i];
      }
      for(int i = 0; i < moved.length; i++) {
        arr[dest + i] = moved[i];
      }
    } else {
      for(int i = 0; i < dest - srcEnd; i++) {
        arr[srcStart + i] = arr[srcEnd + i];
      }
      for(int i = 0; i < moved.length; i++) {
        arr[dest - i] = moved[moved.length -1 - i];
      }
    }
    */
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
// vim: ts=2:sw=2
