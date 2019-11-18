package examples.content.sfo;

public class Programmer extends Person {
	private static final long serialVersionUID = 1L;

	private int bugsPerHour;

	public int getBugsPerHour() {
		return bugsPerHour;
	}

	public void setBugsPerHour(int bugsPerHour) {
		this.bugsPerHour = bugsPerHour;
	}
}
