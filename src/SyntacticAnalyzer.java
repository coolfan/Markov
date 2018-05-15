import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;
import org.atilika.kuromoji.Tokenizer.Mode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.*;

public class SyntacticAnalyzer {
	
	HashMap<String, ArrayList<Phrase>> verbToDirObj;
	HashMap<String, ArrayList<Phrase>> verbToIndObj;
	HashMap<String, ArrayList<Phrase>> verbToSub;
	HashSet<String> verbs;
	
	HashSet<String> cache;
	
	public SyntacticAnalyzer() {
		verbToDirObj = new HashMap<>();
		verbToIndObj = new HashMap<>();
		verbToSub = new HashMap<>();
		verbs = new HashSet<>();
		cache = new HashSet<>();
	}
	
	public void analyze(ArrayList<Token> tokens) {
		ArrayList<Phrase> phrases = tokenize(tokens);
		phrases = preprocess(phrases);
		int prevSize = 0;
		while (phrases.size() > 1) {
			int chunkEnd = getChunkEndIndex(phrases);
			//System.out.println(chunkEnd + " " + phrases.size());
			ArrayList<Phrase> chunk = new ArrayList<>();
			for (int i = 0; i < chunkEnd; i++) {
				chunk.add(phrases.remove(0));
			}
			
			while (oneStep(chunk)) {
				/*for (Phrase p : chunk) {
					System.out.print(p.allText + p.type + "\t");
				}
				System.out.println();*/
				for (Phrase p : chunk) {
					if (p.type == PhraseType.VP && !cache.contains(p.allText)) {
						Phrase verb = findVerb(p);
						ArrayList<Phrase> dirObj = getObj(p, PPType.PPO);
						ArrayList<Phrase> indObj = getObj(p, PPType.PPI);
						
						for (Phrase obj : dirObj) {
							insert(verbToDirObj, verb.allText, obj);
						}
						
						for (Phrase obj : indObj) {
							insert(verbToIndObj, verb.allText, obj);
						}
						
						verbs.add(verb.allText);
						cache.add(p.allText);
					}
					
					if (p.type == PhraseType.TP && !cache.contains(p.allText)) {
						Phrase vp = findVP(p);
						if (vp == null) {
							vp = p;
						}
						Phrase verb = findVerb(vp);
						if (p.left != null && p.left.type == PhraseType.PP && p.left.ppType == PPType.PPS) {
							insert(verbToSub, verb.allText, p.left);
						}
						cache.add(p.allText);
					}
				}
			}
			
			for (int i = chunk.size() - 1; i >= 0; i--) {
				phrases.add(0, chunk.get(i));
			}
			
			/*if (phrases.size() == 2) {
				break;
			}*/
			
			if (phrases.size() == prevSize) break;
			
			prevSize = phrases.size();
		}
	}
	
	private void insert(HashMap<String, ArrayList<Phrase>> map, String key, Phrase p) {
		if (map.containsKey(key)) {
			map.get(key).add(p);
		} else {
			ArrayList<Phrase> newList = new ArrayList<>();
			newList.add(p);
			map.put(key,  newList);
		}
	}
	
	private ArrayList<Phrase> getObj(Phrase p, PPType ppType) {
		
		if (p == null) {
			return new ArrayList<>();
		}
		ArrayList<Phrase> child = getObj(p.right, ppType);
		if (p.left != null && p.left.type == PhraseType.PP && p.left.ppType == ppType) {
			child.add(p.left);
		}
		return child;
	}
	
	private Phrase findVerb(Phrase p) {
		
		if (p == null) {
			return null;
		}
		
		if (p.right != null) {
			return findVerb(p.right);
		}
		
		return p;
	}
	
	private Phrase findVP(Phrase p) {
		if (p == null) {
			return null;
		}
		
		if (p.type == PhraseType.VP) {
			return p;
		}
		
		Phrase vp = findVP(p.right);
		if (vp == null) {
			return findVP(p.left);
		}
		
		return vp;
	}
		
