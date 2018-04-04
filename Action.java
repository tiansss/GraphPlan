import java.util.ArrayList;

public class Action {
	String name;
	String[] preCons;
	String[] effects;
	
	Action(String name, String[] pre, String[] eff){
		this.name =  name;
		this.preCons = pre;
		this.effects = eff;
	}
}
