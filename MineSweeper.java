package MineSweeper;

import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.net.URL;
import java.io.*;
import javax.sound.sampled.*;
import javax.swing.*;

/**
 * The Mine Sweeper Game.
 * Left-click to reveal a cell.
 * Right-click to plant/remove a flag for marking a suspected mine.
 * You win if all the cells not containing mines are revealed.
 * You lose if you reveal a cell containing a mine.
 */
@SuppressWarnings("serial")
public class MineSweeper extends JFrame implements ActionListener {
	// Name-constants for the game properties
	public int ROWS = 10;      // number of cells
	public int COLS = 10;
 
	// Name-constants for UI control (sizes, colors and fonts)
	public static int CELL_SIZE = 40;  // Cell width and height, in pixels
	public int CANVAS_WIDTH = CELL_SIZE * COLS; // Game board width/height
	public int CANVAS_HEIGHT = CELL_SIZE * ROWS;
	public static final Color BGCOLOR_NOT_REVEALED = Color.WHITE;
	public static final Color FGCOLOR_NOT_REVEALED = Color.RED;    // flag
	public static final Color BGCOLOR_REVEALED = Color.LIGHT_GRAY;
	public static final Color FGCOLOR_REVEALED = Color.DARK_GRAY; // number of mines
	public static final Font FONT_NUMBERS = new Font("Monospaced", Font.BOLD, 20);
	public static JLabel Time, remainMine, d_level;
	public static JButton btnPause, btnSound;
	public Container cp;
    public JPanel mainPanel;
    public JPanel side;
    
	// Buttons for user interaction
	private JButton btnCells[][] = new JButton[ROWS][COLS];
	private JLabel jlbremain, jlbtime, jlblevel, jlbsound;
	// Number of mines in this game. Can vary to control the difficulty level.
	private int[] NumOfMines = {10, 30, 80};
	private int[] rowNum = {10, 12, 16};
	private int[] colNum = {10, 15, 25};
	private int numMines=NumOfMines[0], open=0, HintLeft=3, minesLeft=numMines, timeCost=0, difficult=0, num;
	// Location of mines. True if mine is present on this cell.
	private boolean mines[][] = new boolean[ROWS][COLS];
	// User can right-click to plant/remove a flag to mark a suspicious cell
	private boolean flags[][] = new boolean[ROWS][COLS];
	private boolean visit[][] = new boolean[ROWS][COLS];
	private int mines_around[][] = new int[ROWS][COLS];
	private boolean FirstClick = true, pause = false;
	
	//private Timer cntTime = new Timer(true); 
	private String imgplay = "icon/play.png";
	private String imgpause = "icon/stop.png";
	private String imgflag = "icon/flag.png";
	private String imgmine = "icon/mine.png";
	private String imgmute = "icon/mute.png";
	private String imgsound = "icon/sound.png";
	private ImageIcon iconplay = null;
    private ImageIcon iconpause = null;
    private ImageIcon iconflag = null;
    private ImageIcon iconmine = null;
    private ImageIcon iconmute = null;
    private ImageIcon iconsound = null;
    private String bgm = "sound/Start Over.wav";
    private String sound = "sound/explosion.wav";
    private Clip clip, clip_;
    private String[] record = {"/Users/macbook/eclipse-workspace/MineSweeper/bin/record/e_record.txt", 
    							"/Users/macbook/eclipse-workspace/MineSweeper/bin/record/i_record.txt", 
    							"/Users/macbook/eclipse-workspace/MineSweeper/bin/record/h_record.txt"};
    private CellMouseListener listener = new CellMouseListener();
    private String[][] user = new String[3][10];
    private int[][] score = new int[3][10];
    
    Timer cntTime = new Timer(true);
 
