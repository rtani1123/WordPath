import java.io.Serializable;


public class Player implements Serializable {
	String name;
	int points;
	public Player() {
		name = "";
		points = 0;
	}
	public Player(String name) {
		this.name = name;
		points = 0;
	}
	public void setPoints(int points) {
		this.points = points;
	}
	public int getPoints() {
		return points;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
}
