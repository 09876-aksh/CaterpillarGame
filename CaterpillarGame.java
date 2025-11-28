import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class CaterpillarGame extends JPanel implements ActionListener, KeyListener {

    private int[] x = new int[500];
    private int[] y = new int[500];
    private int length = 3;

    private int leafX, leafY;

    private int direction = KeyEvent.VK_RIGHT;
    private boolean running = false;
    private boolean paused = false;
    private boolean gameOver = false; // NEW

    private int score = 0;
    private int highScore = 0;

    private javax.swing.Timer timer;
    private int speed = 150;

    private JButton startButton;   // NEW
    private JButton restartButton; // NEW

    private final String highScoreFile = "highscore.txt";

    public CaterpillarGame() {
        setPreferredSize(new Dimension(600, 600));
        setBackground(Color.WHITE);

        loadHighScore();

        setLayout(null); // allows buttons to be positioned manually
        addKeyListener(this);
        setFocusable(true);

        createButtons();
        initGame();
    }

    // -------------------------------------------------------
    // CREATE *SEPARATE* START & RESTART BUTTONS (proper UI)
    // -------------------------------------------------------
    private void createButtons() {

        startButton = new JButton("START");
        startButton.setBounds(250, 260, 100, 40);
        startButton.addActionListener(e -> {
            running = true;
            paused = false;
            startButton.setVisible(false);
            restartButton.setVisible(false);
            timer.start();
            requestFocusInWindow();
        });

        restartButton = new JButton("RESTART");
        restartButton.setBounds(250, 320, 100, 40);
        restartButton.addActionListener(e -> {
            initGame();
            running = true;
            gameOver = false;
            restartButton.setVisible(false);
            startButton.setVisible(false);
            timer.start();
            requestFocusInWindow();
        });
        restartButton.setVisible(false);

        add(startButton);
        add(restartButton);
    }

    // -------------------------------------------------------
    // Initialize the game
    // -------------------------------------------------------
    private void initGame() {
        length = 3;
        score = 0;
        speed = 150;
        gameOver = false;

        direction = KeyEvent.VK_RIGHT;

        int startX = 300;
        int startY = 300;

        x[0] = startX;
        y[0] = startY;
        x[1] = startX - 20;
        y[1] = startY;
        x[2] = startX - 40;
        y[2] = startY;

        spawnLeaf();

        if (timer != null) timer.stop();
        timer = new javax.swing.Timer(speed, this);

        running = false;
        paused = false;

        startButton.setVisible(true);
        repaint();
    }

    private void spawnLeaf() {
        Random random = new Random();
        leafX = random.nextInt(30) * 20;
        leafY = random.nextInt(30) * 20;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Score & High Score
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.BLACK);
        g.drawString("Score: " + score, 10, 20);
        g.drawString("High Score: " + highScore, 10, 40);

        // Leaf
        g.setColor(Color.RED);
        g.fillOval(leafX, leafY, 20, 20);

        // Caterpillar
        g.setColor(Color.BLACK);
        for (int i = 0; i < length; i++) {
            g.fillRect(x[i], y[i], 20, 20);
        }

        // GAME OVER TEXT
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.setColor(Color.RED);
            g.drawString("GAME OVER", 180, 280);

            restartButton.setVisible(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (!running || paused || gameOver) return;

        // Move body
        for (int i = length - 1; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        // Move head
        if (direction == KeyEvent.VK_RIGHT) x[0] += 20;
        if (direction == KeyEvent.VK_LEFT) x[0] -= 20;
        if (direction == KeyEvent.VK_UP) y[0] -= 20;
        if (direction == KeyEvent.VK_DOWN) y[0] += 20;

        // Wall collision
        if (x[0] < 0 || x[0] > getWidth() - 20 || y[0] < 0 || y[0] > getHeight() - 20) {
            gameOver();
            return;
        }

        // Leaf eaten
        if (x[0] == leafX && y[0] == leafY) {
            score++;
            length++;
            speed = Math.max(50, speed - 5);
            timer.setDelay(speed);
            spawnLeaf();
        }

        repaint();
    }

    private void gameOver() {
        running = false;
        gameOver = true;

        if (score > highScore) {
            highScore = score;
            saveHighScore();
        }

        repaint();
    }

    private void loadHighScore() {
        try {
            File file = new File(highScoreFile);
            if (file.exists()) {
                Scanner sc = new Scanner(file);
                if (sc.hasNextInt()) {
                    highScore = sc.nextInt();
                }
                sc.close();
            }
        } catch (Exception ignored) {}
    }

    private void saveHighScore() {
        try (FileWriter fw = new FileWriter(highScoreFile)) {
            fw.write(String.valueOf(highScore));
        } catch (Exception ignored) {}
    }

    @Override
    public void keyPressed(KeyEvent e) {

        int key = e.getKeyCode();

        if (gameOver) return;

        if (key == KeyEvent.VK_P) {
            if (running) paused = !paused;
        }

        if (!paused && running) {
            if (key == KeyEvent.VK_RIGHT && direction != KeyEvent.VK_LEFT) direction = KeyEvent.VK_RIGHT;
            if (key == KeyEvent.VK_LEFT && direction != KeyEvent.VK_RIGHT) direction = KeyEvent.VK_LEFT;
            if (key == KeyEvent.VK_UP && direction != KeyEvent.VK_DOWN) direction = KeyEvent.VK_UP;
            if (key == KeyEvent.VK_DOWN && direction != KeyEvent.VK_UP) direction = KeyEvent.VK_DOWN;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Caterpillar Game");
            CaterpillarGame game = new CaterpillarGame();

            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