	// Constructor to set up all the UI and game components
	public MineSweeper() {
		addMenu();
		bgm();
		cp = this.getContentPane();           // JFrame's content-pane
		mainPanel = new JPanel(new GridLayout(ROWS, COLS, 2, 2));
		cp.add(mainPanel); // in GridLayout
		addStatusBar();
		addRanking();

        URL imgURL = getClass().getClassLoader().getResource(imgplay);
        iconplay = new ImageIcon(imgURL);
        btnPause.setIcon(iconplay);
        imgURL = getClass().getClassLoader().getResource(imgpause);
        iconpause = new ImageIcon(imgURL);
        imgURL = getClass().getClassLoader().getResource(imgflag);
        iconflag = new ImageIcon(imgURL);
        imgURL = getClass().getClassLoader().getResource(imgmine);
        iconmine = new ImageIcon(imgURL);
        imgURL = getClass().getClassLoader().getResource(imgmute);
        iconmute = new ImageIcon(imgURL);
        imgURL = getClass().getClassLoader().getResource(imgsound);
        iconsound = new ImageIcon(imgURL);
        btnSound.setIcon(iconsound);
        clip.loop(Clip.LOOP_CONTINUOUSLY); 
        
		// Allocate a common instance of listener as the MouseEvent listener for all the JButtons
		//[TODO 3] (later)
		//CellMouseListener listener = new CellMouseListener();
 
		// Construct JButtons and add to the content-pane
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				btnCells[row][col] = new JButton();  // Allocate each JButton of the array
				mainPanel.add(btnCells[row][col]);          // add to content-pane in GridLayout
 
				// Add MouseEvent listener to process the left/right mouse-click
				// ... [TODO 4] (later) ...
				btnCells[row][col].addMouseListener(listener);
			}
		}
 
		// Set the size of the content-pane and pack all the components under this container.
		mainPanel.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
		pack();
 		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // handle window-close button
		setTitle("Mineswepper");
		setVisible(true);   // show it
		setResizable(false);
 		
		// Initialize for a new game
		initGame();
	}
	
	//Layout of menu
	public void addMenu() {
		//Menu Bar
		JMenuBar menu=new JMenuBar();  
		this.setJMenuBar(menu);
		        
		//File
		JMenu file = new JMenu("File");
		String[] f_name={"New Game","Reset Game","Exit"};
		JMenuItem[] f_choice=new JMenuItem[f_name.length];  
		for(int i=0;i<f_name.length;i++){  
			f_choice[i]=new JMenuItem(f_name[i]);  
			f_choice[i].addActionListener(this);   
			file.add(f_choice[i]);  
		}
		menu.add(file);
			        
		//Option: choice of difficult levels
		JMenu option = new JMenu("Option");  
		ButtonGroup bgp=new ButtonGroup();
		String[] d_name={"easy","intermediate","hard"};  
		JRadioButtonMenuItem[] d_choice=new JRadioButtonMenuItem[d_name.length];  
		for(int i=0;i<d_name.length;i++){  
			d_choice[i]=new JRadioButtonMenuItem(d_name[i],true);  
			d_choice[i].addActionListener(this);  
			bgp.add(d_choice[i]);  
			option.add(d_choice[i]);  
		}  
		menu.add(option);
			        
		//Help menu
		JMenu help=new JMenu("Help"); 
		JMenuItem rule=new JMenuItem("Rule");
		JMenuItem hint=new JMenuItem("Hints");
		rule.setActionCommand("view");  
		rule.addActionListener(this); 
		hint.addActionListener(this);
		help.add(rule);
		help.add(hint);
		menu.add(help);
	}
	
	public void addStatusBar() {
		jlbremain = new JLabel("Remaining mines: ");  
        remainMine = new JLabel(minesLeft + "  ");  
        
        jlbtime = new JLabel("Time: ");  
        Time = new JLabel("00:00  ");
        
        btnPause = new JButton();
        btnPause.setEnabled(true);
        btnPause.setPreferredSize(new Dimension(16, 16));
        btnPause.setOpaque(true);
        btnPause.setActionCommand("||");
        btnPause.setVerticalAlignment(SwingConstants.CENTER);
        btnPause.setHorizontalAlignment(SwingConstants.CENTER);
        btnPause.addActionListener(this);
        
        jlblevel = new JLabel("  Level: ");
        d_level = new JLabel("easy");
        
        jlbsound = new JLabel("  BGM: ");
        btnSound = new JButton();
        btnSound.setEnabled(true);
        btnSound.setPreferredSize(new Dimension(16, 16));
        btnSound.setOpaque(true);
        btnSound.setActionCommand("pause");
        btnSound.setVerticalAlignment(SwingConstants.CENTER);
        btnSound.setHorizontalAlignment(SwingConstants.CENTER);
        btnSound.addActionListener(this); 
        
        JPanel p = new JPanel();
        p.add(jlbremain);
        p.add(remainMine);
        p.add(jlbtime);
        p.add(Time);
        p.add(btnPause);
        p.add(jlblevel);
        p.add(d_level);
        p.add(jlbsound);
        p.add(btnSound);
        
        cp.add(p, BorderLayout.NORTH);
	}
	
	public void addRanking() {
		if(side!=null) cp.remove(side);;
		side = new JPanel();
		JLabel[] rank = new JLabel[10];
		JLabel title = new JLabel("Ranking list");
		title.setFont(new Font("Courier", Font.BOLD, 14));
		side.add(title);
		side.setBackground(Color.LIGHT_GRAY);
		
		updateRanking();
		for (int i=0; i<=9; i++) {
			rank[i] = new JLabel();
			if (score[difficult][i]!=0) {
				int min, sec;
				String Strmin, Strsec;
				min=score[difficult][i]/60;
				sec=score[difficult][i]-min*60;
				if (min<10) Strmin = "0" + min;
				else Strmin = "" + min;
				if (sec<10) Strsec = "0" + sec;
				else Strsec = "" + sec;
				rank[i].setText((i+1) + ".  " + user[difficult][i] + " " + Strmin + ":" + Strsec);
			}
				
			else rank[i].setText((i+1) + ".  null " + " 00:00");
			side.add(rank[i]);
		}
		
		side.setPreferredSize(new Dimension(100, ROWS*CELL_SIZE));
		cp.add(side, BorderLayout.EAST);
		pack();
	}
	
	public void updateRanking() {
		File file = new File(record[difficult]);
		Reader reader = null;
		num=0;
		try {
			reader = new InputStreamReader(new FileInputStream(file));
			int tempchar=reader.read();
			while (tempchar!= -1) {
				String player = "";
				String tempScore = "";
				while (((char) tempchar) != '*') {
					player += (char) tempchar;
					tempchar = reader.read();
				}
				user[difficult][num] = player;
				tempchar = reader.read();
				while (((char) tempchar) != '\n' && (tempchar != -1)) {
					tempScore += (char) tempchar;
					tempchar = reader.read();
				}
				score[difficult][num] = Integer.parseInt(tempScore);
				tempchar = reader.read();
				num++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {    
        if(e.getActionCommand().equals("easy")) {
        		difficult=0;
        		numMines=NumOfMines[difficult];
        		ROWS=rowNum[difficult]; COLS=colNum[difficult]; 
        		
        		remainMine.setText(numMines + "  ");
        		d_level.setText("easy");
        		
        		newBoard(ROWS, COLS);
        		initGame();
            return ;  
        }  
        if(e.getActionCommand().equals("intermediate")) {
        		difficult=1;
        		numMines=NumOfMines[difficult];
        		ROWS=rowNum[difficult]; COLS=colNum[difficult]; 
        		
        		remainMine.setText(numMines + "  ");
        		d_level.setText("intermediate");
        		
        		newBoard(ROWS, COLS);
        		initGame(); 
            return ; 
        }  
        if(e.getActionCommand().equals("hard")) {
        		difficult=2;
        		numMines=NumOfMines[difficult];
        		ROWS=rowNum[difficult]; COLS=colNum[difficult];
        		
        		remainMine.setText(numMines + "  ");
        		d_level.setText("hard");
        		
        		newBoard(ROWS, COLS);
        		initGame(); 
            return ;
        }
        
        if(e.getActionCommand().equals("New Game")) { //restart with same difficult level
        		initGame();
	    		return;
	    } 
        if(e.getActionCommand().equals("Reset Game")) { //restart with easy level
	        	numMines=NumOfMines[0];
	    		ROWS=rowNum[0]; COLS=colNum[0]; 
	        	
	        	remainMine.setText(numMines + "  ");
	    		d_level.setText("easy");
	    		
	    		newBoard(ROWS, COLS);
	    		initGame();
	        return ;  
	    }
        if(e.getActionCommand().equals("Exit")) {
	    		System.exit(0);
	    }
        
        if(e.getActionCommand().equalsIgnoreCase("rule")){  
            JOptionPane.showMessageDialog(null, " * The Mine Sweeper Game.\n" + 
            		" * Left-click to reveal a cell.\n" + 
            		" * Right-click to plant/remove a flag for marking a suspected mine.\n" + 
            		" * You win if all the cells not containing mines are revealed.\n" + 
            		" * You lose if you reveal a cell containing a mine.");  
        }
        if(e.getActionCommand().equals("Hints")){
        		if (HintLeft>0) {
        			HintLeft--;
        			boolean success=false;
        			
        			while (success==false) {
        				Random rand = new Random();
        				int x=rand.nextInt(ROWS);
        				int y=rand.nextInt(COLS);
        				if (mines[x][y]==false && visit[x][y]==false) { reveal(x,y); success=true; }
        			}
        			JOptionPane.showMessageDialog(null, "You have " + HintLeft + " hint(s) left.");
        		}
        		else JOptionPane.showMessageDialog(null, "Sorry, no hints left.");
        }
        
        if(e.getActionCommand().equals("||")) {
        		if (!FirstClick) {
        			btnPause.setIcon(iconplay);
        			btnPause.setActionCommand(">");
        			btnPause.setVerticalAlignment(SwingConstants.CENTER);
        			btnPause.setHorizontalAlignment(SwingConstants.CENTER);
        			pause=true;
        		}
        }
        if(e.getActionCommand().equals(">")) {
        		if (!FirstClick) {
        			btnPause.setIcon(iconpause);
        			btnPause.setActionCommand("||");
        			btnPause.setVerticalAlignment(SwingConstants.CENTER);
        			btnPause.setHorizontalAlignment(SwingConstants.CENTER);
        			pause=false;
        		}
        }
        
        if(e.getActionCommand().equals("pause")) {
	    		btnSound.setIcon(iconmute);
	    		btnSound.setActionCommand("play");
	    		btnSound.setVerticalAlignment(SwingConstants.CENTER);
	    		btnSound.setHorizontalAlignment(SwingConstants.CENTER);
	    		clip.stop();
	    }
	    if(e.getActionCommand().equals("play")) {
			btnSound.setIcon(iconsound);
			btnSound.setActionCommand("pause");
	    		btnSound.setVerticalAlignment(SwingConstants.CENTER);
	    		btnSound.setHorizontalAlignment(SwingConstants.CENTER);
	    		clip.loop(Clip.LOOP_CONTINUOUSLY);
	    }
    }  
	
	public void bgm() {
		try {
			// Open an audio input stream.
			URL url = this.getClass().getClassLoader().getResource(bgm);
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
			// Get a sound clip resource.
			clip = AudioSystem.getClip();
			// Open audio clip and load samples from the audio input stream.
			clip.open(audioIn);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		
		try {
			// Open an audio input stream.
			URL url = this.getClass().getClassLoader().getResource(sound);
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
			// Get a sound clip resource.
			clip_ = AudioSystem.getClip();
			// Open audio clip and load samples from the audio input stream.
			clip_.open(audioIn);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	public void TimeStart() {
		cntTime.cancel();
		cntTime = new Timer(true);
		TimerTask task = new TimerTask() {
			public void run() {
				if (!pause) {
					int min, sec;
					timeCost++;
	        			String Strmin, Strsec;
	        			min=timeCost/60;
	        			if (min<10) Strmin = "0" + min;
	        			else Strmin = min + "";
	        			sec=timeCost-min*60;
	        			if (sec<10) Strsec = "0" + sec;
	        			else Strsec = sec + "";
					Time.setText(Strmin + ":" + Strsec + "  ");
				}
			}
		};
		
		cntTime.schedule(task, 0, 1000);
	}
	
	public void saveRecord(String name) {		
		updateRanking();
		
		FileWriter writer;
		try {
			writer = new FileWriter(record[difficult]);
			int i=0, j=0;
			if (num>9) num=9;
			else if(num==0) {
				writer.write(name + "*");
				writer.write(timeCost + "\n");
			}
			else if (i==num && j==0) {
				writer.write(name + "*");
				writer.write(timeCost + "\n");
			}
			else while (i+j<=num && i<num) {
				if (timeCost<score[difficult][i] && j==0) {
					writer.write(name + "*");
					writer.write(timeCost + "\n");
					j=1;
				}
				else {
					writer.write(user[difficult][i] + "*");
					writer.write(score[difficult][i] + "\n");
					i++;
				}
			}
			if (i==num && j==0 && num!=0) {
				writer.write(name + "*");
				writer.write(timeCost + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int cntMinesAround(int x, int y) {
		int p=0;
		for (int i=-1; i<=1; i++) 
			for (int j=-1; j<=1; j++)
				if (x+i<ROWS && x+i>=0 && y+j<COLS && y+j>=0)
					if (mines[x+i][y+j]==true) p++;
		return p;
	}
	
	//recursive function to reveal the cell
	private void reveal(int x, int y) {
		int p=0;
		visit[x][y]=true;
		for (int i=-1; i<=1; i++) 
			for (int j=-1; j<=1; j++)
				if (x+i<ROWS && x+i>=0 && y+j<COLS && y+j>=0)
					if (flags[x+i][y+j]==true) p++;
				
							
		btnCells[x][y].setEnabled(false);
		btnCells[x][y].setForeground(FGCOLOR_REVEALED);
		btnCells[x][y].setBackground(BGCOLOR_REVEALED);
		btnCells[x][y].setOpaque(true);
		if (mines_around[x][y]!=0) btnCells[x][y].setText(mines_around[x][y] + ""); 
		
		if (mines_around[x][y]==p) {
			for (int i=-1; i<=1; i++) 
				for (int j=-1; j<=1; j++)
					if (x+i<ROWS && x+i>=0 && y+j<COLS && y+j>=0)
						if (!(i==0 && j==0) && visit[x+i][y+j]==false && flags[x+i][y+j]==false)
							reveal(x+i, y+j);
		}
		
		open++;
	}
	
	private void win() {
		pause=true;
		btnPause.setIcon(iconplay);
		btnPause.setActionCommand(">");
		btnPause.setVerticalAlignment(SwingConstants.CENTER);
		btnPause.setHorizontalAlignment(SwingConstants.CENTER);
		remainMine.setText("0  ");
		
		int min, sec;
		String Strmin, Strsec;
		min=timeCost/60;
		if (min<10) Strmin = "0" + min;
		else Strmin = min + "";
		sec=timeCost-min*60;
		if (sec<10) Strsec = "0" + sec;
		else Strsec = sec + "";
		JOptionPane.showMessageDialog(null, "You win!\n" + 
				"Time Consuming: " + Strmin + ":" + Strsec);
		String name = JOptionPane.showInputDialog("Please key in your name:");
		saveRecord(name);

		initGame();
	}
	
	private void gameover() { 
		pause=true;
		btnPause.setIcon(iconplay);
		btnPause.setActionCommand(">");
		btnPause.setVerticalAlignment(SwingConstants.CENTER);
		btnPause.setHorizontalAlignment(SwingConstants.CENTER);
		
		clip_.setFramePosition(0);
		clip_.start();
		
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				//reveal all the remaining cells
				btnCells[row][col].setEnabled(false);  // disable button
				btnCells[row][col].setForeground(FGCOLOR_REVEALED);
				btnCells[row][col].setBackground(BGCOLOR_REVEALED);
				btnCells[row][col].setFont(FONT_NUMBERS);
				if (mines[row][col]) {
					btnCells[row][col].setIcon(iconmine);  // display mines
					btnCells[row][col].setVerticalAlignment(SwingConstants.CENTER);
					btnCells[row][col].setHorizontalAlignment(SwingConstants.CENTER);
				}
				else btnCells[row][col].setIcon(null);
			}
		}
		JOptionPane.showMessageDialog(null, "Game over!");
		initGame();
	}
		
	private void newBoard(int x, int y) {
		cp.remove(mainPanel);
		mainPanel = new JPanel(new GridLayout(x, y, 2, 2));
		int w = CELL_SIZE * y;
		int h = CELL_SIZE * x;
		mainPanel.setPreferredSize(new Dimension(w,h));
		cp.add(mainPanel);
		
		btnCells = new JButton[ROWS][COLS];
		mines = new boolean[ROWS][COLS];
		flags = new boolean[ROWS][COLS];
		visit = new boolean[ROWS][COLS];
		mines_around = new int[ROWS][COLS];
		
		// Construct JButtons and add to the content-pane
		for (int row = 0; row < x; row++) {
			for (int col = 0; col < y; col++) {
				btnCells[row][col] = new JButton();  // Allocate each JButton of the array
				mainPanel.add(btnCells[row][col]);          // add to content-pane in GridLayout
 
				// Add MouseEvent listener to process the left/right mouse-click
				btnCells[row][col].addMouseListener(listener);
			}
		}

		pack();
		cp.validate();
	}
	
	// Initialize and re-initialize a new game
	private void initGame() {
		// Reset hints & open
		HintLeft=3; open=0; 
		// Reset Time
		timeCost=0;
		Time.setText("00:00  ");
		pause=true;
		FirstClick=true;
		minesLeft=numMines;
		remainMine.setText(minesLeft+"  ");
		// Reset cells, mines, and flags
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				// Set all cells to un-revealed
				btnCells[row][col].setEnabled(true);  // enable button
				btnCells[row][col].setForeground(FGCOLOR_NOT_REVEALED);
				btnCells[row][col].setBackground(BGCOLOR_NOT_REVEALED);
				btnCells[row][col].setOpaque(true);
				btnCells[row][col].setFont(FONT_NUMBERS);
				btnCells[row][col].setText("");
				btnCells[row][col].setIcon(null);     // display blank
				mines[row][col] = false;   // clear all the mines
				flags[row][col] = false;   // clear all the flags
				visit[row][col] = false;
			}
		}
 
		// Randomly generate mines and mine's location
		int num=0;
		Random rand = new Random();
		while (num<numMines) {
			int x=rand.nextInt(ROWS);
			int y=rand.nextInt(COLS);
			if (!mines[x][y]) {
				mines[x][y]=true;
				num++;
			}
		}
		
		for (int row = 0; row < ROWS; row++) 
			for (int col = 0; col < COLS; col++) 
				mines_around[row][col]=cntMinesAround(row, col);
		addRanking();
	}

	// The entry main() method
	public static void main(String[] args) {
		// [TODO 1] (Now)
		new MineSweeper();
	}
	
	// Define the Listener Inner Class
	private class CellMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			// Determine the (row, col) of the JButton that triggered the event
			int rowSelected = -1;
			int colSelected = -1;
	 
			// Get the source object that fired the Event
			JButton source = (JButton)e.getSource();
	        // Scan all rows and columns, and match with the source object
	        boolean found = false;
	        for (int row = 0; row < ROWS && !found; ++row) {
	        		for (int col = 0; col < COLS && !found; ++col) {
	        			if (source == btnCells[row][col]) {
	        				rowSelected = row;
	        				colSelected = col;
	        				found = true;   // break both inner/outer loops
	        			}
	        		}
	        	}
	 
	        // Left-click to reveal a cell; Right-click to plant/remove the flag.
	        if (e.getButton() == MouseEvent.BUTTON1) {  // Left-button clicked
	        		if (FirstClick) TimeStart();
	        		if (pause) {
	        			pause=false;
	        			btnPause.setIcon(iconpause);
	        			btnPause.setActionCommand("||");
	            		btnPause.setVerticalAlignment(SwingConstants.CENTER);
	            		btnPause.setHorizontalAlignment(SwingConstants.CENTER);
	        		}
	        		
	        		// If there's a flag on the cell, don't do anything
	        		if (flags[rowSelected][colSelected] || visit[rowSelected][colSelected]);
	        		// [TODO 5] If you hit a mine, game over
	        		else if (mines[rowSelected][colSelected]) {
	        			while (FirstClick) {
	        				initGame(); //if the first revealed cell is mine, reinitialize the game
	        				if (!mines[rowSelected][colSelected]) {
	        					FirstClick=false;
	    	        				reveal(rowSelected,colSelected);
	        				}
	        			}
	        			if (!FirstClick && mines[rowSelected][colSelected]) gameover();
	        		}
	            // Otherwise, reveal the cell and display the number of surrounding mines
	        		else {
	        			FirstClick=false;
	        			reveal(rowSelected,colSelected);
	        		}
	 
	        } else if (e.getButton() == MouseEvent.BUTTON3) { // right-button clicked
	        		if (pause) {
	        			pause=false;
	        			btnPause.setIcon(iconpause);
	        			btnPause.setActionCommand("||");
	            		btnPause.setVerticalAlignment(SwingConstants.CENTER);
	            		btnPause.setHorizontalAlignment(SwingConstants.CENTER);
	        		}
	        		
	        		if (visit[rowSelected][colSelected]);
	            // [TODO 6] If the location is flagged, remove the flag
	        		else if (flags[rowSelected][colSelected]) {
		        		flags[rowSelected][colSelected]=false;
		        		btnCells[rowSelected][colSelected].setIcon(null);
		        		btnCells[rowSelected][colSelected].setVerticalAlignment(SwingConstants.CENTER);
		        		btnCells[rowSelected][colSelected].setHorizontalAlignment(SwingConstants.CENTER);
		        		minesLeft++;
		        		remainMine.setText(minesLeft+"  ");
		        	}
	            // Otherwise, plant a flag.
		        	else {
		        		flags[rowSelected][colSelected]=true;
		        		btnCells[rowSelected][colSelected].setIcon(iconflag);
		        		btnCells[rowSelected][colSelected].setVerticalAlignment(SwingConstants.CENTER);
		        		btnCells[rowSelected][colSelected].setHorizontalAlignment(SwingConstants.CENTER);
		        		minesLeft--;
		        		remainMine.setText(minesLeft+"  ");
		        	}
	        }
	 
	        // [TODO 7] Check if the player has won, after revealing this cell
	        if (open==ROWS*COLS-numMines) win();
		}
	}
}
