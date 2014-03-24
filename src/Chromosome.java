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
  private static final float CHANCE_MUTATION = .25f;

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
      Operators.swap(cityList, i - 1, RNG.nextInt(i));
    }

    calculateCost(cities);
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
   * Perform crossover with two parents and mutate the children.
   */
  public static Children mate(Chromosome parent1, Chromosome parent2) {
    Children children = crossover(parent1, parent2);
    children.mutate();
    return children;
  }

  /**
   * Crossover recombination using the Order Crossover operator.
   */
  static Children crossover(Chromosome parent1, Chromosome parent2) {
    int len = parent1.size();

    // Choose a crossover segment between two points
    int startPos = RNG.nextInt(len);
    int endPos = startPos + RNG.nextInt(len - startPos);

    int[] child1 = Operators.edgeRecombination(parent1.cityList, parent2.cityList);
    int[] child2 = Operators.edgeRecombination(parent2.cityList, parent1.cityList);
    return new Children(child1, child2);
  }

  /** Mutates the chromosome by randomly swapping two city indices. */
  public void mutate() {
    if (RNG.nextFloat() <= CHANCE_MUTATION) {
      /*
      if(RNG.nextFloat() <= 0.5) {
        int len = cityList.length;
        int startPos = RNG.nextInt(len);
        int endPos = RNG.nextInt(len);
        Operators.move(cityList, startPos, endPos);
      }
      */
      //if(RNG.nextFloat() <= 0.5) {
        int len = cityList.length;
        int startPos = RNG.nextInt(len);
        int endPos = RNG.nextInt(len);
        Operators.invert(cityList, startPos, endPos);
      //}
      /*
         multi-swap mutation
        int len = cityList.length;
        int x = RNG.nextInt(len);
        int y = RNG.nextInt(len);
        int z = RNG.nextInt(len);

        int a = Math.min(x, Math.min(y, z));
        int c = Math.max(x, Math.max(y, z));
        int b = Math.min(Math.max(x,y), Math.min(Math.max(y,z), Math.max(x,z)));

        Integer intEnd = null;
        Integer dest = null;
        if(RNG.nextBoolean()) {
          Operators.multiMove(cityList, a, b, c);
        } else {
          Operators.multiMove(cityList, b, c, a);
        }
        */
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

  static class Children {
    final Chromosome child1;
    final Chromosome child2;

    Children(int[] child1, int[] child2) {
      this.child1 = new Chromosome(child1);
      this.child2 = new Chromosome(child2);
    }

    void mutate() {
      child1.mutate();
      child2.mutate();
    }

    void calculateCost(City[] cities) {
      child1.calculateCost(cities);
      child2.calculateCost(cities);
    }
  }

}
// vim: ts=2:sw=2
