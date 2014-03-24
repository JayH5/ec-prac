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
   * @param cities The order that this chromosome would
   * visit the cities.
   */
  Chromosome(City[] cities, Random rand) {
    // Initialize with random ordering of cities
    // For some reason Collections.shuffle(Arrays.asList(cityList)) is not working :(
    int len = cities.length;
    cityList = new int[len];
    for (int i = 0; i < len; i++) {
      cityList[i] = i;
    }

    // Shuffle the list
    for (int i = len; i > 1; i--) {
      Operators.swap(cityList, i - 1, rand.nextInt(i));
    }

    calculateCost(cities);
  }

  Chromosome(int[] cityList) {
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

  public int[] getCityList() {
    return cityList;
  }

  @Override
  public int compareTo(Chromosome other) {
    return Double.compare(cost, other.cost);
  }

  @Override
  public String toString() {
    return "Chromosome " + Arrays.toString(cityList) + " with cost = " + cost;
  }

  public static class Children {
    final Chromosome child1;
    final Chromosome child2;

    Children(int[] child1, int[] child2) {
      this.child1 = new Chromosome(child1);
      this.child2 = new Chromosome(child2);
    }

    void calculateCost(City[] cities) {
      child1.calculateCost(cities);
      child2.calculateCost(cities);
    }
  }

}
// vim: ts=2:sw=2
