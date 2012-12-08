import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class WordPathServer extends JFrame {

	static final int portNumber = 34344;
	int timerCounter = 0;
	ServerSocket ss = null;
	Socket s = null;
	ArrayList<HandleClientInput> hciList = new ArrayList<HandleClientInput>();
	ArrayList<HandleClientOutput> hcoList = new ArrayList<HandleClientOutput>();
	ArrayList<ObjectOutputStream> outputs = new ArrayList<ObjectOutputStream>();
	ArrayList<ObjectInputStream> inputs = new ArrayList<ObjectInputStream>();
	BufferedReader dictionaryRead;

	volatile Random generator;
	volatile String word1, word2;
	volatile int wordIndex = -9;
	volatile ArrayList<String> wordList = new ArrayList<String>();
	volatile ArrayList<Player> players = new ArrayList<Player>();
	volatile ArrayList<ArrayList<String>> wordsPlayed = new ArrayList<ArrayList<String>>();  //each player has a list of words

	volatile int clientCount = 0;
	volatile String nameToCheck;
	
	//boolean control
	volatile boolean nameFoundFlag = false;
	volatile boolean nameFoundInList = false;
	volatile boolean moveGoodSend = false;
	volatile boolean moveGood = false;
	volatile boolean wordsDecided = false;
	volatile boolean newPlayerAdded = false;
	volatile boolean startCountDown = false;
	volatile boolean sendNextNumber = false;
	volatile boolean countDownStarted = false;
	volatile boolean gameOngoing = false;
	
	volatile int countDown = 10;
	ArrayList<Boolean> clientReady = new ArrayList<Boolean>();
	
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
		RunCountDownCheck rcdc = new RunCountDownCheck();
		new Thread(rcdc).start();
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
		generator = new Random();
		wordIndex = generator.nextInt() % wordList.size()-1;
		if(wordIndex < 0) wordIndex = wordIndex*-1;
		word1 = wordList.get(wordIndex);
		wordIndex = generator.nextInt() % wordList.size()-1;
		if(wordIndex < 0) wordIndex = wordIndex*-1;
		word2 = wordList.get(wordIndex);
		wordsDecided = true;
	}
	public boolean checkCharacterProximity(String a, String b) {
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
	public void resetRound() {
		for(int i = 0; i < players.size(); i++) {
			wordsPlayed.get(i).clear();
		}
	}
	public void checkWord(String move, int clientCount) {
		if(move.length() == 4)
		{
			for(int i = 0; i < wordList.size(); i++) {
				if(move.equals(wordList.get(i))) {
					if(!wordsPlayed.get(clientCount).isEmpty()) {
						System.out.println("Not empty. checking");
						if(checkCharacterProximity(move, wordsPlayed.get(clientCount).get(wordsPlayed.get(clientCount).size()-1))) {
							wordsPlayed.get(clientCount).add(move);
							moveGood = true;
							moveGoodSend = true;
							return;
						}						
					}
					else {
						System.out.println("current player list empty; adding");
						wordsPlayed.get(clientCount).add(move);
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
				clientReady.add(false);
				try {
					outputs.add(new ObjectOutputStream(s.getOutputStream()));
					inputs.add(new ObjectInputStream(s.getInputStream()));
				} catch (Exception e) {
					System.out.println("Streams unable to connect to socket");
					System.exit(0);
				}

				hciList.add(new HandleClientInput(s, clientCount));
				hcoList.add(new HandleClientOutput(s, clientCount));
				
				new Thread(hciList.get(clientCount)).start();
				new Thread(hcoList.get(clientCount)).start();
				clientCount++;
				
			} catch (Exception e) {
				System.out.println("got an exception" + e.getMessage());
				System.exit(0);
			}

			System.out.println("got a connection");
		}// end while
	}
	public void checkReady() {
		boolean allClientsReady = true;
		
		for(int i = 0; i < clientCount; i++) {
			//System.out.println("" + i + clientReady.get(i));
			if(!clientReady.get(i))
				allClientsReady = false;
		}
		
		
		if(allClientsReady == true && clientCount > 0 && !countDownStarted && !gameOngoing) {
			System.out.println("start count down");
			startCountDown = true;
			countDownStarted = true;
		}
	}
	public void checkNames(String name) {
		for(int i = 0; i < players.size(); i++) {
			if(name == players.get(i).getName()) {
				nameFoundInList = true;
			}
		}
		if(!nameFoundInList) {
			players.add(new Player(name));
			newPlayerAdded = true;
			nameFoundInList = false;
		}
		nameFoundFlag = true;
	}
	

	public static void main(String[] args) {
		WordPathServer wps = new WordPathServer();
		wps.setVisible(true);
		wps.setSize(600, 600);
		wps.setLocation(500, 500);
		wps.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	class RunCountDownCheck implements Runnable {
		public void run() {
			while(true) {
				try {
					checkReady();
					if(countDownStarted) {
						sendNextNumber = true;

					}
				} catch(Exception exc) {
					exc.printStackTrace();
				}
				try {
					Thread.sleep(1000);
				} catch(Exception exc) {
					System.out.println("timer error");
				}
			}
		}
	}
	class HandleClientInput implements Runnable {
		Socket mySocket;
		int clientNumber;

		public HandleClientInput(Socket s, int clientNumber) {
			mySocket = s;
			this.clientNumber = clientNumber;
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
						System.out.println("Name sent from " + clientNumber + ": " + nameToCheck);
						checkNames(nameToCheck);
					}
					else if(message.equals("new move")) {
						checkWord(receiveString(), clientNumber);
					}
					else if(message.equals("get initial names")) {
						outputs.get(clientNumber).writeObject("sending names");
						outputs.get(clientNumber).writeObject(players);
					}
					else if(message.equals("ready")) {
						clientReady.set(clientNumber, true);
					}
					else if(message.equals("game started")) {
						countDownStarted = false;
						sendNextNumber = false;
						gameOngoing = true;
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
				while ((obj = inputs.get(clientNumber).readObject()) != null) {
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
		public Integer receiveInt(int index) {
			Object obj = null;
			try {
				while ((obj = inputs.get(index).readObject()) != null) {
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
		int clientNumber;

		public HandleClientOutput(Socket s, int clientNumber) {
			mySocket = s;
			this.clientNumber = clientNumber;

			System.out.println("Output Server Started");
		}

		public void run() {
			while(true) {
				try {
					if(nameFoundFlag) {
						System.out.println("name checked");
						outputs.get(clientNumber).writeObject(new String("name checked"));
						outputs.get(clientNumber).reset();
						outputs.get(clientNumber).writeObject(nameFoundInList);
						outputs.get(clientNumber).reset();
						nameFoundFlag = false;
					}
					else if(moveGoodSend) {
						if(moveGood) {
							System.out.println("good word");
							outputs.get(clientNumber).writeObject("good move");
						}
						else if(!moveGood) {
							System.out.println("bad word");
							outputs.get(clientNumber).writeObject("bad move");
						}
						outputs.get(clientNumber).reset();
						moveGoodSend = false;
					}
					else if(wordsDecided) {
						System.out.println("CC " + clientCount);
						for(int i = 0; i < clientCount; i++) {
							outputs.get(i).writeObject("initial words");
							outputs.get(i).reset();
							outputs.get(i).writeObject(word1);
							outputs.get(i).reset();
							outputs.get(i).writeObject(word2);
							outputs.get(i).reset();
						}
						wordsDecided = false;
					}
					else if(newPlayerAdded) {
						for(int i = 0; i < clientCount; i++) {
							outputs.get(i).writeObject(new String("new player"));
							outputs.get(i).reset();
							outputs.get(i).writeObject(players);
							outputs.get(i).reset();
						}
						newPlayerAdded = false;
					}
					else if(startCountDown) {
						for(int i = 0; i < clientCount; i++) {
							outputs.get(i).writeObject("count");
							outputs.get(i).reset();
							outputs.get(i).writeObject(countDown);
						}
						startCountDown = false;
					}
					else if(sendNextNumber) {
						for(int i = 0; i < clientCount; i++) {
							countDown--;
							outputs.get(i).writeObject("next number");
							outputs.get(i).reset();
							outputs.get(i).writeObject(countDown);
							outputs.get(i).reset();
						}
						sendNextNumber = false;
					}
				}
				catch(Exception ex) {
					System.out.println("Trouble sending message");
					ex.printStackTrace();
					System.exit(0);
				}
			}
		}
		public String receiveString(int index) {
			Object obj = null;
			try {
				while ((obj = inputs.get(index).readObject()) != null) {
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
		public Integer receiveInt(int index) {
			Object obj = null;
			try {
				while ((obj = inputs.get(index).readObject()) != null) {
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
