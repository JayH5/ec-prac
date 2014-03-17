import java.util.Arrays;
import java.util.Random;

class Chromosome implements Comparable<Chromosome> {
  /**
   * The list of cities, which are the genes of this
   * chromosome.
   */
  protected int[] cityList;

  /**
   * The cost of following the cityList order of this
   * chromosome.
   */
  protected double cost;

  /**
   * Class-wide random number generator seeded with current time.
   */
  private static final Random RNG = new Random(System.currentTimeMillis());

  private static final float CHANCE_POSITION_SWAP = .02f;

  /**
   * @param cities The order that this chromosome would
   * visit the cities.
   */
  Chromosome(City[] cities) {
    // Initialize with random ordering of cities
    // For some reason Collections.shuffle(Arrays.asList(cityList)) is not working :(
    int len = cities.length;
    cityList = new int[len];
    for (int i = 0; i < len; i++) {
      cityList[i] = i;
    }

    // Shuffle the list
    for (int i = len; i > 1; i--) {
      swap(cityList, i - 1, RNG.nextInt(i));
    }

    calculateCost(cities);
  }

  private static void swap(int[] arr, int i, int j) {
    int tmp = arr[i];
    arr[i] = arr[j];
    arr[j] = tmp;
  }

  private Chromosome(int[] cityList) {
    this.cityList = cityList;
  }

  /**
   * Calculate the cost of the specified list of cities.
   *
   * @param cities A list of cities.
   */
  void calculateCost(City[] cities) {
    cost = 0;
    for (int i = 0; i < cityList.length - 1; i++) {
      cost += cities[cityList[i]].proximity(cities[cityList[i + 1]]);
    }
  }

  /**
   * Get the cost for this chromosome. This is the
   * amount of distance that must be traveled.
   */
  double getCost() {
    return cost;
  }

  /**
   * @param i The city you want.
   * @return The ith city.
   */
  int getCity(int i) {
    return cityList[i];
  }

  /**
   * Set the order of cities that this chromosome
   * would visit.
   *
   * @param list A list of cities.
   */
  void setCities(int[] list) {
    for (int i = 0; i < cityList.length; i++) {
      cityList[i] = list[i];
    }
  }

  /**
   * Set the index'th city in the city list.
   *
   * @param index The city index to change
   * @param value The city number to place into the index.
   */
  void setCity(int index, int value) {
    cityList[index] = value;
  }

  /** Crossover recombination using the Order Crossover operator. */
  public void crossover(Chromosome parent2) {
    int len = cityList.length;

    // Choose a crossover segment between two points
    int startPos = RNG.nextInt(len);
    int endPos = RNG.nextInt(len);

    if (startPos > endPos) {
      int temp = endPos;
      endPos = startPos;
      startPos = temp;
    }

    int[] child1 = orderCrossover(cityList, parent2.cityList, startPos, endPos);
    int[] child2 = orderCrossover(parent2.cityList, cityList, startPos, endPos);
    setCities(child1);
    parent2.setCities(child2);
  }

  /**
   * Order crossover (OX-1)
   * A portion of one parent is mapped to a portion of the other parent. From
   * the replaced portion on, the rest is filled up by the remaining genes,
   * where already present genes are omitted and the order is preserved.
   * NOTE: parent1.length == parent2.length AND startPos <= endPos
   */
  private static int[] orderCrossover(int[] parent1, int[] parent2, int startPos, int endPos) {
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

  /** Mutates the chromosome by randomly swapping two city indices. */
  public void mutate() {
    int len = cityList.length;
    swap(cityList, RNG.nextInt(len), RNG.nextInt(len));
  }

  @Override
  public int compareTo(Chromosome other) {
    return Double.compare(cost, other.cost);
  }

  @Override
  public String toString() {
    return "Chromosome " + Arrays.toString(cityList) + " with cost = " + cost;
  }

}
