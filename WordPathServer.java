import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class WordPathServer extends JFrame implements ActionListener {

	static final int portNumber = 34344;
	ServerSocket ss = null;
	Socket s = null;
	ArrayList<HandleClientInput> hciList = new ArrayList<HandleClientInput>();
	ArrayList<HandleClientOutput> hcoList = new ArrayList<HandleClientOutput>();
	ArrayList<ObjectOutputStream> outputs = new ArrayList<ObjectOutputStream>();
	ArrayList<ObjectInputStream> inputs = new ArrayList<ObjectInputStream>();
	BufferedReader dictionaryRead;

	Random generator;
	String word1, word2;
	int wordIndex;
	ArrayList<String> wordList = new ArrayList<String>();
	ArrayList<Player> players = new ArrayList<Player>();
	ArrayList<ArrayList<String>> wordsPlayed = new ArrayList<ArrayList<String>>();  //each player has a list of words

	int clientNumber = 0;
	String nameToCheck;
	
	//boolean control
	volatile boolean nameFoundFlag = false;
	volatile boolean nameFoundInList = false;
	volatile boolean moveGoodSend = false;
	volatile boolean moveGood = false;
	volatile boolean wordsDecided = false;
	
	
	public WordPathServer() {
		players = new ArrayList<Player>();
		wordsPlayed = new ArrayList<ArrayList<String>>();
		readDictionary();
		
		// creating the server socket
		System.out.println("Server running - Ctrl-C to quit");
		try {
			ss = new ServerSocket(portNumber);
		} catch (Exception ex) {
			System.out.println("Port Number in Use");
			System.exit(0);
		}

		getClients();

	}
	public void readDictionary() {
		try {
			dictionaryRead = new BufferedReader(
				new FileReader(
				new File("fourLetterDictionary.txt")));
		}
		catch(Exception exc) {
			System.out.println("File non-existent");
		}
		
		String line;
		try {
			while((line = dictionaryRead.readLine()) != null) {
				wordList.add(line);
			}
		} catch(Exception exc) {
			System.out.println("Problem Reading File");
		}
		wordIndex = generator.nextInt() % wordList.size()-1;
		word1 = wordList.get(wordIndex);
		wordIndex = generator.nextInt() % wordList.size()-1;
		word2 = wordList.get(wordIndex);
		wordsDecided = true;
	}
	public boolean checkCharacterProximity(String a, String b) {
		System.out.println("checking");
		char[] aList = a.toCharArray();
		char[] bList = b.toCharArray();
		for(int i = 0; i < aList.length; i++) {
			System.out.println(aList[i]);
		}
		for(int i = 0; i < bList.length; i++) {
			System.out.println(bList[i]);
		}
		if(!(aList[0] == bList[0])) {
			if(!(aList[1] == bList[1]))
				return false;
			else if(!(aList[2] == bList[2]))
				return false;
			else if(!(aList[3] == bList[3]))
				return false;
			else return true;
		}
		else if(!(aList[1] == bList[1])) {
			if(!(aList[2] == bList[2]))
				return false;
			else if(!(aList[3] == bList[3]))
				return false;
			else return true;
		}
		else if(!(aList[2] == bList[2])) {
			if(!(aList[3] == bList[3]))
				return false;
			else return true;
		}
		else if(!(aList[3] == bList[3])) {
			return true;
		}
		else return false;
		
	}
	public void checkWord(String move, int clientNumber) {
		if(move.length() == 4)
		{
			for(int i = 0; i < wordList.size(); i++) {
				if(move.equals(wordList.get(i))) {
					if(!wordsPlayed.get(clientNumber).isEmpty()) {
						System.out.println("Not empty. checking");
						if(checkCharacterProximity(move, wordsPlayed.get(clientNumber).get(wordsPlayed.get(clientNumber).size()-1))) {
							wordsPlayed.get(clientNumber).add(move);
							moveGood = true;
							moveGoodSend = true;
							return;
						}						
					}
					else {
						System.out.println("current player list empty; adding");
						wordsPlayed.get(clientNumber).add(move);
						moveGood = true;
						moveGoodSend = true;
						return;
					}
				}
			}
		}
		moveGood = false;
		moveGoodSend = true;
	}
	public void getClients() {
		while (true) {
			try {
				s = ss.accept();
				wordsPlayed.add(new ArrayList<String>());
				try {
					outputs.add(new ObjectOutputStream(s.getOutputStream()));
					inputs.add(new ObjectInputStream(s.getInputStream()));
				} catch (Exception e) {
					System.out.println("Streams unable to connect to socket");
					System.exit(0);
				}

				hciList.add(new HandleClientInput(s, clientNumber));
				hcoList.add(new HandleClientOutput(s, clientNumber));
				
				new Thread(hciList.get(clientNumber)).start();
				new Thread(hcoList.get(clientNumber)).start();
				clientNumber++;
				
			} catch (Exception e) {
				System.out.println("got an exception" + e.getMessage());
				System.exit(0);
			}

			System.out.println("got a connection");
		}// end while
	}

	public void checkNames(String name) {
		for(int i = 0; i < players.size(); i++) {
			if(name == players.get(i).getName()) {
				nameFoundInList = true;
			}
		}
		if(!nameFoundInList) {
			players.add(new Player(name));
		}
		nameFoundFlag = true;
	}
	public void actionPerformed(ActionEvent e) {

	}

	public static void main(String[] args) {
		WordPathServer wps = new WordPathServer();
		wps.setVisible(true);
		wps.setSize(600, 600);
		wps.setLocation(500, 500);
		wps.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	class HandleClientInput implements Runnable {
		Socket mySocket;
		ObjectInputStream input;
		ObjectOutputStream output;
		int clientNumber;

		public HandleClientInput(Socket s, int clientNumber) {
			mySocket = s;
			this.clientNumber = clientNumber;
			input = inputs.get(clientNumber);
			output = outputs.get(clientNumber);
			System.out.println("Input Server Started");
		}

		public void run() {
			String message;
			while(true) {
				try {
					message = receiveString();
					System.out.println("Message from " + clientNumber + " is: " + message);
					if(message.equals("check names")) {
						nameToCheck = receiveString();
						checkNames(nameToCheck);
					}
					else if(message.equals("new move")) {
						checkWord(receiveString(), clientNumber);
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

	class HandleClientOutput implements Runnable {
		Socket mySocket;
		ObjectInputStream input;
		ObjectOutputStream output;
		int clientNumber;

		public HandleClientOutput(Socket s, int clientNumber) {
			mySocket = s;
			this.clientNumber = clientNumber;
			output = outputs.get(clientNumber);
			input = inputs.get(clientNumber);
			System.out.println("Output Server Started");
		}

		public void run() {
			while(true) {
				try {
					if(nameFoundFlag) {
						output.writeObject(new String("name checked"));
						output.reset();
						output.writeObject(nameFoundInList);
						output.reset();
						nameFoundFlag = false;
					}
					else if(moveGoodSend) {
						if(moveGood) {
							output.writeObject("good move");
						}
						else if(!moveGood) {
							output.writeObject("bad move");
						}
						output.reset();
						moveGoodSend = false;
					}
					else if(wordsDecided) {
						output.writeObject("initial words");
						output.reset();
						output.writeObject(word1);
						output.reset();
						output.writeObject(word2);
						output.reset();
						wordsDecided = false;
					}
				}
				catch(Exception ex) {
					System.out.println("Trouble sending message");
					ex.printStackTrace();
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

}