	private ArrayList<Phrase> tokenize(ArrayList<Token> tokens) {
		ArrayList<Phrase> ret = new ArrayList<>();
		for (Token t : tokens) {
			//System.out.println(t.getPartOfSpeech());
			if (t.getPartOfSpeech().contains("副詞")) {
				ret.add(new Phrase(null, null, PhraseType.ADV, t.getSurfaceForm()));
			} else if (t.getPartOfSpeech().contains("助詞") || (t.getSurfaceForm().equals("な") && t.getPartOfSpeech().contains("助動詞"))) {
				ret.add(new Phrase(null, null, PhraseType.P, t.getSurfaceForm(), t));
			} else if (t.getPartOfSpeech().contains("助動詞") || (t.getPartOfSpeech().contains("動詞") && t.getPartOfSpeech().contains("非自立"))) {
				ret.add(new Phrase(null, null, PhraseType.T, t.getSurfaceForm(), t));
			} else if (t.getPartOfSpeech().contains("名詞")) {
				ret.add(new Phrase(null, null, PhraseType.N, t.getSurfaceForm(), t));
			} else if (t.getPartOfSpeech().contains("動詞")) {
				ret.add(new Phrase(null, null, PhraseType.V, t.getSurfaceForm(), t));
			} else if (t.getPartOfSpeech().contains("形容詞")) {
				if (t.getSurfaceForm().endsWith("く")) {
					ret.add(new Phrase(null, null, PhraseType.ADV, t.getSurfaceForm(), t));
				} else {
					ret.add(new Phrase(null, null, PhraseType.ADJ, t.getSurfaceForm(), t));
				}
			}
		}
		
		return ret;
	}
	
	private ArrayList<Phrase> preprocess(ArrayList<Phrase> phrases) {
		ArrayList<Phrase> ret = new ArrayList<>();
		String concat = "";
		boolean build = false;
		for (int i = 0; i < phrases.size()-1; i++) {
			Phrase cur = phrases.get(i);
			Phrase next = phrases.get(i+1);
			
			if (!build) {
				if ((cur.type == PhraseType.N || cur.type == PhraseType.V || cur.type == PhraseType.P || cur.type == PhraseType.T) && cur.type == next.type) {
					build = true;
					concat = cur.allText;
				} else {
					ret.add(cur);
				}
			} else {
				if ((cur.type == PhraseType.N || cur.type == PhraseType.V || cur.type == PhraseType.P || cur.type == PhraseType.T) && cur.type != next.type) {
					build = false;
					concat += cur.allText;
					ret.add(new Phrase(null, null, cur.type, concat));
					concat = "";
				} else {
					concat += cur.allText;
				}
			}
		}
		
		if (build) {
			ret.add(new Phrase(null, null, phrases.get(phrases.size()-1).type, concat));
		} else {
			ret.add(phrases.get(phrases.size()-1));
		}
		
		/*phrases = ret;
		ret = new ArrayList<>();
		concat = "";
		int i;
		for (i = 0; i < phrases.size()-1; i++) {
			Phrase cur = phrases.get(i);
			Phrase next = phrases.get(i+1);
			
			if (cur.type == PhraseType.N && next.type == PhraseType.V && next.head.getBaseForm().equals("する")) {
				ret.add(new Phrase(null, null, PhraseType.V, cur.allText + next.allText));
				i++;
			} else {
				ret.add(cur);
			}
		}
		
		if (i != phrases.size()) {
			ret.add(phrases.get(phrases.size() - 1));
		}*/
		
		return ret;
	}
	
	private int getChunkEndIndex(ArrayList<Phrase> phrases) {
		int index = 0;
		boolean approaching = false;
		for (Phrase p : phrases) {
			if (approaching && p.type != PhraseType.V && p.type != PhraseType.T) {
				break;
			}
			
			if (p.type == PhraseType.V || p.type == PhraseType.T) {
				approaching = true;
			}
			index++;
		}
		
		return index;
	}
	
