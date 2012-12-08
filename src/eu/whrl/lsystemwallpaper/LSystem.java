package eu.whrl.lsystemwallpaper;

public class LSystem {
	abstract class Command {
		public abstract void print();	
	}
	class Move extends Command {
		public float dist;
		public Move(float d) { dist = d; }
		public void print() {
			System.out.println("Move " + dist);
		}
	}
	class Turn extends Command {
		public float angle;
		public Turn(float a) { angle = a; }
		public void print() {
			System.out.println("Turn " + angle);
		}
	}
	class Expand extends Command {
		public String name;
		public Expand(String n) { name = n; }
		public void print() {
			System.out.println("Expand " + name);
		}
	}
	
	public Command[] commands;
	
	public LSystem(int n) {
		if (n <= 0) {
			n = 1;
		}
		produceCommands(n);
	}
	
	public void produceCommands(int n) {
		commands = new Command[1];
		commands[0] = new Expand("l");
		expand(n);
	}
	
	private void expand(int n) {
		int i = 0;
		
		while (i < commands.length) {
			
			Command c = commands[i];
			
			if (c instanceof Expand) {
				Command[] newCommands = null;
				if (((Expand) c).name.equals("l")) {
					newCommands = l(n);
				} else if (((Expand) c).name.equals("r")) {
					newCommands = r(n);
				} else if (((Expand) c).name.equals("f")) {
					newCommands = f(n);
				}

				insertNewCommands(i, newCommands);
				
				i += newCommands.length - 1;
			}
			
			i++;
		}
		
		if (n > 0) {
			expand(n-1);
		}
	}
	
	private Command[] l(int n) {
		if (n == 0) {
			Command[] newCommands = new Command[1];
			newCommands[0] = new Move(0.0f);
			return newCommands;
		}
		
		Command[] newCommands = new Command[11];
		newCommands[0] = new Turn(90.0f);
		newCommands[1] = new Expand("r");
		newCommands[2] = new Expand("f");
		newCommands[3] = new Turn(-90.0f);
		newCommands[4] = new Expand("l");
		newCommands[5] = new Expand("f");
		newCommands[6] = new Expand("l");
		newCommands[7] = new Turn(-90.0f);
		newCommands[8] = new Expand("f");
		newCommands[9] = new Expand("r");
		newCommands[10] = new Turn(90.0f);

		return newCommands;
	}
	
	private Command[] r(int n) {
		if (n == 0) {
			Command[] newCommands = new Command[1];
			newCommands[0] = new Move(0.0f);
			return newCommands;
		}
		
		Command[] newCommands = new Command[11];
		newCommands[0] = new Turn(-90.0f);
		newCommands[1] = new Expand("l");
		newCommands[2] = new Expand("f");
		newCommands[3] = new Turn(90.0f);
		newCommands[4] = new Expand("r");
		newCommands[5] = new Expand("f");
		newCommands[6] = new Expand("r");
		newCommands[7] = new Turn(90.0f);
		newCommands[8] = new Expand("f");
		newCommands[9] = new Expand("l");
		newCommands[10] = new Turn(-90.0f);

		return newCommands;
	}
	
	private Command[] f(int n) {
		Command[] newCommands = new Command[1];
		newCommands[0] = new Move(20.0f);
		return newCommands;
	}
	
	private void insertNewCommands(int insertionPoint, Command[] newCommands) {
		Command[] fullCommands = new Command[commands.length + newCommands.length - 1];
		
		// Copy before
		for (int i = 0; i < insertionPoint; i++) {
			fullCommands[i] = commands[i];
		}
		
		// Expand
		for (int i = 0; i < newCommands.length; i++) {
			fullCommands[insertionPoint + i] = newCommands[i];
		}
		
		// Copy after
		for (int i = insertionPoint+1; i < commands.length; i++) {
			fullCommands[i + newCommands.length - 1] = commands[i];
		}
		
		commands = fullCommands;
	}

	public void printCommands() {
		for (Command c : commands) {
			c.print();
		}
	}
}
