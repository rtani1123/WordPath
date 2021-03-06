import java.io.*;
import java.net.*;
import java.util.ArrayList;
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
	ArrayList<Player> playerList;
	JLabel nameStatusLabel;
	
	//Boolean control
	volatile boolean nameCheckFlag = false;
	volatile boolean nameFound = false;
	volatile boolean sendReady = false;
	volatile String nameCheck = null;
	volatile boolean gameStartedSend = false;
	volatile boolean clientConnected = true;
	volatile boolean playerWon = false;
	
	volatile boolean newMove = false;
	volatile String currentMove;
	volatile String winnerName;
	volatile int numberMoves = 0;
	
	public WordPathClient() {
		setTitle("Word Paths");
		playerList = new ArrayList<Player>();
		cl = new CardLayout();
		appPanel = new JPanel();
		namePanel = new NamePanel(this);
		nameEntryPanel = new JPanel();
		gamePanel = new GamePanel(this);
		nameField = new JTextField(20);
		nameAcceptButton = new JButton("Accept");
		player = new Player();
		
		add(appPanel);
		appPanel.setLayout(cl);
		appPanel.add(namePanel, "Name Panel");
		appPanel.add(gamePanel, "Game Panel");
		cl.show(appPanel, "Name Panel");
		
		
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
		clientConnected = true;
	}
	
	public void checkNames(String name) {
		attachServer();
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
		client.setSize(450, 600);
		client.setLocation(500, 200);
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
					System.out.println("Message from Server is: " + message);
					
					if(message.equals("name checked")) {
						nameFound = receiveBool();
						if(nameFound) {
							namePanel.statusLabel.setText("Name already taken");
						}
						else {
							playerList.add(new Player(nameCheck));
							namePanel.statusLabel.setText("");
							output.writeObject(new String("get initial names"));
							output.reset();
							cl.show(appPanel, "Game Panel");
						}
					}
					else if(message.equals("client number")) {
						clientNumber = receiveInt();
					}
					else if(message.equals("bad move")) {
						
					}
					else if(message.equals("good move")) {
						addWord(currentMove);
					}
					else if(message.equals("initial words")) {
						gamePanel.word1 = receiveString();
						System.out.println("received " + gamePanel.word1);
						gamePanel.word2 = receiveString();
						System.out.println("received " + gamePanel.word2);
					}
					else if(message.equals("sending names")) {
						Object obj = null;
						try {
							while ((obj = input.readObject()) != null) {
								if (obj instanceof ArrayList) {
									playerList = ((ArrayList) obj);
									System.out.println("got updated players");
									break;
								}
							}
						} catch(Exception exc) {
							System.out.println("Couldn't read names");
						}
						gamePanel.updateUsers();
					}
					else if(message.equals("new player")) {
						Object obj = null;
						try {
							while ((obj = input.readObject()) != null) {
								if (obj instanceof ArrayList) {
									playerList = ((ArrayList) obj);
									System.out.println("got updated players");
									break;
								}
							}
						} catch(Exception exc) {
							System.out.println("Couldn't read names");
						}
						System.out.println("got new player");
						gamePanel.updateUsers();
					}
					else if(message.equals("count")) {
						gamePanel.displayTimer(gamePanel.timer--);
					}
					else if(message.equals("next number")) {
						gamePanel.setTimer(gamePanel.timer--);
						gamePanel.checkTime();
					}
					else if(message.equals("game won")) {
						winnerName = receiveString();
						numberMoves = receiveInt();
						gamePanel.declareWinner(winnerName, numberMoves);
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
					if(clientConnected) {
						output.writeObject(new String("request initial words"));
						output.reset();
						clientConnected = false;
					}
					else if(nameCheckFlag == true) {
						System.out.println("message sent server: check names");
						output.writeObject(new String("check names " + clientNumber));
						
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
					else if(sendReady) {
						output.writeObject("ready");
						output.reset();
						sendReady = false;
					}
					else if(gameStartedSend) {
						output.writeObject("game started");
						output.reset();
						gameStartedSend = false;
					}
					else if(playerWon) {
						output.writeObject("player won " + clientNumber);
						output.reset();
						output.writeObject(gamePanel.userEntries.size());
						output.reset();
						playerWon = false;
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
