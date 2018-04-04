import java.util.ArrayList;

public class StateNode {
	String name;
	ArrayList<ActionNode> pre;
	ArrayList<ActionNode> pos;	
	
	StateNode(String name){
		this.name = name;
		pre = new ArrayList<ActionNode>();
		pos = new ArrayList<ActionNode>();
	}
}
