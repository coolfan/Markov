import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import javafx.geometry.NodeOrientation;

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
			for (int j = 0; j < phrases.size(); j++) {
				if (i != j) {
					Node key = new Node(phrases.get(i));
					if (graph.containsKey(key)) {
						TreeSet<Node> edges = graph.get(key);
						Node val = new Node(phrases.get(j));
						if (!edges.contains(val)) {
							val.addCount();
							edges.add(val);
						} else {
							edges.floor(val).addCount();
						}
					
						graph.floorKey(key).addCount();
					} else {
						key.addCount();
						Node val = new Node(phrases.get(j));
						val.addCount();
						TreeSet<Node> value = new TreeSet<>();
						value.add(val);
						
						graph.put(key, value);
					}
				}
			}
		}
	}
	
	public ArrayList<Link> getTopLinks(int k) {
		PriorityQueue<Link> pq = new PriorityQueue<>();
		Node bad = new Node(new Phrase("です"));
		for (Node n : graph.keySet()) {
			TreeSet<Node> others = graph.get(n);
			for (Node o : others) {
				if (n.compareTo(bad) == 0 || o.compareTo(bad) == 0) {
					continue;
				}
				Link link = new Link(n.phrase, o.phrase, (double) o.count / n.count);
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
		
		return ret;
	}
	
	private class Node implements Comparable<Node> {
		private Phrase phrase;
		private int count;
		
		public Node(Phrase p) {
			phrase = p;
			count = 0;
		}
		
		public void addCount() {
			count++;
		}

		@Override
		public int compareTo(Node o) {
			return phrase.compareTo(o.phrase);
		}
	}
	
	public class Link implements Comparable<Link> {
		private Phrase left;
		private Phrase right;
		private double strength;
		
		public Link(Phrase left, Phrase right, double strength) {
			this.left = left;
			this.right = right;
			this.strength = strength;
		}

		@Override
		public int compareTo(Link o) {
			return -Double.compare(this.strength, o.strength);
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
		
		TreeSet<Node> edgesTo = g.graph.get(g.graph.floorKey(g.new Node(new Phrase("白い御飯"))));
		
		for (Node n : edgesTo) {
			System.out.println(n.phrase.toString() + " " + n.count);
		}
		
		System.out.println("Done");
	}
}
