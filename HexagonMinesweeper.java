/**
 * Java. Classic Game Minesweeper
 *
 * @author Sergey Iryupin
 * @version 0.3.2 dated September 23, 2016
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.Timer;
/**
 * the class implements the minesweeper game in a version with hexagonal
 * cells with the ability to adjust the number of mines and the size of the field
 */
class HexagonMinesweeper extends JFrame {

    private final int BLOCK_SIZE = 40; // size of one block
    private final int FIELD_SIZE = 9; // in blocks
    private final int MOUSE_BUTTON_LEFT = 1; // for mouse listener
    private final int MOUSE_BUTTON_RIGHT = 3;
    private final int NUMBER_OF_MINES = 10;
    private final int[] COLOR_OF_NUMBERS = {0x0000FF, 0x008000, 0xFF0000, 0x800000, 0x0};
    private Cell[][] field = new Cell[FIELD_SIZE][FIELD_SIZE];
    private Random random = new Random();
    private int countOpenedCells;
    private boolean youWon, bangMine; // flags for win and bang/fail
    private int bangX, bangY; // for fix the coordinates of the explosion

    public static void main(String[] args) {
        new HexagonMinesweeper();
    }


    private HexagonMinesweeper() {
        String TITLE_OF_PROGRAM = "Hexagon Minesweeper";
        setTitle(TITLE_OF_PROGRAM);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        int FIELD_DX = 40;
        int FIELD_DY = 80;
        int START_LOCATION = 200;
        setBounds(START_LOCATION, START_LOCATION, FIELD_SIZE * (BLOCK_SIZE + 2) + FIELD_DX, FIELD_SIZE * (BLOCK_SIZE / 4 * 3 + 2) + FIELD_DY);
        setResizable(false);
        final TimerLabel timeLabel = new TimerLabel();
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        final Canvas canvas = new Canvas();
        canvas.setBackground(Color.white);
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                int x1 = e.getX() / (BLOCK_SIZE + 2);
                int y1 = e.getY() / (BLOCK_SIZE / 4 * 3 + 2);
                int x = -1, y = -1;
                if(y1 % 2 == 0) {
                    if(x1 < FIELD_SIZE && y1 < FIELD_SIZE && field[y1][x1].hexagon.contains(e.getX(), e.getY())){x = x1; y = y1;}
                    if(y1 - 1 > -1 && y1 < FIELD_SIZE && field[y1 - 1][x1].hexagon.contains(e.getX(), e.getY())){x = x1; y = y1 - 1;}
                    if(x1 - 1 > -1 && y1 - 1 > -1 && field[y1 - 1][x1 - 1].hexagon.contains(e.getX(), e.getY())){x = x1 - 1; y = y1 - 1;}
                }
                else {
                    if(x1 < FIELD_SIZE && y1 < FIELD_SIZE && field[y1][x1].hexagon.contains(e.getX(), e.getY())){x = x1; y = y1;}
                    if(y1 - 1 > -1 && x1 + 1 < FIELD_SIZE && field[y1 - 1][x1 + 1].hexagon.contains(e.getX(), e.getY())){x = x1 + 1; y = y1 - 1;}
                    if(y1 - 1 > -1 && x1 < FIELD_SIZE && field[y1 - 1][x1].hexagon.contains(e.getX(), e.getY())){x = x1; y = y1 - 1;}
                }
                if (!bangMine && !youWon && x > -1 && y > -1) {
                    if (e.getButton() == MOUSE_BUTTON_LEFT)
                        if (field[y][x].isNotOpen()) {
                            openCells(x, y);
                            youWon = countOpenedCells == FIELD_SIZE*FIELD_SIZE - NUMBER_OF_MINES;
                            if (bangMine) {
                                bangX = x;
                                bangY = y;
                            }
                        }
                    if (e.getButton() == MOUSE_BUTTON_RIGHT) field[y][x].inverseFlag();
                    if (bangMine || youWon) timeLabel.stopTimer();
                    canvas.repaint();
                }
            }
        });
        add(BorderLayout.CENTER, canvas);
        add(BorderLayout.SOUTH, timeLabel);
        setVisible(true);
        initField();
    }

    /**
     * recursive procedure of opening the cells
     * @param x - cell side x
     * @param y - cell side y
     */
    private void openCells(int x, int y) {
        if (x < 0 || x > FIELD_SIZE - 1 || y < 0 || y > FIELD_SIZE - 1) return;
        if (!field[y][x].isNotOpen()) return;
        field[y][x].open();
        if (field[y][x].getCountBomb() > 0 || bangMine) return;
        for (int dx = -1; dx < 1; dx++)
            for (int dy = -1; dy < 2; dy++) openCells(x + dx + y % 2, y + dy);
        if(y % 2 == 0 && x + 1 < FIELD_SIZE) {openCells(x + 1, y);}
        if(y % 2 > 0 && x - 1 > -1){openCells(x - 1, y);}
    }

    /**
     * initialization of the playing field
     */
    private void initField() {
        int x, y, countMines = 0;
        for (x = 0; x < FIELD_SIZE; x++)
            for (y = 0; y < FIELD_SIZE; y++)
                field[y][x] = new Cell(x, y);
        while (countMines < NUMBER_OF_MINES) {
            do {
                x = random.nextInt(FIELD_SIZE);
                y = random.nextInt(FIELD_SIZE);
            } while (field[y][x].isMined());
            field[y][x].mine();
            countMines++;
        }
        for (x = 0; x < FIELD_SIZE; x++)
            for (y = 0; y < FIELD_SIZE; y++)
                if (!field[y][x].isMined()) {
                    int count = 0;
                    for (int dx = -1; dx < 1; dx++)
                        for (int dy = -1; dy < 2; dy++) {
                            int nX = x + dx + y % 2;
                            int nY = y + dy;
                            if (nX < 0 || nY < 0 || nX > FIELD_SIZE - 1 || nY > FIELD_SIZE - 1) {
                                nX = x;
                                nY = y;
                            }
                            count += (field[nY][nX].isMined()) ? 1 : 0;
                        }
                    if(y % 2 == 0 && x + 1 < FIELD_SIZE) {count += (field[y][x + 1].isMined()) ? 1 : 0;}
                    if(y % 2 > 0 && x - 1 > -1){count += (field[y][x - 1].isMined()) ? 1 : 0;}
                    field[y][x].setCountBomb(count);
                }
    }

    /**
     * the class creates a cell of the playing field
     */
    class Cell {
        private int countBombNear;
        private boolean isOpen, isMine, isFlag;
        private Polygon hexagon;

        Cell(int x, int y) {
            int xpoints[] = new int[6];
            int ypoints[] = new int[6];
            int dx;
            if (y % 2 == 0) {
                dx = 0;
            }
            else {
                dx = BLOCK_SIZE / 2 + 1;
            }
            xpoints[0] = 2*x + x * BLOCK_SIZE + dx; ypoints[0] = 2*y + y * BLOCK_SIZE / 4 * 3 + BLOCK_SIZE / 4;
            xpoints[1] = 2*x + x * BLOCK_SIZE + BLOCK_SIZE / 2 + dx; ypoints[1] = 2*y +  y * BLOCK_SIZE / 4 * 3;
            xpoints[2] = 2*x + x * BLOCK_SIZE + BLOCK_SIZE + dx; ypoints[2] = 2*y +  y * BLOCK_SIZE / 4 * 3 + BLOCK_SIZE / 4;
            xpoints[3] = 2*x + x * BLOCK_SIZE + BLOCK_SIZE + dx; ypoints[3] = 2*y + y * BLOCK_SIZE / 4 * 3 + BLOCK_SIZE / 4 * 3;
            xpoints[4] = 2*x + x * BLOCK_SIZE + BLOCK_SIZE / 2 + dx; ypoints[4] = 2*y + y * BLOCK_SIZE / 4 * 3 + BLOCK_SIZE;
            xpoints[5] = 2*x + x * BLOCK_SIZE + dx; ypoints[5] = 2*y + y * 30 + 30;
            hexagon = new Polygon(xpoints, ypoints, 6);
        }

        void open() {
            isOpen = true;
            bangMine = isMine;
            if (!isMine) countOpenedCells++;
        }

        void mine() { isMine = true; }

        void setCountBomb(int count) { countBombNear = count; }

        int getCountBomb() { return countBombNear; }

        boolean isNotOpen() { return !isOpen; }

        boolean isMined() { return isMine; }

        void inverseFlag() { isFlag = !isFlag; }

        void paintBomb(Graphics g, int x, int y, Color color) {
            g.setColor(color);
            int dx;
            if (y % 2 == 0) {
                dx = 0;
            }
            else {
                dx = BLOCK_SIZE / 2 + 1;
            }
            int xcenter = 2*x + x * BLOCK_SIZE + BLOCK_SIZE / 2 + dx;
            int ycenter = 2*y + y * BLOCK_SIZE / 4 * 3 + BLOCK_SIZE / 2;
            g.fillRect(xcenter - BLOCK_SIZE / 8, ycenter - BLOCK_SIZE / 4, BLOCK_SIZE / 4, BLOCK_SIZE / 2);
            g.fillRect(xcenter - BLOCK_SIZE / 4, ycenter - BLOCK_SIZE / 8, BLOCK_SIZE / 2, BLOCK_SIZE / 4);
            g.fillRect(xcenter - BLOCK_SIZE / 8 - BLOCK_SIZE / 16, ycenter - BLOCK_SIZE / 8 - BLOCK_SIZE / 16, BLOCK_SIZE / 8 * 3, BLOCK_SIZE / 8 * 3);
            g.setColor(Color.white);
            g.fillRect(xcenter - BLOCK_SIZE / 16, ycenter - BLOCK_SIZE / 16, BLOCK_SIZE / 16, BLOCK_SIZE / 16);
        }

        void paintString(Graphics g, String str, int x, int y, Color color) {
            g.setColor(color);
            int dx;
            if (y % 2 == 0) {
                dx = 0;
            }
            else {
                dx = BLOCK_SIZE / 2 + 1;
            }
            int xcenter = 2*x + x * BLOCK_SIZE + BLOCK_SIZE / 2 + dx;
            int ycenter = 2*y + y * BLOCK_SIZE / 4 * 3 + BLOCK_SIZE / 2;
            g.setFont(new Font("", Font.BOLD, BLOCK_SIZE / 2));
            g.drawString(str, xcenter - BLOCK_SIZE / 8, ycenter + BLOCK_SIZE / 4);
        }

        void paint(Graphics g, int x, int y) {
            g.setColor(Color.darkGray);
            g.drawPolygon(hexagon);

            if (!isOpen) {
                if ((bangMine || youWon) && isMine) paintBomb(g, x, y, Color.black);
                else {
                    g.setColor(Color.lightGray);
                    g.fillPolygon(hexagon);
                    String SIGN_OF_FLAG = "f";
                    if (isFlag) paintString(g, SIGN_OF_FLAG, x, y, Color.red);
                }
            } else
            if (isMine) paintBomb(g, x, y, bangMine? Color.red : Color.black);
            else
            if (countBombNear > 0)
                paintString(g, Integer.toString(countBombNear), x, y, new Color(COLOR_OF_NUMBERS[countBombNear - 1]));
        }
    }

    /**
     * class realizes stopwatch
     */
    class TimerLabel extends JLabel {
        Timer timer = new Timer();

        TimerLabel() { timer.scheduleAtFixedRate(timerTask, 0, 1000); }

        TimerTask timerTask = new TimerTask() {
            volatile int time;
            Runnable refresher = new Runnable() {
                public void run() {
                    TimerLabel.this.setText(String.format("%02d:%02d", time / 60, time % 60));
                }
            };
            public void run() {
                time++;
                SwingUtilities.invokeLater(refresher);
            }
        };

        void stopTimer() { timer.cancel(); }
    }

    /**
     * my class for painting
     */

    class Canvas extends JPanel {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            for (int x = 0; x < FIELD_SIZE; x++)
                for (int y = 0; y < FIELD_SIZE; y++) field[y][x].paint(g, x, y);
        }
    }
}