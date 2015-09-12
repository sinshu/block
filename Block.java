import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Block extends Applet implements Runnable, WindowListener, MouseListener, MouseMotionListener
{
	static final int NUM_BLOCK_ROWS = 20;
	static final int NUM_BLOCK_COLS = 15;
	static final int BLOCK_WIDTH = 32;
	static final int BLOCK_HEIGHT = 24;
	static final int SCREEN_WIDTH = NUM_BLOCK_COLS * BLOCK_WIDTH;
	static final int SCREEN_HEIGHT = NUM_BLOCK_ROWS * BLOCK_HEIGHT;
	
	static final int MAX_NUM_BALLS = 16;
	static final double MAX_BALL_SPEED = 12;
	
	static final int RACKET_WIDTH = 64;
	static final int RACKET_HEIGHT = 12;
	static final double INIT_RACKET_X = (SCREEN_WIDTH - RACKET_WIDTH) / 2;
	static final double INIT_RACKET_Y = SCREEN_HEIGHT - BLOCK_HEIGHT;
	static final int NUM_RACKET_DX = 4;
	
	int[][] blocks;
	
	int numBalls;
	double[] ballX;
	double[] ballY;
	double[] ballX2;
	double[] ballY2;
	double[] ballVX;
	double[] ballVY;
	
	double racketX;
	double racketY;
	double racketX2;
	double[] racketDX;
	double racketVX;
	
	int mouseX;
	
	Image[] imgBlock;
	Image imgSolid;
	Image imgBall;
	Image imgRacket;
	
	Image bufImg = null;
	Graphics bufGfx;
	
	boolean appAlive;
	
	boolean turbo = false;
	
	public Block()
	{
		blocks = new int[NUM_BLOCK_ROWS][];
		for (int row = 0; row < NUM_BLOCK_ROWS; row++)
		{
			blocks[row] = new int[NUM_BLOCK_COLS];
			for (int col = 0; col < NUM_BLOCK_COLS; col++)
			{
				if (row == 0 || col == 0 || col == NUM_BLOCK_COLS - 1)
				{
					blocks[row][col] = 20000;
				}
				else
				{
					if (col >= 2 && col < NUM_BLOCK_COLS - 2)
					if (row == 2)
					{
						blocks[row][col] = 10000;
					}
					else if (row == 3)
					{
						blocks[row][col] = 10001;
					}
					else if (row == 4)
					{
						blocks[row][col] = 10002;
					}
					else if (row == 5)
					{
						blocks[row][col] = 10004;
					}
					else if (row == 6)
					{
						blocks[row][col] = 10006;
					}
					else if (row == 7)
					{
						blocks[row][col] = 10008;
					}
					else if (row == 8)
					{
						blocks[row][col] = 10010;
					}
					else
					{
						blocks[row][col] = 0;
					}
				}
			}
		}
		numBalls = 0;
		ballX = new double[MAX_NUM_BALLS];
		ballY = new double[MAX_NUM_BALLS];
		ballX2 = new double[MAX_NUM_BALLS];
		ballY2 = new double[MAX_NUM_BALLS];
		ballVX = new double[MAX_NUM_BALLS];
		ballVY = new double[MAX_NUM_BALLS];
		racketX = racketX2 = INIT_RACKET_X;
		racketY = INIT_RACKET_Y;
		racketDX = new double[NUM_RACKET_DX];
		for (int i = 0; i < NUM_RACKET_DX; i++)
		{
			racketDX[i] = 0;
		}
		mouseX = SCREEN_WIDTH / 2;
		setBackground(Color.black);
	}
	
	void tick()
	{
		for (int i = 0; i < numBalls;)
		{
			if (ballY[i] > SCREEN_HEIGHT)
			{
				remBall(i);
			}
			else
			{
				ballX2[i] = ballX[i];
				ballY2[i] = ballY[i];
				ballVX[i] += 0.015625 - Math.random() * 0.03125;
				ballVY[i] += 0.125 + 0.015625 - Math.random() * 0.03125;
				ballX[i] += ballVX[i];
				ballY[i] += ballVY[i];
				int row = rowOf(ballY[i]);
				int col = colOf(ballX[i]);
				int row2 = rowOf(ballY2[i]);
				int col2 = colOf(ballX2[i]);
				if (isSolidBlock(row, col))
				{
					//ÅõÅ®Å†Å©Åõ
					if (row2 == row)
					{
						//Å†Å©Åõ
						if (col2 == col + 1)
						{
							if (ballVX[i] < 0)
							{
								ballX[i] = (col + 1) * BLOCK_WIDTH;
								reflectX(i);
								hit(row, col);
							}
						}
						//ÅõÅ®Å†
						else if (col2 == col - 1)
						{
							if (ballVX[i] > 0)
							{
								ballX[i] = col * BLOCK_WIDTH;
								reflectX(i);
								hit(row, col);
							}
						}
					}
					//    Å†
					//  Å^Å™Å_
					//Åõ  Åõ  Åõ
					else if (row2 == row + 1)
					{
						if (ballVY[i] < 0)
						{
							//Å†
							//Å™
							//Åõ
							if (col2 == col)
							{
								ballY[i] = (row + 1) * BLOCK_HEIGHT;
								reflectY(i);
								hit(row, col);
							}
							//Å†ÅH
							//ÅHÅ_
							//    Åõ
							else if (col2 == col + 1)
							{
								if (ballVX[i] < 0)
								{
									//Å†Å†
									//  Å_
									//    Åõ
									if (!isSolidBlock(row + 1, col) && isSolidBlock(row, col + 1))
									{
										ballY[i] = (row + 1) * BLOCK_HEIGHT;
										reflectY(i);
										hit(row, col);
									}
									//Å†
									//Å†Å_
									//    Åõ
									else if (isSolidBlock(row + 1, col) && !isSolidBlock(row, col + 1))
									{
										ballX[i] = (col + 1) * BLOCK_WIDTH;
										reflectX(i);
										hit(row, col);
									}
									//Å†Å†
									//Å†Å_
									//    Åõ
									else if (isSolidBlock(row + 1, col) && isSolidBlock(row, col + 1))
									{
										ballX[i] = (col + 1) * BLOCK_WIDTH;
										ballY[i] = (row + 1) * BLOCK_HEIGHT;
										reflectX(i);
										reflectY(i);
										hit(row + 1, col);
										hit(row, col + 1);
									}
									//Å†
									//  Å_
									//    Åõ
									else
									{
										ballX[i] = (col + 1) * BLOCK_WIDTH;
										ballY[i] = (row + 1) * BLOCK_HEIGHT;
										reflectX(i);
										reflectY(i);
										hit(row, col);
									}
								}
							}
							//  ÅHÅ†
							//  Å^ÅH
							//Åõ
							else if (col2 == col - 1)
							{
								if (ballVX[i] > 0)
								{
									//  Å†Å†
									//  Å^
									//Åõ
									if (!isSolidBlock(row + 1, col) && isSolidBlock(row, col - 1))
									{
										ballY[i] = (row + 1) * BLOCK_HEIGHT;
										reflectY(i);
										hit(row, col);
									}
									//    Å†
									//  Å^Å†
									//Åõ
									else if (isSolidBlock(row + 1, col) && !isSolidBlock(row, col - 1))
									{
										ballX[i] = col * BLOCK_WIDTH;
										reflectX(i);
										hit(row, col);
									}
									//  Å†Å†
									//  Å^Å†
									//Åõ
									else if (isSolidBlock(row + 1, col) && isSolidBlock(row, col - 1))
									{
										ballX[i] = col * BLOCK_WIDTH;
										ballY[i] = (row + 1) * BLOCK_HEIGHT;
										reflectX(i);
										reflectY(i);
										hit(row + 1, col);
										hit(row, col - 1);
									}
									//    Å†
									//  Å^
									//Åõ
									else
									{
										ballX[i] = col * BLOCK_WIDTH;
										ballY[i] = (row + 1) * BLOCK_HEIGHT;
										reflectX(i);
										reflectY(i);
										hit(row, col);
									}
								}
							}
						}
					}
					//Åõ  Åõ  Åõ
					//  Å_Å´Å^
					//    Å†
					else if (row2 == row - 1)
					{
						if (ballVY[i] > 0)
						{
							//Åõ
							//Å´
							//Å†
							if (col2 == col)
							{
								ballY[i] = row * BLOCK_HEIGHT;
								reflectY(i);
								hit(row, col);
							}
							//    Åõ
							//ÅHÅ^
							//Å†ÅH
							else if (col2 == col + 1)
							{
								if (ballVX[i] < 0)
								{
									//    Åõ
									//  Å^
									//Å†Å†
									if (!isSolidBlock(row - 1, col) && isSolidBlock(row, col + 1))
									{
										ballY[i] = row * BLOCK_HEIGHT;
										reflectY(i);
										hit(row, col);
									}
									//    Åõ
									//Å†Å^
									//Å†
									else if (isSolidBlock(row - 1, col) && !isSolidBlock(row, col + 1))
									{
										ballX[i] = (col + 1) * BLOCK_WIDTH;
										reflectX(i);
										hit(row, col);
									}
									//    Åõ
									//Å†Å^
									//Å†Å†
									else if (isSolidBlock(row - 1, col) && isSolidBlock(row, col + 1))
									{
										ballX[i] = (col + 1) * BLOCK_WIDTH;
										ballY[i] = row * BLOCK_HEIGHT;
										reflectX(i);
										reflectY(i);
										hit(row - 1, col);
										hit(row, col + 1);
									}
									//    Åõ
									//  Å^
									//Å†
									else
									{
										ballX[i] = (col + 1) * BLOCK_WIDTH;
										ballY[i] = row * BLOCK_HEIGHT;
										reflectX(i);
										reflectY(i);
										hit(row, col);
									}
								}
							}
							//Åõ
							//  Å_ÅH
							//  ÅHÅ†
							else if (col2 == col - 1)
							{
								if (ballVX[i] > 0)
								{
									//Åõ
									//  Å_
									//  Å†Å†
									if (!isSolidBlock(row - 1, col) && isSolidBlock(row, col - 1))
									{
										ballY[i] = row * BLOCK_HEIGHT;
										reflectY(i);
										hit(row, col);
									}
									//Åõ
									//  Å_Å†
									//    Å†
									else if (isSolidBlock(row - 1, col) && !isSolidBlock(row, col - 1))
									{
										ballX[i] = col * BLOCK_WIDTH;
										reflectX(i);
										hit(row, col);
									}
									//Åõ
									//  Å_Å†
									//  Å†Å†
									else if (isSolidBlock(row - 1, col) && isSolidBlock(row, col - 1))
									{
										ballX[i] = col * BLOCK_WIDTH;
										ballY[i] = row * BLOCK_HEIGHT;
										reflectX(i);
										reflectY(i);
										hit(row - 1, col);
										hit(row, col - 1);
									}
									//Åõ
									//  Å_
									//    Å†
									else
									{
										ballX[i] = col * BLOCK_WIDTH;
										ballY[i] = row * BLOCK_HEIGHT;
										reflectX(i);
										reflectY(i);
										hit(row, col);
									}
								}
							}
						}
					}
				}
				else
				{
					//  Å†
					//Å†Å_
					//    Åõ
					if (row2 == row + 1 && col2 == col + 1)
					{
						if (isSolidBlock(row + 1, col) && isSolidBlock(row, col + 1))
						{
							if (ballVX[i]<0 && ballVY[i]<0)
							{
								ballX[i] = (col + 1) * BLOCK_WIDTH;
								ballY[i] = (row + 1) * BLOCK_HEIGHT;
								reflectX(i);
								reflectY(i);
								hit(row + 1, col);
								hit(row, col + 1);
							}
						}
					}
					//  Å†
					//  Å^Å†
					//Åõ
					else if (row2 == row + 1 && col2 == col - 1)
					{
						if (isSolidBlock(row + 1, col) && isSolidBlock(row, col - 1))
						{
							if (ballVX[i]>0 && ballVY[i]<0)
							{
								ballX[i] = col * BLOCK_WIDTH;
								ballY[i] = (row + 1) * BLOCK_HEIGHT;
								reflectX(i);
								reflectY(i);
								hit(row + 1, col);
								hit(row, col - 1);
							}
						}
					}
					//    Åõ
					//Å†Å^
					//  Å†
					else if (row2 == row - 1 && col2 == col + 1)
					{
						if (isSolidBlock(row - 1, col) && isSolidBlock(row, col + 1))
						{
							if (ballVX[i]<0 && ballVY[i]>0)
							{
								ballX[i] = (col + 1) * BLOCK_WIDTH;
								ballY[i] = row * BLOCK_HEIGHT;
								reflectX(i);
								reflectY(i);
								hit(row, col - 1);
								hit(row, col + 1);
							}
						}
					}
					//Åõ
					//  Å_Å†
					//  Å†
					else if (row2 == row - 1 && col2 == col - 1)
					{
						if (isSolidBlock(row - 1, col) && isSolidBlock(row, col - 1))
						{
							if (ballVX[i]>0&&ballVY[i]>0)
							{
								ballX[i] = col * BLOCK_WIDTH;
								ballY[i] = row * BLOCK_HEIGHT;
								reflectX(i);
								reflectY(i);
								hit(row - 1, col);
								hit(row, col - 1);
							}
						}
					}
				}
				if (ballY2[i] <= racketY && ballY[i] >= racketY)
				{
					if ((ballX[i] >= racketX && ballX[i] <= racketX + RACKET_WIDTH) || (ballX2[i] >= racketX && ballX2[i] <= racketX + RACKET_WIDTH))
					{
						if (ballVY[i] > 0)
						{
							ballY[i] = racketY;
							ballVX[i] += racketVX + (ballX[i] - racketX - RACKET_WIDTH / 2) / 4;
							ballVY[i] = -MAX_BALL_SPEED;
						}
					}
				}
				if (ballVX[i] < -MAX_BALL_SPEED)
				{
					ballVX[i] = -MAX_BALL_SPEED;
				}
				else if (ballVX[i] > MAX_BALL_SPEED)
				{
					ballVX[i] = MAX_BALL_SPEED;
				}
				if (ballVY[i] < -MAX_BALL_SPEED)
				{
					ballVY[i] = -MAX_BALL_SPEED;
				}
				else if (ballVY[i] > MAX_BALL_SPEED)
				{
					ballVY[i] = MAX_BALL_SPEED;
				}
				i++;
			}
		}
		racketX2 = racketX;
		racketX = mouseX - RACKET_WIDTH / 2;
		if (racketX < BLOCK_WIDTH)
		{
			racketX = BLOCK_WIDTH;
		}
		else if (racketX > SCREEN_WIDTH - BLOCK_WIDTH-RACKET_WIDTH)
		{
			racketX = SCREEN_WIDTH - BLOCK_WIDTH - RACKET_WIDTH;
		}
		for (int i = NUM_RACKET_DX - 1; i >= 1; i--)
		{
			racketDX[i] = racketDX[i-1];
		}
		racketDX[0] = racketX - racketX2;
		{
			double total = 0;
			for (int i = 0; i < NUM_RACKET_DX; i++)
			{
				total += racketDX[i];
			}
			racketVX = total / NUM_RACKET_DX;
		}
	}
	
	void addBall(double x, double y, double vx, double vy)
	{
		if (numBalls == MAX_NUM_BALLS)
		{
			return;
		}
		ballX[numBalls] = ballX2[numBalls] = x;
		ballY[numBalls] = ballX2[numBalls] = y;
		ballVX[numBalls] = vx;
		ballVY[numBalls] = vy;
		numBalls++;
	}
	
	void remBall(int index)
	{
		numBalls--;
		ballX[index] = ballX[numBalls];
		ballY[index] = ballY[numBalls];
		ballX2[index] = ballX2[numBalls];
		ballY2[index] = ballY2[numBalls];
		ballVX[index] = ballVX[numBalls];
		ballVY[index] = ballVY[numBalls];
	}
	
	void reflectX(int index)
	{
		ballVX[index] = -ballVX[index];
		ballVX[index] *= 0.75;
	}
	
	void reflectY(int index)
	{
		ballVY[index] = -ballVY[index];
		ballVY[index] *= 0.75;
	}
	
	void hit(int row, int col)
	{
		if (row < 0 || row >= NUM_BLOCK_ROWS || col < 0 || col >= NUM_BLOCK_COLS)
		{
			return;
		}
		switch (blocks[row][col] / 10000)
		{
			case 1:
				blocks[row][col] = 0;
				break;
		}
	}
	
	boolean isSolidBlock(int row, int col)
	{
		if (row < 0 || row >= NUM_BLOCK_ROWS || col < 0 || col >= NUM_BLOCK_COLS)
		{
			return false;
		}
		switch (blocks[row][col] / 10000)
		{
			case 1:
			case 2:
				return true;
		}
		return false;
	}
	
	int rowOf(double y)
	{
		return (int)(y / BLOCK_HEIGHT);
	}
	
	int colOf(double x)
	{
		return (int)(x / BLOCK_WIDTH);
	}
	
	public void init()
	{
		{
			Class c = getClass();
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			MediaTracker tracker = new MediaTracker(this);
			imgBlock = new Image[12];
			for (int i = 0; i < 12; i++)
			{
				String n = null;
				if (i < 10)
				{
					n = "0" + i;
				}
				else
				{
					n = Integer.toString(i);
				}
				imgBlock[i] = toolkit.getImage(c.getResource("images/block" + n + ".gif"));
				tracker.addImage(imgBlock[i], 0);
			}
			imgSolid = toolkit.getImage(c.getResource("images/solid.gif"));
			tracker.addImage(imgSolid, 0);
			imgBall = toolkit.getImage(c.getResource("images/ball.gif"));
			tracker.addImage(imgBall, 0);
			imgRacket = toolkit.getImage(c.getResource("images/racket.gif"));
			tracker.addImage(imgRacket, 0);
			try
			{
				tracker.waitForID(0);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
		}
		addMouseListener(this);
		addMouseMotionListener(this);
		appAlive = true;
		new Thread(this).start();
	}
	
	public void destroy()
	{
		appAlive = false;
		System.out.println("Ç®îÊÇÍólÇ≈ÇµÇΩÅB");
	}
	
	public void paint(Graphics g)
	{
		if (bufImg == null)
		{
			bufImg = createImage(SCREEN_WIDTH, SCREEN_HEIGHT);
			if (bufImg == null)
			{
				return;
			}
			bufGfx = bufImg.getGraphics();
		}
		for (int row = 0; row < NUM_BLOCK_ROWS; row++)
		{
			for (int col = 0; col < NUM_BLOCK_COLS; col++)
			{
				switch (blocks[row][col] / 10000)
				{
					case 0:
						bufGfx.setColor(Color.black);
						bufGfx.fillRect(col * BLOCK_WIDTH, row * BLOCK_HEIGHT, BLOCK_WIDTH, BLOCK_HEIGHT);
						break;
					case 1:
						bufGfx.drawImage(imgBlock[blocks[row][col] - 10000], col * BLOCK_WIDTH, row * BLOCK_HEIGHT, this);
						break;
					case 2:
						bufGfx.drawImage(imgSolid, col * BLOCK_WIDTH, row * BLOCK_HEIGHT, this);
						break;
				}
			}
		}
		{
			int x = (int)Math.round(racketX);
			int y = (int)Math.round(racketY);
			bufGfx.drawImage(imgRacket, x, y, this);
		}
		for (int i = 0; i < numBalls; i++)
		{
			int x = (int)Math.round(ballX[i]);
			int y = (int)Math.round(ballY[i]);
			bufGfx.drawImage(imgBall, x - 8, y - 8, this);
		}
		g.drawImage(bufImg, 0, 0, this);
	}
	
	public void update(Graphics g)
	{
		paint(g);
	}
	
	public void run()
	{
		while (appAlive)
		{
			tick();
			repaint();
			try
			{
				if (turbo)
				{
					Thread.sleep(8);
				}
				else
				{
					Thread.sleep(16);
				}
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	public void windowOpened(WindowEvent e)
	{
	}
	
	public void windowClosing(WindowEvent e)
	{
		stop();
		destroy();
		System.exit(0);
	}
	
	public void windowClosed(WindowEvent e)
	{
	}
	
	public void windowIconified(WindowEvent e)
	{
	}
	
	public void windowDeiconified(WindowEvent e)
	{
	}
	
	public void windowActivated(WindowEvent e)
	{
	}
	
	public void windowDeactivated(WindowEvent e)
	{
	}
	
	public void mouseClicked(MouseEvent e)
	{
	}
	
	public void mousePressed(MouseEvent e)
	{
		switch (e.getButton())
		{
			case MouseEvent.BUTTON1:
				addBall(racketX + RACKET_WIDTH / 2, racketY, racketVX, -4);
				break;
			case MouseEvent.BUTTON3:
				turbo = true;
				break;
		}
	}
	
	public void mouseReleased(MouseEvent e)
	{
		switch (e.getButton())
		{
			case MouseEvent.BUTTON3:
				turbo = false;
				break;
		}
	}
	
	public void mouseEntered(MouseEvent e)
	{
	}
	
	public void mouseExited(MouseEvent e)
	{
	}
	
	public void mouseDragged(MouseEvent e)
	{
		mouseMoved(e);
	}
	
	public void mouseMoved(MouseEvent e)
	{
		mouseX = e.getX();
	}
	
	public static void main(String[] args)
	{
		Block block = new Block();
		Frame frame = new Frame("Block");
		frame.setBackground(Color.black);
		frame.pack();
		frame.setVisible(true);
		frame.setVisible(false);
		frame.pack();
		frame.setResizable(false);
		frame.pack();
		{
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			Insets i = frame.getInsets();
			frame.setLocation((d.width + i.left + i.right - SCREEN_WIDTH) / 2, (d.height + i.top + i.bottom - SCREEN_HEIGHT) / 2);
			frame.setSize(SCREEN_WIDTH + i.left + i.right, SCREEN_HEIGHT + i.top + i.bottom);
		}
		frame.addWindowListener(block);
		block.init();
		block.start();
		frame.add("Center", block);
		frame.setVisible(true);
	}
}