	private boolean oneStep(ArrayList<Phrase> phrases) {
		Result best = null;
		int bestIndex = -1;
		for (int i = 0; i < phrases.size() - 1; i++) {
			Result cur = testMerge(phrases.get(i), phrases.get(i+1));
			if ((best == null) || (cur != null && best.priority < cur.priority)) {
				best = cur;
				bestIndex = i;
			}
		}
		
		if (best != null) {
			Phrase left = phrases.remove(bestIndex);
			Phrase right = phrases.remove(bestIndex);
			
			Phrase merged = new Phrase(left, right, best.mergeType, null);
			if (best.mergeType == PhraseType.PP) {
				if (right.allText.equals("が") || right.allText.equals("は") || right.allText.equals("も")) {
					merged.ppType = PPType.PPS;
				} else if (right.allText.equals("を")) {
					merged.ppType = PPType.PPO;
				} else {
					merged.ppType = PPType.PPI;
				}
			}
			phrases.add(bestIndex, merged);
			return true;
		}
		
		for (int i = 0; i < phrases.size(); i++) {
			Result cur = testPromotion(phrases.get(i));
			if ((best == null) || (cur != null && best.priority < cur.priority)) {
				best = cur;
				bestIndex = i;
			}
		}
		
		if (best != null) {
			phrases.get(bestIndex).type = best.mergeType;
			if (best.mergeType == PhraseType.PP) {
				phrases.get(bestIndex).ppType = PPType.PPO;
			}
			return true;
		}

		return false;
	}
	
	private Result testMerge(Phrase left, Phrase right) {
		if (left.type == PhraseType.ADVBAR && right.type == PhraseType.ADJBAR) {
			return new Result(PhraseType.ADJBAR, 20);
		}
		
		if (left.type == PhraseType.ADJP && right.type == PhraseType.NBAR) {
			return new Result(PhraseType.NBAR, 19);
		}
		
		if (left.type == PhraseType.ADJP && right.type == PhraseType.ADVBAR) {
			return new Result(PhraseType.ADVBAR, 19);
		}
		
		if (left.type == PhraseType.ADJP && right.type == PhraseType.ADVP) {
			return new Result(PhraseType.ADVP, 19);
		}
		
		if ((left.type == PhraseType.ADJP || left.type == PhraseType.ADVP) && right.type == PhraseType.P) {
			return new Result(PhraseType.ADVP, 18);
		}
		
		if (left.type == PhraseType.NP && right.type == PhraseType.P) {
			if (right.allText.equals("な") || right.allText.equals("の")) {
				return new Result(PhraseType.ADJBAR, 17);
			}
			return new Result(PhraseType.PP, 17);
		}
		
		if (left.type == PhraseType.TP && right.type == PhraseType.NBAR) {
			return new Result(PhraseType.NBAR, 16);
		}
		
		if (left.type == PhraseType.TP && right.type == PhraseType.P) {
			return new Result(PhraseType.PP, 15);
		}
		
		if (left.type == PhraseType.PP && left.ppType == PPType.PPO && (right.type == PhraseType.V || right.type == PhraseType.T || right.type == PhraseType.VBAR || right.type == PhraseType.TBAR)) {
			return new Result(PhraseType.VBAR, 14);
		}
		
		if (left.type == PhraseType.PP && left.ppType == PPType.PPI && (right.type == PhraseType.VBAR || right.type == PhraseType.TBAR)) {
			return new Result(PhraseType.VBAR, 13);
		}
		
		if (left.type == PhraseType.ADVP && right.type == PhraseType.VBAR) {
			return new Result(PhraseType.VBAR, 12);
		}
		
		if (left.type == PhraseType.ADVP && right.type == PhraseType.VP) {
			return new Result(PhraseType.VP, 12);
		}
		
		if (left.type == PhraseType.ADVP && right.type == PhraseType.TBAR) {
			return new Result(PhraseType.TBAR, 12);
		}
		
		if (left.type == PhraseType.ADVP && right.type == PhraseType.TP) {
			return new Result(PhraseType.TP, 12);
		}
		
		if (left.type == PhraseType.VP && right.type == PhraseType.T) {
			return new Result(PhraseType.TBAR, 11);
		}
		
		if (left.type == PhraseType.TBAR && right.type == PhraseType.T) {
			return new Result(PhraseType.TBAR, 10);
		}
		
		if (left.type == PhraseType.PP && left.ppType == PPType.PPS && right.type == PhraseType.TBAR) {
			return new Result(PhraseType.TP, 9);
		}
		
		return null;
	}
	
