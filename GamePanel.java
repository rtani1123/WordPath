import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

	WordPathClient wpc;
	JScrollPane entryPane, userPane;
	JLabel word1Label, word2Label;
	JPanel appPanel;
	JPanel playerMovePanel;
	JPanel wordsPanel;
	JList entryList;
	JList userList;
	JTextField entryField;
	JButton acceptMoveButton, readyButton;
	ArrayList<String> userEntries; //list of current valid user moves
	ArrayList<String> users;
	String currentMove;
	
	GridBagConstraints gbc;
	
	public GamePanel(WordPathClient parent) {
		wpc = parent;
		appPanel = new JPanel();
		appPanel.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(5,5,5,5);
		
		playerMovePanel = new JPanel();
		playerMovePanel.setLayout(new BoxLayout(playerMovePanel, BoxLayout.X_AXIS));
		
		wordsPanel = new JPanel();
		wordsPanel.setLayout(new BoxLayout(wordsPanel, BoxLayout.Y_AXIS));
		word1Label = new JLabel("Word 1: ");
		word2Label = new JLabel("Word 2: ");
		wordsPanel.add(word1Label);
		wordsPanel.add(word2Label);
		
		userEntries = new ArrayList<String>();
		users = new ArrayList<String>();
		entryField = new JTextField(4);
		entryField.requestFocusInWindow();
		readyButton = new JButton("Ready to Play");
		
		acceptMoveButton = new JButton("Accept Move");
		entryList = new JList(userEntries.toArray());
		entryList.setVisibleRowCount(userEntries.size());
		entryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		userList = new JList(users.toArray());
		entryPane = new JScrollPane(entryList);
		entryPane.setPreferredSize(new Dimension(300,400));
		userPane = new JScrollPane(userList);
		userPane.setPreferredSize(new Dimension(100,400));
		
		acceptMoveButton.addActionListener(this);
		readyButton.addActionListener(this);
		entryField.addKeyListener(this);
		
		//List of User Entries
		
		add(appPanel);
		
		//GUI initialization
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		appPanel.add(wordsPanel);
		gbc.gridx = 1;
		appPanel.add(new JLabel("Current Users:"));
		gbc.gridx = 0;
		gbc.gridy = 1;
		appPanel.add(entryPane, gbc);
		gbc.gridx = 1;
		appPanel.add(userPane, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		appPanel.add(readyButton, gbc);
		updateUsers();
	}
	public void allowUserEntry() {
		gbc.gridy = 2;
		appPanel.add(playerMovePanel, gbc);
		playerMovePanel.add(entryField);
		playerMovePanel.add(acceptMoveButton);
	}
	public void updateUsers() {
		for(int i = 0; i < wpc.playerList.size(); i++) {
			users.add(wpc.playerList.get(i).getName());
		}
		userList.removeAll();
		userList.setListData(users.toArray());
	}
	public void addWord(String word) {
		userEntries.add(word);
		entryList.removeAll();
		entryList.setListData(userEntries.toArray());
	}

	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			acceptMoveButton.doClick();
		}
	}
	public void keyTyped(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == acceptMoveButton) {
			currentMove = entryField.getText();
			wpc.newMove = true;
			wpc.currentMove = currentMove;
			entryField.setText("");
		}
	}
}
