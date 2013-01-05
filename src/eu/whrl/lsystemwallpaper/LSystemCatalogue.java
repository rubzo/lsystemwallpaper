package eu.whrl.lsystemwallpaper;

import java.util.HashMap;
import java.util.Map;

public class LSystemCatalogue {
	private static Map<String, LSystemDescription> catalogue = new HashMap<String, LSystemDescription>();
	
	static {
		LSystemDescription lsDesc = new LSystemDescription();
		lsDesc.name = "hilbert";
		lsDesc.functions = new String[3];
		lsDesc.functions[0] = "f::20";
		lsDesc.functions[1] = "l:+rf-lfl-fr+:0";
		lsDesc.functions[2] = "r:-lf+rfr+fl-:0";
		lsDesc.startState = "l";
		lsDesc.iterations = 5;
		lsDesc.turnAngle = 90.0f;
		add(lsDesc);
	}
	
	static {
		LSystemDescription lsDesc = new LSystemDescription();
		lsDesc.name = "tree";
		lsDesc.functions = new String[2];
		lsDesc.functions[0] = "f:g[-f][+f][gf]:10";
		lsDesc.functions[1] = "g:gg:10";
		lsDesc.startState = "f";
		lsDesc.iterations = 5;
		lsDesc.turnAngle = 45.0f;
		add(lsDesc);
	}
	
	static {
		LSystemDescription lsDesc = new LSystemDescription();
		lsDesc.name = "kochcurve";
		lsDesc.functions = new String[1];
		lsDesc.functions[0] = "f:f+f-f-f+f:10";
		lsDesc.startState = "f";
		lsDesc.iterations = 3;
		lsDesc.turnAngle = 90.0f;
		add(lsDesc);
	}
	
	static {
		LSystemDescription lsDesc = new LSystemDescription();
		lsDesc.name = "sierpinski";
		lsDesc.functions = new String[2];
		lsDesc.functions[0] = "a:b-a-b:10";
		lsDesc.functions[1] = "b:a+b+a:10";
		lsDesc.startState = "a";
		lsDesc.iterations = 5;
		lsDesc.turnAngle = 60.0f;
		add(lsDesc);
	}
	
	static {
		LSystemDescription lsDesc = new LSystemDescription();
		lsDesc.name = "dragoncurve";
		lsDesc.functions = new String[3];
		lsDesc.functions[0] = "x:x+yf:0";
		lsDesc.functions[1] = "y:fx-y:0";
		lsDesc.functions[2] = "f::10";
		lsDesc.startState = "fx";
		lsDesc.iterations = 10;
		lsDesc.turnAngle = 90.0f;
		add(lsDesc);
	}
	
	static {
		LSystemDescription lsDesc = new LSystemDescription();
		lsDesc.name = "fractalplant";
		lsDesc.functions = new String[2];
		lsDesc.functions[0] = "f:ff:10";
		lsDesc.functions[1] = "g:f-[[g]+g]+f[+fg]-g:10";
		lsDesc.startState = "g";
		lsDesc.iterations = 4;
		lsDesc.turnAngle = 22.5f;
		add(lsDesc);
	}
	
	static {
		LSystemDescription lsDesc = new LSystemDescription();
		lsDesc.name = "levyccurve";
		lsDesc.functions = new String[1];
		lsDesc.functions[0] = "f:+f--f+:10";
		lsDesc.startState = "f";
		lsDesc.iterations = 8;
		lsDesc.turnAngle = 45.0f;
		add(lsDesc);
	}
	
	static {
		LSystemDescription lsDesc = new LSystemDescription();
		lsDesc.name = "peanogosper";
		lsDesc.functions = new String[2];
		lsDesc.functions[0] = "f:f+g++g-f--ff-g+:10";
		lsDesc.functions[1] = "g:-f+gg++g+f--f-g:10";
		lsDesc.startState = "f";
		lsDesc.iterations = 4;
		lsDesc.turnAngle = 60.0f;
		add(lsDesc);
	}
	
	static {
		LSystemDescription lsDesc = new LSystemDescription();
		lsDesc.name = "cross";
		lsDesc.functions = new String[1];
		lsDesc.functions[0] = "f:f-f+f+ff-f-f+f:10";
		lsDesc.startState = "f-f-f-f-";
		lsDesc.iterations = 2;
		lsDesc.turnAngle = 90.0f;
		add(lsDesc);
	}
	
	static {
		LSystemDescription lsDesc = new LSystemDescription();
		lsDesc.name = "32segment";
		lsDesc.functions = new String[1];
		lsDesc.functions[0] = "F:-F+F-F-F+F+FF-F+F+FF+F-F-FF+FF-FF+F+F-FF-F-F+FF-F-F+F+F-F+:10";
		lsDesc.startState = "F+F+F+F";
		lsDesc.iterations = 1;
		lsDesc.turnAngle = 90.0f;
		add(lsDesc);
	}
	
	static {
		LSystemDescription lsDesc = new LSystemDescription();
		lsDesc.name = "snowflake";
		lsDesc.functions = new String[1];
		lsDesc.functions[0] = "f:f+f--f+f:10";
		lsDesc.startState = "f--f--f--";
		lsDesc.iterations = 4;
		lsDesc.turnAngle = 60.0f;
		add(lsDesc);
	}
	
	// TODO make this throw an exception
	public static LSystemDescription get(String name) {
		if (catalogue.containsKey(name)) {
			return catalogue.get(name);
		}
		return null;
	}
	
	public static boolean add(LSystemDescription lsDesc) {
		if (verify(lsDesc)) {
			catalogue.put(lsDesc.name, lsDesc);
			return true;
		}
		return false;
	}
	
	private static boolean verify(LSystemDescription lsDesc) {
		// TODO
		return true;
	}
}
