import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import org.atilika.kuromoji.*;
import org.atilika.kuromoji.Tokenizer.Mode;

import java.io.*;
import java.util.*;

public class Graph {
	
	private Phrasifier phrasifier;
	private TreeMap<Node, TreeSet<Node>> graph;
	
	public Graph() {
		phrasifier = new Phrasifier();
		graph = new TreeMap<>();
	}
	
	public void addLinks(String sentence) {
		ArrayList<Phrase> phrases = phrasifier.phrasify(sentence);
		
		for (int i = 0; i < phrases.size(); i++) {
			ArrayList<Node> keyVals = Node.getNodes(phrases.get(i));
			for (Node key : keyVals) {
				for (Node val : keyVals) {
					if (key != val) {
						if (graph.containsKey(key)) {
							TreeSet<Node> edges = graph.get(key);
							if (!edges.contains(val)) {
								val.addCount();
								edges.add(val);
							} else {
								edges.floor(val).addCount();
							}
						
							graph.floorKey(key).addCount();
						} else {
							key.addCount();
							val.addCount();
							TreeSet<Node> value = new TreeSet<>();
							value.add(val);
							
							graph.put(key, value);
						}
					}
				}
			}
		}
		
		for (int i = 0; i < phrases.size(); i++) {
			for (int j = 0; j < phrases.size(); j++) {
				if (i != j) {
					ArrayList<Node> keys = Node.getNodes(phrases.get(i));
					ArrayList<Node> vals = Node.getNodes(phrases.get(j));
					for (Node key : keys) {
						for (Node val : vals) {
							if (graph.containsKey(key)) {
								TreeSet<Node> edges = graph.get(key);
								if (!edges.contains(val)) {
									val.addCount();
									edges.add(val);
								} else {
									edges.floor(val).addCount();
								}
							
								graph.floorKey(key).addCount();
							} else {
								key.addCount();
								val.addCount();
								TreeSet<Node> value = new TreeSet<>();
								value.add(val);
								
								graph.put(key, value);
							}
						}
					}
				}
			}
		}
	}
	
	public ArrayList<Link> getTopLinks(int k) {
		PriorityQueue<Link> pq = new PriorityQueue<>();
		Node bad = new Node(Utils.toToken("あっ"));
		for (Node n : graph.keySet()) {
			TreeSet<Node> others = graph.get(n);
			for (Node o : others) {
				if (n.token.getPartOfSpeech().contains("助") || o.token.getPartOfSpeech().contains("助")) {
					continue;
				}
				if (n.compareTo(bad) == 0 || o.compareTo(bad) == 0) {
					continue;
				}
				Link link = new Link(n.token, o.token, (double) o.count);
				pq.add(link);
				if (pq.size() > k) {
					pq.poll();
				}
			}
		}
		
		ArrayList<Link> ret = new ArrayList<>();
		
		for (Link l : pq) {
			ret.add(l);
		}
		
		Collections.sort(ret, new Comparator<Link>() {

			@Override
			public int compare(Link arg0, Link arg1) {
				return arg1.compareTo(arg0);
			}
			
		});
		
		return ret;
	}
	
	private static class Node implements Comparable<Node> {
		private Token token;
		private int count;
		
		public Node(Token t) {
			token = t;
			count = 0;
		}
		
		public void addCount() {
			count++;
		}

		@Override
		public int compareTo(Node o) {
			return token.getSurfaceForm().compareTo(o.token.getSurfaceForm());
		}
		
		public static ArrayList<Node> getNodes(Phrase p) {
			ArrayList<Node> ret = new ArrayList<>();
			
			for (Token t : p.getTokens()) {
				ret.add(new Node(t));
			}
			
			return ret;
		}
	}
	
	public class Link implements Comparable<Link> {
		private Token left;
		private Token right;
		private double strength;
		
		public Link(Token left, Token right, double strength) {
			this.left = left;
			this.right = right;
			this.strength = strength;
		}

		@Override
		public int compareTo(Link o) {
			return Double.compare(this.strength, o.strength);
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		Graph g = new Graph();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.equals("done")) break;
			String[] sentences = line.split("。");
			for (String s : sentences) {
				g.addLinks(s);
			}
		}
		
		/*
		TreeSet<Node> edgesTo = g.graph.get(g.graph.floorKey(new Node(Utils.toToken("食物"))));
		ArrayList<Node> sorted = new ArrayList<>(edgesTo);
		Collections.sort(sorted, new Comparator<Node>() {

			@Override
			public int compare(Node arg0, Node arg1) {
				return arg1.count - arg0.count;
			}
			
		});
		for (Node n : sorted) {
			System.out.println(n.token.getSurfaceForm() + " " + n.count);
		}
		*/
		
		ArrayList<Link> links = g.getTopLinks(100);
		for (Link l : links) {
			System.out.println(l.strength + " " + l.left.getBaseForm() + " - " + l.right.getBaseForm());
		}
		
		System.out.println("Done");
	}
}
