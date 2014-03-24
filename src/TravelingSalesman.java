import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Queue;
import java.util.LinkedList;

/**
 * This class implements the Traveling Salesman problem
 * as a Java applet.
 */
public class TravelingSalesman extends Applet implements Runnable {

  /**
   * How many cities to use.
   */
  protected int cityCount;

  /**
   * How many chromosomes to use.
   */
  protected int populationSize;

  /**
   * The part of the population eligable for mateing.
   */
  protected int matingPopulationSize;

  /**
   * The part of the population selected for mating.
   */
  protected int selectedParents;

  /**
   * The current generation
   */
  protected int generation;

  /**
   * The background worker thread.
   */
  protected Thread worker = null;

  /**
   * Is the thread started.
   */
  protected boolean started = false;

  /**
   * The list of cities.
   */
  protected City [] cities;

  /**
   * The list of chromosomes.
   */
  protected Chromosome [] chromosomes;

  /**
   * The Start button.
   */
  private Button ctrlStart;

  /**
   * The Magic button.
   */
  private Button ctrlMagic;

  /**
   * The TextField that holds the number of cities.
   */
  private TextField ctrlCities;

  /**
   * The TextField for the population size.
   */
  private TextField ctrlPopulationSize;

  /**
   * Holds the buttons and other controls, forms a strip across
   * the bottom of the applet.
   */
  private Panel ctrlButtons;

  /**
   * The current status, which is displayed just above the controls.
   */
  private String status = "";

  /**
   * Signal the thread to end.
   */
  private boolean stop = false;

  /**
   * The cost at END_GEN generations; the simulation keeps going, but this value is recorded.
   */
  private Double endCost;

  private int END_GEN = 1000;

  private Queue<Double> convergenceHistory;
  private Queue<Long> timingHistory;

  private int HISTORY_SIZE = 200;

  private final Random random = new Random(System.currentTimeMillis());

  private Integer minParentPoolSize = null;
  private static final int ELITISM = 0;

