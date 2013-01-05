package eu.whrl.lsystemwallpaper;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

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
		public Command[] baseCommand;
		public boolean alwaysBase;
	}
	
	/*
	 * Member variables.
	 */
	public Command[] commands;
	private Map<String,Function> functionStore;
	private float turnAngle;
	private String seed;
	
	private int initMaxTmpCommandsContSize = 64;
	private Command[] tmpCommands;
	private int tmpCommandsContSize;
	
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
		
		tmpCommands = new Command[initMaxTmpCommandsContSize];
		tmpCommandsContSize = 0;
		
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
		func.baseCommand = new Command[1];
		func.baseCommand[0] = new Move(Integer.parseInt(components[2]));
		func.alwaysBase = false;
		if (func.body.length == 0) {
			func.alwaysBase = true;
		}
		
		functionStore.put(func.name, func);
	}
	
	
	public void produceCommands(int iterations) {
		Command[] seedCommands = readCommands(seed);
		for (int i = 0; i < seedCommands.length; i++) {
			tmpCommands[i] = seedCommands[i];
		}
		tmpCommandsContSize = seedCommands.length;
		expand(iterations);
		saveTemporaryCommands();
	}
	
	private Command[] fetchFromFunctionStore(String name, int n) {
		Function function = functionStore.get(name);
		
		if (n == 0 || function.alwaysBase) {
			return function.baseCommand;
		}
		
		return function.body;
	}
	
	private void expand(int n) {
		int i = 0;
		
		while (i < tmpCommandsContSize) {
			
			Command command = tmpCommands[i];
			
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
		if ((tmpCommandsContSize + newCommands.length - 1) > tmpCommands.length) {
			Log.d(LSystemDrawingService.TAG, "Expanding temporary command container... " + tmpCommands.length*2);
			Command[] largerTmpCommands = new Command[tmpCommands.length*2];
			for (int i = 0; i < tmpCommandsContSize; i++) {
				largerTmpCommands[i] = tmpCommands[i];
			}
			tmpCommands = largerTmpCommands;
		}
		
		// Shunt everything after the expansion forward
		for (int i = tmpCommandsContSize-1; i > insertionPoint; i--) {
			tmpCommands[i + newCommands.length - 1] = tmpCommands[i];
		}
				
		// Expand
		for (int i = 0; i < newCommands.length; i++) {
			tmpCommands[insertionPoint + i] = newCommands[i];
		}
		
		tmpCommandsContSize += (newCommands.length - 1);
	}
	
	private void saveTemporaryCommands() {
		commands = new Command[tmpCommandsContSize];
		for (int i = 0; i < tmpCommandsContSize; i++) {
			commands[i] = tmpCommands[i];
		}
	}

	public void printCommands() {
		for (Command c : commands) {
			c.print();
		}
	}
}
