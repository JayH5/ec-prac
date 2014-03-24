class City {

  private static final int WINDOW_WIDTH = 630; //getBounds().width - 10
  private static final int WINDOW_HEIGHT = 412; //ctrlButtons.getBounds().y - fm.getHeight() - 2

  /**
   * The city's x position.
   */
  private double xpos;

  /**
   * The city's y position.
   */
  private double ypos;

  /**
   * Constructor.
   *
   * @param x The city's x position
   * @param y The city's y position.
   */
  public City(double x, double y) {
    xpos = x;
    ypos = y;
  }

  /**
   * Return's the city's x position.
   *
   * @return The city's x position.
   */
  public double getx() {
    return xpos;
  }

  /**
   * Returns the city's y position.
   *
   * @return The city's y position.
   */
  public double gety() {
    return ypos;
  }

  public int getProjectedX(int width) {
    return (int)(xpos * width);
  }

  public int getProjectedY(int height) {
    return (int)(ypos * height);
  }

  /**
   * Returns how close the city is to another city.
   *
   * @param cother The other city.
   * @return A distance.
   */
  public double proximity(City cother) {
    return proximity(cother.getx(), cother.gety());
  }

  /**
   * Returns how far this city is from a a specific point.
   * This method uses the pythagorean theorum to calculate
   * the distance.
   *
   * @param x The x coordinate
   * @param y The y coordinate
   * @return The distance.
   */
  public double proximity(double x, double y) {
    double xdiff = (WINDOW_WIDTH * xpos) - (WINDOW_WIDTH * x);
    double ydiff = (WINDOW_HEIGHT * ypos) - (WINDOW_HEIGHT * y);
    // TODO: Save a sqrt today
    return Math.sqrt(xdiff * xdiff + ydiff * ydiff);
  }
}
// vim: ts=2:sw=2