	private Result testPromotion(Phrase p) {
		if (p.type == PhraseType.ADVBAR) {
			return new Result(PhraseType.ADVP, 20);
		}
		
		if (p.type == PhraseType.ADV) {
			return new Result(PhraseType.ADVBAR, 19);
		}
		
		if (p.type == PhraseType.ADJBAR) {
			return new Result(PhraseType.ADJP, 18);
		}
		
		if (p.type == PhraseType.ADJ) {
			return new Result(PhraseType.ADJBAR, 17);
		}
		
		if (p.type == PhraseType.NP) {
			return new Result(PhraseType.PP, 16);
		}
		
		if (p.type == PhraseType.NBAR) {
			return new Result(PhraseType.NP, 15);
		}
		
		if (p.type == PhraseType.N) {
			return new Result(PhraseType.NBAR, 14);
		}
		
		if (p.type == PhraseType.VP) {
			return new Result(PhraseType.TBAR, 13);
		}
		
		if (p.type == PhraseType.VBAR) {
			return new Result(PhraseType.VP, 12);
		}
		
		if (p.type == PhraseType.V) {
			return new Result(PhraseType.VBAR, 11);
		}
		
		if (p.type == PhraseType.ADJP) {
			return new Result(PhraseType.VBAR, 10);
		}
		
		if (p.type == PhraseType.TBAR) {
			return new Result(PhraseType.TP, 9);
		}
		
		if (p.type == PhraseType.T) {
			return new Result(PhraseType.TBAR, 8);
		}
		
		return null;
	}
	
	public void printStatus() {
		System.out.println("Verb to Dir Obj");
		for (String key : verbToDirObj.keySet()) {
			System.out.println(key + ": " + verbToDirObj.get(key).toString());
		}
		System.out.println("Verb to Ind Obj");
		for (String key : verbToIndObj.keySet()) {
			System.out.println(key + ": " + verbToIndObj.get(key).toString());
		}
		System.out.println("Verb to Sub");
		for (String key : verbToSub.keySet()) {
			System.out.println(key + ": " + verbToSub.get(key).toString());
		}
		System.out.println("Verbs");
		System.out.println(verbs.toString());
	}
	
	public static class Phrase {
		public Phrase left, right;
		public String allText;
		public PhraseType type;
		public PPType ppType;
		public Token head;
		
		public Phrase(Phrase left, Phrase right, PhraseType type, String text) {
			this.left = left;
			this.right = right;
			if (left != null && right != null) {
				this.allText = left.allText + right.allText;
			} else {
				this.allText = text;
			}
			this.type = type;
		}
		
		public Phrase(Phrase left, Phrase right, PhraseType type, String text, Token head) {
			this(left, right, type, text);
			this.head = head;
		}
		
		public String toString() {
			return allText;
		}
	}
	
	public static class Result {
		public PhraseType mergeType;
		public int priority;
		
		public Result(PhraseType mergeType, int priority) {
			this.mergeType = mergeType;
			this.priority = priority;
		}
	}
	
	public enum PhraseType {
		N, NBAR, NP, V, VBAR, VP, T, TBAR, TP, ADJ, ADJBAR, ADJP, ADV, ADVBAR, ADVP, P, PP, NONE
	}
	
	public enum PPType {
		PPS, PPI, PPO
	}
	
	public static void main(String[] args) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(new File("examples.utf")));
		String line;
		SyntacticAnalyzer sa = new SyntacticAnalyzer();
		int i = 0;
		while ((line = reader.readLine()) != null) {
			String[] breaks = line.substring(3).split("\t");
			ArrayList<Token> tokens = new ArrayList<>(Tokenizer.builder().mode(Mode.NORMAL).build().tokenize(breaks[0]));
			sa.analyze(tokens);
			reader.readLine();
		}
		//sa.printStatus();
		reader.close();
		System.out.println("Done");
		
		TreeSet<String> verbs = new TreeSet<>(sa.verbs);
		 
		reader = new BufferedReader(new InputStreamReader(System.in));
		while ((line = reader.readLine()) != null) {
			String[] arr = line.split(" ");
			String verb = verbs.floor(arr[0]);
			String verbType = arr[1];
			String subject = "", dirObj = "", indObj = "";
			if (verbType.equals("T")) {
				subject = sample(sa.verbToSub.get(verb));
				dirObj = sample(sa.verbToDirObj.get(verb));
			} else {
				subject = sample(sa.verbToSub.get(verb));
				indObj = sample(sa.verbToIndObj.get(verb));
			}
			System.out.println(subject + indObj + dirObj + verb);
		}
	}
	
	private static String sample(ArrayList<Phrase> list) {
		if (list == null || list.size() == 0) {
			return "";
		}
		int index = (int) (Math.random() * list.size());
		return list.get(index).allText;
	}
}
