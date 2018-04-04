import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Graphplan {
	static HashSet<String> init = new HashSet<>();
	static HashSet<String> goal = new HashSet<>();
	static HashSet<Action> actions = new HashSet<>();
	static boolean found = false;
	
	static ArrayList<HashMap<String, ActionNode>> actionNodes = new ArrayList<HashMap<String, ActionNode>>();
	static ArrayList<HashMap<String, StateNode>> stateNodes = new ArrayList<HashMap<String, StateNode>>();
	static ArrayList<HashSet<String>> stateMutex = new ArrayList<HashSet<String>>();
	static ArrayList<HashSet<String>> actionMutex = new ArrayList<HashSet<String>>();
	
	static ArrayList<HashSet<String>> answerActions = new ArrayList<HashSet<String>>();
	
	private static void readFile() {
		String line = "";
		try {
			FileReader fr = new FileReader("pddl.txt");
			BufferedReader br = new BufferedReader(fr);
			String name = "";
			String[] preString = null;
			String[] postString = null;
			while((line = br.readLine()) != null) {
				if (line.charAt(0) == 'I') {
					String[] initString = (line.substring(5, line.length()-2)).split(",");
					for (String initS: initString) init.add(initS);
				}
				if (line.charAt(0) == 'G') {
					String[] goalString = (line.substring(5, line.length()-2)).split(",");
					for (String goalS: goalString) goal.add(goalS);
				}
				if (line.charAt(0) == 'a') {
					if (!name.equals("")) actions.add(new Action(name, preString, postString));
					name = line.substring(7, line.length()-1);
				}
				if (line.charAt(0) == 'P') {
					preString = (line.substring(8, line.length())).split(",");
				}
				if (line.charAt(0) == 'E') {
					postString = (line.substring(7, line.length()-1)).split(",");
				}
            }
			if (!name.equals("")) actions.add(new Action(name, preString, postString));
			br.close();
		}
		catch(FileNotFoundException ex) {
			ex.printStackTrace();
        }
        catch(IOException ex) {
        		ex.printStackTrace();
        }
	}

	public static void printMutex(HashSet<String> mutex, String name) {
		System.out.print(name+":");
		for (String m: mutex) {
			System.out.print("("+m+")  ");
		}
		System.out.println();
	}
	
	public static void printAction(HashMap<String, ActionNode> actions) {
		System.out.println("Actions: ");
		for (String action: actions.keySet())
			System.out.print(action+",");
	}
	
	public static void printState(HashMap<String, StateNode> states) {
		System.out.println("Next States: ");
		for (String state: states.keySet())
			System.out.print(state+",");	
	}
	
	public static void printAction(ArrayList<HashSet<String>> actions) {
		for (int i = 0; i < actions.size(); i++) {
			System.out.print("action");
			System.out.print(i);
			System.out.print(": ");
			
			for (String ac: actions.get(i)) {
				System.out.print(ac+", ");
			}
			System.out.println();
		}
		
	}
	public static void extract(int currentLevel, HashSet<String> currentGoal, HashSet<String> nextGoal) {
		if (currentLevel == 0 && !found) {
			printAction(answerActions);
			found = true;
		}
		else {
			if (currentGoal.size() == 0) {
				extract(currentLevel - 1, nextGoal, new HashSet<String>());
			}
			else {
				for (StateNode state: stateNodes.get(currentLevel).values()) {
					if (currentGoal.contains(state.name)) {
						for (ActionNode preAct: state.pre) {
							//if no contex between preAct and answerActions.get(currentLevel - 1)
							boolean mutex = false;
							for (String act: answerActions.get(currentLevel - 1)) {
								if (actionMutex.get(currentLevel - 1).contains(act+preAct.name)) mutex = true;
							}
							if (!mutex) {
								currentGoal.remove(state.name);
								for (StateNode preState: preAct.pre) {
									nextGoal.add(preState.name);
								}
								answerActions.get(currentLevel - 1).add(preAct.name);
								extract(currentLevel, currentGoal, nextGoal);
								answerActions.get(currentLevel - 1).remove(preAct.name);
								currentGoal.add(state.name);
								for (StateNode preState: preAct.pre) {
									nextGoal.remove(preState.name);
								}
							}
						}
					}
				}
			}
		}		
	}
	public static void main(String[] args) {
		
		//read data from file, initialize the init set
		readFile();
		for (String s: goal) {
			if (s.charAt(0) == '~') {
				if (!init.contains(s.substring(1))){
					init.add(s.substring(1));
				}
			}
			else {
				if (!init.contains("~"+s)) {
					init.add("~"+s);
				}
				
			}
		}
		
	
		//initialize the process of expanding

		int level = 0;
		HashMap<String, StateNode> initStateNodes = new HashMap<String, StateNode>();
		for (String initState: init) {
			initStateNodes.put(initState, new StateNode(initState));
		}
		stateNodes.add(initStateNodes);
		stateMutex.add(new HashSet<String>());
		boolean solution = false;
		boolean levelOff = false;
		boolean level1 = false;
		
		//expand from stateNodes.get(level)
		while (!solution && !levelOff) {
			System.out.println();
			System.out.print("level = ");
			System.out.println(level);
			actionNodes.add(new HashMap<String, ActionNode>());//new action layer
			stateNodes.add(new HashMap<String, StateNode>());//new state layer
			
			//1. looking for proper actions and make connections
			for (Action act: actions) {
				boolean actOK = true;
				for (int i = 0; i < act.preCons.length && actOK; i++) {
					if (stateNodes.get(level).containsKey(act.preCons[i])) {//previous states contain this preCon
						//judge whether mutux with previous preCons?
						if (level != 0) {
							for (int j = 0; j < i && actOK; j++) {
								if (stateMutex.get(level).contains(act.preCons[i] + act.preCons[j])
									|| stateMutex.get(level).contains(act.preCons[j] + act.preCons[i]))
									actOK = false;
							}
						}
					}
					else actOK = false;			
				}
				
				if (actOK) { //make connections
					ActionNode actNode = new ActionNode(act.name); //new the action node
					actionNodes.get(level).put(act.name, actNode); //add the string and value into the level hashmap
					stateNodes.add(new HashMap<>());//new the empty level+1 states
					
					//connections between preState and act
					for (String preName: act.preCons) {
						StateNode preState = stateNodes.get(level).get(preName);
						preState.pos.add(actNode);
						actNode.pre.add(preState);
					}
					
					//connections between act and postState
					for (String posName: act.effects) {
						StateNode posState;
						if (!stateNodes.get(level+1).containsKey(posName)) {
							posState = new StateNode(posName);
							stateNodes.get(level+1).put(posName, posState);
						}
						else posState = stateNodes.get(level+1).get(posName);
						
						actNode.pos.add(posState);
						posState.pre.add(actNode);
					}
				}
			}
			
			//2. persistent expanding and make connections
			for (String persistentState: stateNodes.get(level).keySet()) {
				//get the post state node
				StateNode posState;
				if (!stateNodes.get(level+1).containsKey(persistentState)) {
					posState = new StateNode(persistentState);
				}
				else posState = stateNodes.get(level+1).get(persistentState);
				
				//new the persistent action node
				ActionNode actPersistentNode = new ActionNode("p("+persistentState+")");
				//add connections
				stateNodes.get(level).get(persistentState).pos.add(actPersistentNode);
				actPersistentNode.pre.add(stateNodes.get(level).get(persistentState));
				actPersistentNode.pos.add(posState);
				posState.pre.add(actPersistentNode);		
				
				stateNodes.get(level+1).put(posState.name, posState);
				actionNodes.get(level).put("p("+persistentState+")", actPersistentNode);
			}
			
			printAction(actionNodes.get(level));
			System.out.println();
			printState(stateNodes.get(level+1));
			System.out.println();
			
			
			//3. add action mutex
			actionMutex.add(new HashSet<String>());
			HashSet<String> cnMutex = new HashSet<>();
			HashSet<String> ieMutex = new HashSet<>();
			HashSet<String> iMutex = new HashSet<>();
			for (ActionNode action1: actionNodes.get(level).values()) {
				for (ActionNode action2: actionNodes.get(level).values()) {
					if (action1.name.equals(action2.name)) continue;
					//CN
					for (StateNode preCon1: action1.pre)
						for (StateNode preCon2: action2.pre) {
							if (stateMutex.get(level).contains(preCon1.name+ preCon2.name)) {
								actionMutex.get(level).add(action1.name+action2.name);
							    if (!cnMutex.contains(action2.name+","+action1.name)) 
							    	cnMutex.add(action1.name+","+action2.name);
							}
						}
					//IE
					for (StateNode posEff1: action1.pos)
						for (StateNode posEff2: action2.pos) {
							if (posEff1.name.substring(1).equals(posEff2.name)||
									posEff2.name.substring(1).equals(posEff1.name)) {
								actionMutex.get(level).add(action1.name+action2.name);
								if (!ieMutex.contains(action2.name+","+action1.name)) 
								ieMutex.add(action1.name+","+action2.name);
							}
						}
					//I
					for (StateNode pre1: action1.pre)
						for (StateNode pos2: action2.pos) {
							if (pre1.name.substring(1).equals(pos2.name)||
									pos2.name.substring(1).equals(pre1.name))  {
								actionMutex.get(level).add(action1.name+action2.name);
								if (!iMutex.contains(action2.name+","+action1.name)) 
								iMutex.add(action1.name+","+action2.name);
							}
						}
					
					for (StateNode pos1: action1.pos)
						for (StateNode pre2: action2.pre) {
							if (pos1.name.substring(1).equals(pre2.name)||
									pre2.name.substring(1).equals(pos1.name))  {
								actionMutex.get(level).add(action1.name+action2.name);
								if (!iMutex.contains(action2.name+","+action1.name)) 
								iMutex.add(action1.name+","+action2.name);
							}
						}
				}
			}
			printMutex(cnMutex, "CN");
			printMutex(ieMutex, "IE");
			printMutex(iMutex, "I");
	
			
			//4. add state mutex
			stateMutex.add(new HashSet<String>());
			HashSet<String> nlMutex = new HashSet<>();
			HashSet<String> isMutex = new HashSet<>();
			for (StateNode state1: stateNodes.get(level+1).values()) {
				for (StateNode state2: stateNodes.get(level+1).values()) {
					//NL
					if (state1.name.equals(state2.name)) continue;
					if (state1.name.substring(1,state1.name.length()).equals(state2.name)
							|| state2.name.substring(1,state2.name.length()).equals(state1.name)) {
						stateMutex.get(level+1).add(state1.name+state2.name);
						if (!nlMutex.contains(state2.name+","+state1.name)) 
						nlMutex.add(state1.name+","+state2.name);
					}
					//IS
					else {
						boolean isIS = true;
						for (ActionNode pre1: state1.pre) {
							for (ActionNode pre2: state2.pre) {
								if (!actionMutex.get(level).contains(pre1.name+pre2.name)
										&& !actionMutex.get(level).contains(pre2.name+pre1.name)) {
									isIS = false;
								}
							}
						}
						if (isIS) {
							stateMutex.get(level+1).add(state1.name+state2.name);
							if (!isMutex.contains(state2.name+","+state1.name)) 
							isMutex.add(state1.name+","+state2.name);
						}
					}
				}
			}
			printMutex(nlMutex, "NL");
			printMutex(isMutex, "IS");
			
			//5. extract?
			boolean timeToExtract = true;
			for (String g: goal) {
				if (!stateNodes.get(level+1).containsKey(g)) timeToExtract = false; //lose something in the new states
				else {
					for (String g2: goal)
						if (stateMutex.get(level+1).contains(g+g2)) timeToExtract = false; //some goals are mutex
				}
				if (!timeToExtract) break;
			}
			
			if (timeToExtract) {
				System.out.println();
				System.out.println("Extract:");
				for (int i = 0; i < level+1; i++) {
					answerActions.add(new HashSet<String>());
				}
				extract(level+1, goal, new HashSet<String>());
				if (found) solution = true;
			}
			
			//6. levelOff?
			if ((stateNodes.get(level+1).size() == init.size()*2)) {
				if (level1) levelOff = true;
				if (!level1) level1 = true;
			}
			else level1 = false;
			
			//increase the level
			level++;
		}
		
		if (levelOff) System.out.println("LevelOff Fail!");
	}
}
