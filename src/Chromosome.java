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

  /**
   * Chance that a given child will be mutated.
   */
  private static final float CHANCE_MUTATION = .2f;

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

  /** Get the size of the genome (size of city list). */
  int size() {
    return cityList.length;
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

  /**
   * Perform crossover and mutation on two parent chromosomes. Mutates the
   * parent chromosomes (children replace parents directly).
   */
  public static void mate(Chromosome parent1, Chromosome parent2) {
    crossover(parent1, parent2);
    parent1.mutate();
    parent2.mutate();
  }

  /**
   * Crossover recombination using the Order Crossover operator. Parents are
   * replaced by their children.
   */
  static void crossover(Chromosome parent1, Chromosome parent2) {
    int len = parent1.size();

    // Choose a crossover segment between two points
    int startPos = RNG.nextInt(len);
    int endPos = RNG.nextInt(len);

    if (startPos > endPos) {
      int tmp = endPos;
      endPos = startPos;
      startPos = tmp;
    }

    int[] child1 = orderCrossover(parent1.cityList, parent2.cityList, startPos, endPos);
    int[] child2 = orderCrossover(parent2.cityList, parent1.cityList, startPos, endPos);
    parent1.setCities(child1);
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
    if (RNG.nextFloat() <= CHANCE_MUTATION) {
      int len = cityList.length;
      swap(cityList, RNG.nextInt(len), RNG.nextInt(len));
    }
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
