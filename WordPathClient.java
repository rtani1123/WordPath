import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class WordPathClient extends JFrame implements ActionListener {

	//Server Variables
	int portNumber = 34344;
	int clientNumber;
	Socket s;
	ObjectInputStream input = null;
	ObjectOutputStream output = null;
	HandleServerInput hsin;
	HandleServerOutput hsout;

	//GUI Variables
	CardLayout cl;
	GridBagLayout gbl;
	GridBagConstraints gbc;
	JPanel appPanel;
	JPanel nameEntryPanel;
	NamePanel namePanel;
	GamePanel gamePanel;
	JTextField nameField;
	JButton nameAcceptButton;
	Player player;
	JLabel nameStatusLabel;
	
	//Boolean control
	volatile boolean nameCheckFlag = false;
	volatile boolean nameFound = false;
	volatile String nameCheck = null;
	
	volatile boolean newMove = false;
	volatile String currentMove;
	
	public WordPathClient() {
		setTitle("Word Paths");
		cl = new CardLayout();
		appPanel = new JPanel();
		namePanel = new NamePanel(this);
		nameEntryPanel = new JPanel();
		gamePanel = new GamePanel(this);
		nameField = new JTextField(20);
		nameAcceptButton = new JButton("Accept");
		player = new Player();
		nameStatusLabel = new JLabel("");
		
		add(appPanel);
		appPanel.setLayout(cl);
		appPanel.add(namePanel, "Name Panel");
		appPanel.add(gamePanel, "Game Panel");
		cl.show(appPanel, "Identity Panel");

		attachServer();
		
		
		
		
	}
	public void setPlayerName(String name) {
		player.setName(name);
	}
	public void attachServer() {
		try {
			s = new Socket("localhost", portNumber);
		} catch (Exception e) {
			System.out.println("Client socket exception" + e.getMessage());
		}
		System.out.println("connection made client");
		try {
			// Create the 2 streams for talking to the server
			output = new ObjectOutputStream(s.getOutputStream());
			input = new ObjectInputStream(s.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		hsin = new HandleServerInput(s);
		hsout = new HandleServerOutput(s);
		new Thread(hsin).start();
		new Thread(hsout).start();
	}
	
	public void checkNames(String name) {
		nameCheckFlag = true;
		nameCheck = name;
	}
	
	public void addWord(String word) {
		gamePanel.addWord(word);
	}
	public void actionPerformed(ActionEvent ae) {
		
	}
	public static void main(String args[]) {
		WordPathClient client = new WordPathClient();
		client.setVisible(true);
		client.setSize(600, 600);
		client.setLocation(500, 500);
		client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	class HandleServerInput implements Runnable {
		Socket mySocket;
		public HandleServerInput(Socket s) {
			mySocket = s;
			System.out.println("Input Client Started");
		}
		public void run() {
			String message;
			while(true) {
				try {
					message = receiveString();
					System.out.println("message is: " + message);
					
					if(message.equals("name checked")) {
						nameFound = receiveBool();
						if(nameFound) {
							nameStatusLabel.setText("Bad Name");
						}
						else {
							cl.show(appPanel, "Game Panel");
						}
					}
					else if(message.equals("bad move")) {
						
					}
					else if(message.equals("good move")) {
						addWord(currentMove);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
		public String receiveString() {
			Object obj = null;
			try {
				while ((obj = input.readObject()) != null) {
					if (obj instanceof String) {
						return ((String) obj);
					}
				}
			} catch (Exception e) {
				System.out.println("Unable to retrieve string info");
				System.exit(0);
			}
			return "error";
		}
		public boolean receiveBool() {
			Object obj = null;
			try {
				while((obj = input.readObject()) != null) {
					if(obj instanceof Boolean) {
						return ((Boolean) obj);
					}
				}
			} catch (Exception e) {
				System.out.println("Unable to retrieve string info");
			}
			return false;
		}
		// RECEIVE INTEGER FROM SERVER
		public Integer receiveInt() {
			Object obj = null;
			try {
				while ((obj = input.readObject()) != null) {
					if (obj instanceof Integer) {
						return ((Integer) obj);
					}
				}
			} catch (Exception e) {
				System.out.println("Unable to retrieve int info");
			}
			return -999;
		}
	}
	
	class HandleServerOutput implements Runnable {
		Socket mySocket;
		public HandleServerOutput(Socket s) {
			mySocket = s;
			System.out.println("Output Client Started");
		}
		public void run() {
			while(true) {
				try {
					//System.out.println(nameCheckFlag);
					if(nameCheckFlag == true) {
						System.out.println("message sent server: check names");
						output.writeObject(new String("check names"));
						
						output.reset();
						output.writeObject(nameCheck);
						output.reset();
						nameCheckFlag = false;
					}
					else if(newMove) {
						output.writeObject(new String("new move"));
						output.reset();
						output.writeObject(currentMove);
						output.reset();
						newMove = false;
					}
				}
				catch(Exception ex) {
					System.out.println("error");
					ex.printStackTrace();
					System.exit(0);
				}
			}
		}
		
	}
}
