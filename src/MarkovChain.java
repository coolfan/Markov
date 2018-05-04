import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;
import org.atilika.kuromoji.Tokenizer.Mode;


public class MarkovChain {
	
	private TreeMap<Node, HashMap<String, Integer>> mChain;
	
	public MarkovChain(String input) {
		Tokenizer tokenizer = Tokenizer.builder().mode(Mode.NORMAL).build();
		ArrayList<Token> tokens = new ArrayList<>(tokenizer.tokenize(input));
		
		HashMap<String, ArrayList<String>> graph = new HashMap<>();
		
		for (int i = 0; i < tokens.size() - 1; i++) {
			Token cur = tokens.get(i);
			Token next = tokens.get(i+1);
			
			if (graph.containsKey(cur.getSurfaceForm())) {
				graph.get(cur.getSurfaceForm()).add(next.getSurfaceForm());
			} else {
				ArrayList<String> list = new ArrayList<>();
				list.add(next.getSurfaceForm());
				graph.put(cur.getSurfaceForm(), list);
			}
		}
		
		mChain = new TreeMap<>();
		
		for (String key : graph.keySet()) {
			ArrayList<String> others = graph.get(key);
			HashMap<String, Integer> entries = new HashMap<>();
			
			for (String s : others) {
				if (entries.containsKey(s)) {
					entries.put(s, entries.get(s) + 1);
				} else {
					entries.put(s, 1);
				}
			}
			
			mChain.put(new Node(key, others.size()), entries);
		}
	}
	
	public String build(String seed) {
		Node cur = mChain.floorKey(new Node(seed, 0));
		String res = "";
		
		while (!cur.str.equals("。")) {
			res += cur.str;
			HashMap<String, Integer> entries = mChain.get(cur);
			ArrayList<String> followUps = new ArrayList<>(entries.keySet());
			int[] cumulativeCount = new int[followUps.size()];
			for (int i = 0; i < followUps.size(); i++) {
				if (i == 0) {
					cumulativeCount[i] = entries.get(followUps.get(i));
				} else {
					cumulativeCount[i] = entries.get(followUps.get(i)) + cumulativeCount[i-1];
				}
			}
			//System.out.println(Arrays.toString(cumulativeCount));
			int rand = (int) (Math.random() * cur.count);
			//System.out.println(rand);
			String selected = "";
			for (int i = 0; i < followUps.size(); i++) {
				if (cumulativeCount[i] < rand) {
					continue;
				}
				
				selected = followUps.get(i);
				break;
			}
			
			cur = mChain.floorKey(new Node(selected, 0));
		}
		
		return res;
	}
	
	private static class Node implements Comparable<Node> {
		private String str;
		private int count;
		
		
		public Node(String key, int size) {
			str = key;
			count = size;
		}
		
		@Override
		public int compareTo(Node arg0) {
			return str.compareTo(arg0.str);
		}
	}
	
	public static void main(String[] args) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		
		String input = "";
		String line;
		
		while ((line = reader.readLine()) != null) {
			input += line;
		}
		
		MarkovChain mc = new MarkovChain(input);
		for (int i = 0; i < 50; i++)
			System.out.println(mc.build("小説"));
	}
}
