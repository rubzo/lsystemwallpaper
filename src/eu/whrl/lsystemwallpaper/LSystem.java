package eu.whrl.lsystemwallpaper;

import java.util.HashMap;
import java.util.Map;

public class LSystem {
	
	/*
	 * The commands.
	 */
	abstract class Command {
		public abstract String print();	
	}
	class Move extends Command {
		public float dist;
		public Move(float d) { dist = d; }
		public String print() {
			return "Move " + dist;
		}
	}
	class Turn extends Command {
		public float angle;
		public Turn(float a) { angle = a; }
		public String print() {
			return "Turn " + angle;
		}
	}
	class Expand extends Command {
		public String name;
		public Expand(String n) { name = n; }
		public String print() {
			return "Expand " + name;
		}
	}
	class BranchStart extends Command {
		public String print() {
			return "Branch ( ";
		}
	}
	class BranchEnd extends Command {
		public String print() {
			return ")";
		}
	}
	
	class Function {
		public String name;
		public Command[] body;
		public float baseMovement;
		public boolean alwaysBase;
	}
	
	/*
	 * Member variables.
	 */
	public Command[] commands;
	private Map<String,Function> functionStore;
	private float turnAngle;
	private String seed;
	
	/*
	 * Constructor.
	 */
	public LSystem(int iterations, float angle, String seed, String[] functions) {
		
		if (iterations <= 0) {
			iterations = 1;
		}
		
		functionStore = new HashMap<String,Function>();
		
		turnAngle = angle;
		this.seed = seed;
		
		for (String function : functions) {
			readFunction(function);
		}
		
		produceCommands(iterations);
	}
	
	/*
	 * Methods.
	 */
	private Command[] readCommands(String commandString) {
		Command[] body = new Command[commandString.length()];
		int i = 0;
		for (char c : commandString.toCharArray()) {
			switch (c) {
			case '+':
				body[i] = new Turn(turnAngle);
				break;
			case '-':
				body[i] = new Turn(-turnAngle);
				break;
			case '[':
				body[i] = new BranchStart();
				break;
			case ']':
				body[i] = new BranchEnd();
				break;
			default:
				body[i] = new Expand("" + c);
				break;
			}
			i++;
		}
		return body;
	}
	
	private void readFunction(String function) {
		
		String[] components = function.split(":");
		
		Function func = new Function();
		
		func.name = components[0];
		func.body = readCommands(components[1]);
		func.baseMovement = Integer.parseInt(components[2]);
		func.alwaysBase = false;
		if (func.body.length == 0) {
			func.alwaysBase = true;
		}
		
		functionStore.put(func.name, func);
	}
	
	
	public void produceCommands(int iterations) {
		commands = readCommands(seed); 
		expand(iterations);
	}
	
	private Command[] fetchFromFunctionStore(String name, int n) {
		Function function = functionStore.get(name);
		
		if (n == 0 || function.alwaysBase) {
			Command[] newCommands = new Command[1];
			newCommands[0] = new Move(function.baseMovement);
			return newCommands;
		}
		
		return function.body;
	}
	
	private void expand(int n) {
		int i = 0;
		
		while (i < commands.length) {
			
			Command command = commands[i];
			
			if (command instanceof Expand) {
				Command[] newCommands = fetchFromFunctionStore(((Expand)command).name, n);
				insertNewCommands(i, newCommands);
				i += newCommands.length - 1;
			}
			
			i++;
		}
		
		if (n > 0) {
			expand(n-1);
		}
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
