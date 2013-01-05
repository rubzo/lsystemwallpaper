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
