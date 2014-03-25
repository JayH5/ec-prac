import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.SwingUtilities;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class implements the Traveling Salesman problem
 * as a Java applet.
 */
public class TravelingSalesman extends Applet implements Simulation.Listener {

  /**
   * Is the thread started.
   */
  protected boolean started = false;

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

  private Integer minParentPoolSize = null;

  private Simulation simulation;

  private ExecutorService executorService;


  @Override
  public void init() {
    setLayout(new BorderLayout());

    // setup the controls
    ctrlButtons = new Panel();
    ctrlStart = new Button("Start");
    ctrlMagic = new Button("Stats run");
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
        final int cities = 200;
        final int population = 1000;
        final int parentPoolSize = 500;
        System.out.println("Running simulation for " + cities + " cities, " + population
            + " population, " + parentPoolSize + " parent pool size");
        ExecutorService ex = Executors.newFixedThreadPool(1);
        int repeats = 50;
        for(int i = 0; i < repeats; i++) {
          final int x = i;
          ex.execute(new Runnable() {
            public void run() {
              Simulation.RunResult r = new Simulation(cities, population, parentPoolSize).simulate();
              System.out.println();
            }
          });
        }
        try {
          ex.awaitTermination(24, TimeUnit.HOURS);
        } catch(InterruptedException exc) {
          exc.printStackTrace();
        }
        System.out.println("Done");
      }
    });

    executorService = Executors.newSingleThreadExecutor();

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
    if (simulation != null) {
      simulation.stop();
    }

    simulation = new Simulation(citCount, population, poolsize);
    simulation.setListener(this);

    // start up the background thread
    started = true;

    executorService.submit(new Runnable() {
      @Override
      public void run() {
        simulation.simulate();
      }
    });
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

    if(started && simulation != null) {
      City[] cities = simulation.getCities();
      Chromosome[] chromosomes = simulation.getChromosomes();

      if (cities == null || chromosomes == null) {
        return;
      }

      g.setColor(Color.red);
      for (int i = 0; i < cities.length; i++) {
        int xpos = cities[i].getProjectedX(drawableWidth);
        int ypos = cities[i].getProjectedY(drawableHeight);
        g.fillOval(xpos - 5, ypos - 5, 10, 10);
      }

      g.setColor(Color.white);
      for (int i = 0; i < cities.length; i++) {
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

  @Override
  public void onUpdate(final String status) {
    // Invoke on the UI thread
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        TravelingSalesman.this.status = status;
        update();
      }
    });
  }

  @Override
  public void paint(Graphics g) {
    update();
  }
}
// vim: ts=2:sw=2
