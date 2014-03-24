import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Simulation {

  /**
   * Chance that a given child will be mutated.
   */
  private static final float CHANCE_MUTATION = .25f;

  private static final int GENERATIONS = 1000;
  private static final int HISTORY_SIZE = 200;

  private Listener listener;

  private boolean stop = false;

  private final int cityCount;
  private final int populationSize;
  private final int parentPoolSize;

  private final City[] cities;
  private final Chromosome[] chromosomes;

  // Per-simulation random generator
  private final Random rand = new Random(System.currentTimeMillis());

  /**
   * The cost at GENERATIONS generations; the simulation keeps going, but this value is recorded.
   */
  private Double endCost;

  public Simulation(int cityCount, int populationSize, int parentPoolSize) {
    this.cityCount = cityCount;
    this.populationSize = populationSize;
    this.parentPoolSize = parentPoolSize;

    cities = new City[cityCount];
    chromosomes = new Chromosome[populationSize];
  }

  public RunResult simulate() {
    initializeCitiesAndChromosomes();

    // Initialize data structures for GA and stats
    Set<Integer> parentPool = new HashSet<Integer>();
    List<Chromosome> childPool = new ArrayList<Chromosome>();
    Queue<Double> convergenceHistory = new LinkedList<Double>();
    Queue<Long> timingHistory = new LinkedList<Long>();

    EdgeRecombinationCrossover edgeRecombination = new EdgeRecombinationCrossover(cityCount, rand);

    long genStartTime = -1;
    RunResult result = new RunResult();

    NumberFormat doubf = NumberFormat.getInstance();
    doubf.setMinimumFractionDigits(0);
    doubf.setMaximumFractionDigits(2);
    NumberFormat intf = NumberFormat.getInstance();
    intf.setMaximumFractionDigits(0);
    intf.setGroupingUsed(false);

    setStatus("Simulation starting up...");

    int generation = 0;
    for (; generation <= GENERATIONS; generation++) {
      // Stop if we've been asked to
      if(stop) {
        stop = false;
        return result;
      }

      // Record the start
      genStartTime = System.currentTimeMillis();

      evolve(parentPool, childPool, edgeRecombination);

      // Sort the new list of chromosomes
      Arrays.sort(chromosomes);
      result.cost = chromosomes[0].getCost();

      //convergence detection
      if (convergenceHistory.size() == HISTORY_SIZE) {
        convergenceHistory.remove();
      }
      convergenceHistory.add(result.cost);
      if (convergenceHistory.size() == HISTORY_SIZE) {
        double sum = 0;
        for (double i : convergenceHistory) {
          sum += i;
        }
        double avg = sum / convergenceHistory.size();
        double sumOfSquares = -1;
        for(double i : convergenceHistory) {
          double diff = Math.abs(i - avg);
          sumOfSquares += diff * diff;
        }
        double stdDev = Math.sqrt(sumOfSquares);
        if(stdDev < 50) {
          //setStatus("Converged at generation " + generation + " with cost " +  intf.format(result.cost) + "; Rate: " + doubf.format(result.rate));
          result.convergenceGen = generation;
        }
      }

      //evolution rate calculation
      long currentGenTime = System.currentTimeMillis() - genStartTime;
      if(timingHistory.size() == HISTORY_SIZE) {
        timingHistory.remove();
      }
      timingHistory.add(currentGenTime);
      double sum = 0;
      for (double i : timingHistory) {
        sum += i;
      }
      result.rate = (timingHistory.size() * 1000.0) / sum;

      if(generation == 1000) {
        endCost = result.cost;
      }
      if(endCost != null) {
        setStatus("Generation " + generation + " Cost " + intf.format(result.cost) + " Rate " + doubf.format(result.rate) + " Cost at 1000: " + intf.format(endCost));
      } else {
        setStatus("Generation " + generation + " Cost " + intf.format(result.cost) + " Rate " + doubf.format(result.rate));
      }
    }
    return result;
  }

  public City[] getCities() {
    return cities;
  }

  public Chromosome[] getChromosomes() {
    return chromosomes;
  }

  private void initializeCitiesAndChromosomes() {
    for (int i = 0; i < cityCount; i++) {
      cities[i] = new City(rand.nextDouble(), rand.nextDouble());
    }
    for (int i = 0; i < populationSize; i++) {
      chromosomes[i] = new Chromosome(cities, rand);
    }
    Arrays.sort(chromosomes);
  }

  private void evolve(Set<Integer> parentPool, List<Chromosome> childPool,
      EdgeRecombinationCrossover edgeRecombination) {

    // Linear selection
    final int n = populationSize;
    final float inversePop = 1.0f / n;
    final float nMinus = 0.5f;
    final float nPlus = 2 - nMinus;
    final float nDiff = nPlus - nMinus;

    // Select chromsomes for the parent pool
    parentPool.clear();
    while (parentPool.size() < parentPoolSize) {
      for (int i = 1; i <= n; i++) {
        float probability = inversePop * (nMinus + nDiff * (i - 1) / (n - 1));
        if (rand.nextFloat() <= probability) {
          parentPool.add(n - i); // ith best
        }
      }
    }

    // Iterate through parent pool, choose pairs and perform crossover/mutation
    childPool.clear();
    Chromosome parent1 = null;
    for (Integer index : parentPool) {
      if (parent1 == null) {
        parent1 = chromosomes[index];
      } else {
        Chromosome parent2 = chromosomes[index];

        // Preform crossover
        int[] p1 = parent1.getCityList();
        int[] p2 = parent2.getCityList();

        int[] c1 = edgeRecombination.crossover(p1, p2);
        int[] c2 = edgeRecombination.crossover(p2, p1);

        mutate(c1);
        mutate(c2);

        Chromosome child1 = new Chromosome(c1);
        Chromosome child2 = new Chromosome(c2);

        child1.calculateCost(cities);
        child2.calculateCost(cities);

        // Add children and parents to pool
        childPool.add(child1);
        childPool.add(child2);
        childPool.add(parent1);
        childPool.add(parent2);

        parent1 = null;
      }
    }

    // Sort children and replace parents where children better
    Collections.sort(childPool);
    int pos = 0;
    for (Integer index : parentPool) {
      chromosomes[index] = childPool.get(pos++);
    }
  }

  private void mutate(int[] child) {
    if (rand.nextFloat() <= CHANCE_MUTATION) {
      int len = child.length;
      /*
      if(rand.nextFloat() <= 0.5) {
        int startPos = rand.nextInt(len);
        int endPos = rand.nextInt(len);
        Operators.swap(child, startPos, endPos);
      }
      */
      //if(rand.nextFloat() <= 0.5) {
        int startPos = rand.nextInt(len);
        int endPos = rand.nextInt(len);
        Operators.invert(child, startPos, endPos);
      //}
    }
  }

  public void stop() {
    this.stop = true;
  }

  public void setListener(Listener listener) {
    this.listener = listener;
  }

  private void setStatus(String status) {
    if (listener != null) {
      listener.onUpdate(status);
    }
  }

  public interface Listener {
    void onUpdate(String status);
  }

  private static class RunResult {
    public double rate = 0;
    public int convergenceGen = 0;
    public double cost = 0;
    public void add(RunResult other) {
      this.rate += other.rate;
      this.convergenceGen += other.convergenceGen;
      this.cost += other.cost;
    }
    public void divide(double factor) {
      this.rate /= factor;
      this.convergenceGen /= factor;
      this.cost /= factor;
    }
  }

}