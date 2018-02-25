package GUI;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import BenchMarking.BenchMarkID;

/** La classe fornisce la possibilita' di elencare tutte le query di un
 * benchmark e di mostrare quanti documenti rilevanti sono stati trovati
 * per ciascuna query del benchmark
 */

public class BenchmarkModePanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 8377960691553435443L;

	private static final Color BACKGROUND_COLOR = new Color(117,219,144);
	
	private Image logo ;
	private static final int LOGO_OFFSET_X = 80;
	private static final int LOGO_INVERSE_OFFSET_Y = 70;
	private static final int LOGO_WIDTH = 280;
	private static final int LOGO_HEIGHT = 50;
	
	private static final int MAX_PATH_LEN = 40;
	
	//private static final int QUERY_AREA_ROW = 5;
	//private static final int QUERY_AREA_COLUMN = 51;
	private static final int RESULT_AREA_ROW = 29;
	private static final int RESULT_AREA_COLUMN = 61;
	
	/* Permette di eseguire i primi X0 query, la selezione
	 * di una query per utilizzarla è solo per fare test,
	 * questa interfaccia è più finalizzata ad automatizzare
	 * il processo di valutazione */
	private static final int MAX_NUM_QUERY = 50;
	
	private JLabel queryLb,benchPathLb,indexedPathLb, benchMarkLb;
	private JTextField benchPathTxt,indexPathTxt;
	private JButton loadBtn,benchFcBtn,indexFcBtn;
	private JFileChooser benchFc, indexFc;
	private JComboBox<BenchMarkID> benchMarkCB;
	private JTextArea resultArea;
	//private JTextArea queryArea;
	//private JComboBox<Integer> queryNumCB;
	/* E' piu' opportuno un JTextArea in cui si lascia scegliere il numero
	 * di query e in base al numero selezionato appare la query  */
	
	/**
	 * 
	 */
	public BenchmarkModePanel() {
		super();
		
		//queryLb = new JLabel("Query:");
		benchPathLb = new JLabel("Benchmark path:");
		indexedPathLb = new JLabel("Index path:");
		benchMarkLb = new JLabel("Select Benchmark: ");
		//queryLb.setBackground(BACKGROUND_COLOR);
		benchPathLb.setBackground(BACKGROUND_COLOR);
		indexedPathLb.setBackground(BACKGROUND_COLOR);
		benchMarkLb.setBackground(BACKGROUND_COLOR);
		//queryLb.setForeground(Color.WHITE);
		benchPathLb.setForeground(Color.WHITE);
		indexedPathLb.setForeground(Color.WHITE);
		benchMarkLb.setForeground(Color.WHITE);
		//queryNumCB = new JComboBox<Integer>();
		//for (int i = 1 ; i < MAX_NUM_QUERY+1 ; i++)
		//	queryNumCB.addItem(new Integer(i));
		
		benchMarkCB = new JComboBox<BenchMarkID>(BenchMarkID.values());
		
		//queryArea = new JTextArea(QUERY_AREA_ROW,QUERY_AREA_COLUMN);
		resultArea = new JTextArea(RESULT_AREA_ROW,RESULT_AREA_COLUMN);
		//queryArea.setEditable(false);
		resultArea.setEditable(false);
		//JScrollPane scrollQuery = new JScrollPane(queryArea);
		//scrollQuery.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane scrollResult = new JScrollPane(resultArea);
		scrollResult.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		benchPathTxt = new JTextField("",MAX_PATH_LEN);
		benchPathTxt.setEditable(false);
		indexPathTxt = new JTextField("",MAX_PATH_LEN);
		indexPathTxt.setEditable(false);
		loadBtn = new JButton("Load benchmark");
		loadBtn.addActionListener(this);
		benchFcBtn = new JButton("Choose");
		benchFcBtn.addActionListener(this);
		indexFcBtn = new JButton("Choose");
		indexFcBtn.addActionListener(this);
		
		benchFc = new JFileChooser("./");
		indexFc = new JFileChooser("./");
		benchFc.setDialogTitle("Select benchmark");
		indexFc.setDialogTitle("Select benchmark index");
		benchFc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		indexFc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		benchFc.setAcceptAllFileFilterUsed(false);
		indexFc.setAcceptAllFileFilterUsed(false);
		
		setLayout(new FlowLayout(FlowLayout.TRAILING));
		//add(queryLb); add(queryNumCB); add(scrollQuery);
		add(benchPathLb); add(benchPathTxt); add(benchFcBtn);
		add(indexedPathLb); add(indexPathTxt); add(indexFcBtn);
		add(benchMarkLb); add(benchMarkCB); add(loadBtn); add(scrollResult);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source ==  benchFcBtn) {
			if (benchFc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				String benchPath = benchFc.getSelectedFile().getPath();
				benchPathTxt.setText(benchPath);
			}
		} if (source == indexFcBtn) {
			if (indexFc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				String indexPath = indexFc.getSelectedFile().getPath();
				indexPathTxt.setText(indexPath);
			}
		} if (source == loadBtn && 
				!(benchPathTxt.getText().equals("") || 
				  indexPathTxt.getText().equals(""))) {
			resultArea.setText("Testing and producing results...");
		}
		
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(BACKGROUND_COLOR) ;
		g.fillRect(0, 0, getWidth(), getHeight()) ;
		
		Toolkit t = Toolkit.getDefaultToolkit() ;
		if (!(new File("./img/poweredbylucene.png").exists())) {
			System.err.println("Logo file path error") ;
			System.exit(-1);
		}
		logo = t.getImage("./img/poweredbylucene.png") ;
		MediaTracker m = new MediaTracker(this) ;
		m.addImage(logo, 1) ;
		try { m.waitForAll(); 
		} catch (InterruptedException e) {
			System.err.println("Logo not loaded") ;
			System.exit(-1);
		}
		
		g.drawImage(logo, LOGO_OFFSET_X, getHeight()-LOGO_INVERSE_OFFSET_Y,
						  LOGO_WIDTH, LOGO_HEIGHT, null) ;
	}
}