  @Override
  public void init() {
    setLayout(new BorderLayout());

    // setup the controls
    ctrlButtons = new Panel();
    ctrlStart = new Button("Start");
    ctrlMagic = new Button("Magic");
    ctrlButtons.add(ctrlStart);
    ctrlButtons.add(new Label("# Cities:"));
    ctrlButtons.add(ctrlCities = new TextField(5));
    ctrlButtons.add(new Label("Population Size:"));
    ctrlButtons.add(ctrlPopulationSize = new TextField(5));
    ctrlButtons.add(ctrlMagic);
    this.add(ctrlButtons, BorderLayout.SOUTH);

    // set the default values
    ctrlPopulationSize.setText("1000");
    ctrlCities.setText("200");

    // add an action listener for the button
    ctrlStart.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        startThreadFromUI();
      }
    });

    ctrlMagic.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        startThreadFromUI();
      }
    });

    started = false;
    update();
  }

  /**
   * Start the background thread.
   */
  private void startThreadFromUI() {
    int cities = 0;
    try {
      cities = Integer.parseInt(ctrlCities.getText());
    } catch (NumberFormatException e) {
      cities = 50;
    }

    int population = 0;
    try {
      population = Integer.parseInt(ctrlPopulationSize.getText());
    } catch (NumberFormatException e) {
      population = 1000;
    }
    minParentPoolSize = 500;
    startThread(cities, population, minParentPoolSize);
  }

  private void startThread(int citCount, int population, int poolsize) {
    if (worker != null) {
      stop = true;
      try {
        worker.join();
      } catch(InterruptedException e) {
        throw new Error("Zomg!");
      }
    }
    cityCount = citCount;

    populationSize = population;
    minParentPoolSize = poolsize;
    endCost = null;

    FontMetrics fm = getGraphics().getFontMetrics();
    int bottom = ctrlButtons.getBounds().y - fm.getHeight() - 2;

    // create a random list of cities
    cities = new City[cityCount];
    for (int i = 0; i < cityCount; i++) {
      cities[i] = new City(
          (double) (Math.random()),
          //(int) (Math.random() * (getBounds().width - 10)),
          (double) (Math.random()));
          //(int) (Math.random() * (bottom - 10)));
    }

    convergenceHistory = new LinkedList<Double>();
    timingHistory = new LinkedList<Long>();

    // create the initial population of chromosomes
    chromosomes = new Chromosome[populationSize];
    for (int i = 0; i < populationSize; i++) {
      chromosomes[i] = new Chromosome(cities);
    }
    Arrays.sort(chromosomes);

    matingPopulationSize = 100;

    // start up the background thread
    started = true;

    generation = 0;

    worker = new Thread(this);
    worker.setPriority(Thread.MIN_PRIORITY);
    worker.start();
  }

  /**
   * Update the display
   */
  public void update() {
    Image img = createImage(getBounds().width, getBounds().height);
    Graphics g = img.getGraphics();
    FontMetrics fm = g.getFontMetrics();

    int width = getBounds().width;
    int bottom = ctrlButtons.getBounds().y - fm.getHeight() - 2;

    int drawableWidth = width - 10;
    int drawableHeight = bottom - 10;

    g.setColor(Color.black);
    g.fillRect(0, 0, width, bottom);

    if(started && cities != null) {
      g.setColor(Color.red);
      for (int i = 0; i < cityCount; i++) {
        int xpos = cities[i].getProjectedX(drawableWidth);
        //int xpos = cities[i].getx();
        int ypos = cities[i].getProjectedY(drawableHeight);
        //int ypos = cities[i].gety();
        g.fillOval(xpos - 5, ypos - 5, 10, 10);
      }

      g.setColor(Color.white);
      for (int i = 0; i < cityCount; i++) {
        Chromosome chromosome = chromosomes[0];
        int icity = chromosome.getCity(i);
        if (i != 0) {
          int last = chromosome.getCity(i - 1);
          g.drawLine(
              cities[icity].getProjectedX(drawableWidth),
              cities[icity].getProjectedY(drawableHeight),
              cities[last].getProjectedX(drawableWidth),
              cities[last].getProjectedY(drawableHeight));
        }
      }

    }


    g.drawString(status, 0, bottom);

    getGraphics().drawImage(img, 0, 0, this);
  }

  /**
   * Update the status.
   *
   * @param status The status.
   */
  private void setStatus(String status) {
    this.status = status;
  }

  /**
   * The main loop for the background thread.
   */
  @Override
  public void run() {
    //feed all our UI-supplied parameters to the GA
    runGA(true, 10000, populationSize, minParentPoolSize);
    /*
    int[] ar = {50, 100, 150, 250, 400, 600, 1000, 2000, 3000, 5000};
    //RunResult[] resses = new RunResult[ar.length];
    int numSamples = 5;
    for(int i = 0; i < ar.length; i++) { //for each population size
      int pSize = ar[i];
      RunResult averaged = new RunResult();
      for(int j = 0; j < numSamples; j++) {
        averaged.add(runGA(false, 10000, pSize, (int)(0.5 * pSize)));
      }
      averaged.divide(numSamples);
      System.out.println("For population size " + pSize + ":");
      System.out.println("Average cost: " + averaged.cost);
      System.out.println("Average convergence generation: " + averaged.convergenceGen);
      System.out.println("Average rate: " + averaged.rate);
      System.out.println();
    }
    */
  }

  @Override
  public void paint(Graphics g) {
    update();
  }

  public RunResult runGA(boolean graphical, int maxGenerations, int popSize, int poolSize) {
    final int n = populationSize;
    final float inversePop = 1.0f / n;
    final float nMinus = 0.5f;
    final float nPlus = 2 - nMinus;
    final float nDiff = nPlus - nMinus;

    Set<Integer> parentPool = new HashSet<Integer>();
    List<Chromosome> childPool = new ArrayList<Chromosome>();
    long genStartTime = -1;
    RunResult result = new RunResult();

    NumberFormat doubf = NumberFormat.getInstance();
    doubf.setMinimumFractionDigits(0);
    doubf.setMaximumFractionDigits(2);
    NumberFormat intf = NumberFormat.getInstance();
    intf.setMaximumFractionDigits(0);
    intf.setGroupingUsed(false);

    if(graphical) {
      setStatus("Simulation starting up...");
      update();
    }

    for (; generation <= maxGenerations; generation++) {
      if(stop) {
        stop = false;
        return result;
      }
      genStartTime = System.currentTimeMillis();
      parentPool.clear();
      while (parentPool.size() < poolSize) {
        for (int i = 1; i <= n - ELITISM; i++) {
          float probability = inversePop * (nMinus + nDiff * (i - 1) / (n - 1));
          if (random.nextFloat() <= probability) {
            parentPool.add(n - i); // ith best
          }
        }
      }

      //System.out.println("Parent pool size= " + parentPool.size());

      // Iterate through parent pool, choose pairs and perform crossover/mutation
      childPool.clear();
      Chromosome parent1 = null;
      for (Integer index : parentPool) {
        if (parent1 == null) {
          parent1 = chromosomes[index];
        } else {
          Chromosome parent2 = chromosomes[index];
          Chromosome.Children children = Chromosome.mate(parent1, parent2);
          children.calculateCost(cities);

          // Add children and parents to pool
          childPool.add(children.child1);
          childPool.add(children.child2);
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

      Arrays.sort(chromosomes);
      result.cost = chromosomes[0].getCost();

      //convergence detection
      if(convergenceHistory.size() == HISTORY_SIZE) {
        convergenceHistory.remove();
      }
      convergenceHistory.add(result.cost);
      if(convergenceHistory.size() == HISTORY_SIZE){
        double sum = 0;
        for(double i : convergenceHistory) {
          sum += i;
        }
        double avg = sum/convergenceHistory.size();
        double sumOfSquares = -1;
        for(double i : convergenceHistory) {
          double diff = Math.abs(i - avg);
          sumOfSquares += diff * diff;
        }
        double stdDev = Math.sqrt(sumOfSquares);
        if(stdDev < 50) { //we've converged!
          if(graphical) {
            if(endCost != null) {
              setStatus("Converged at generation " + generation + " with cost " +  intf.format(result.cost) + "; Rate: " + doubf.format(result.rate)  + "Cost at 1000: " + intf.format(endCost));
            } else {
              setStatus("Converged at generation " + generation + " with cost " +  intf.format(result.cost) + "; Rate: " + doubf.format(result.rate));
            }
            update();
          }
          result.convergenceGen = generation;
          return result;
        }
      }

      //evolution rate calculation
      long currentGenTime = System.currentTimeMillis() - genStartTime;
      if(timingHistory.size() == HISTORY_SIZE) {
        timingHistory.remove();
      }
      timingHistory.add(currentGenTime);
      double sum = 0;
      for(double i : timingHistory) {
        sum += i;
      }
      result.rate = (timingHistory.size() * 1000.0) / sum;

      if(generation == 1000) {
        endCost = result.cost;
      }
      if(graphical) {
        if(endCost != null) {
          setStatus("Generation " + generation + " Cost " + intf.format(result.cost) + " Rate " + doubf.format(result.rate) + "Cost at 1000: " + intf.format(endCost));
        } else {
          setStatus("Generation " + generation + " Cost " + intf.format(result.cost) + " Rate " + doubf.format(result.rate));
        }
        update();
      }
    }
    if(graphical) {
      setStatus("Ran out of generations at " + generation + ". Cost " + intf.format(result.cost) + " Rate " + doubf.format(result.rate));
      update();
    }
    return result;
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

  private static String nullableToString(Object a) {
    if(a == null) {
      return "";
    } else {
      return a.toString();
    }
  }
}
// vim: ts=2:sw=2
