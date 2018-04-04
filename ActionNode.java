import java.util.ArrayList;

public class ActionNode {
	String name;
	ArrayList<StateNode> pre;
	ArrayList<StateNode> pos;	
	ActionNode(String name){
		this. name = name;
		pre = new ArrayList<StateNode>();
		pos = new ArrayList<StateNode>();
	}
}
