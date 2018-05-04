import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;
import org.atilika.kuromoji.Tokenizer.Mode;

import java.util.*;

public class SyntacticAnalyzer {
	public SyntacticAnalyzer(ArrayList<Token> tokens) {
		ArrayList<Phrase> phrases = analyze(tokens);

		while (phrases.size() > 1) {
			for (Phrase p : phrases) {
				System.out.print(p.allText + " ");
			}
			merge(phrases);
			System.out.println();
		}
	}
	
	private void merge(ArrayList<Phrase> phrases) {
		boolean[] mergeable = new boolean[phrases.size() - 1];
		int cutoff = mergeable.length;
		for (int i = 0; i < mergeable.length; i++) {
			mergeable[i] = (Phrase.getFusedType(phrases.get(i), phrases.get(i+1)) != PhraseType.NONE);
			if ((phrases.get(i+1).type == PhraseType.VERB || phrases.get(i+1).type == PhraseType.AUX_VERB)) {
				cutoff = i+1;
				break;
			}
		}
		mergeable = Arrays.copyOfRange(mergeable, 0, cutoff);
		int index = highestPriority(mergeable, phrases);
		Phrase left = phrases.remove(index);
		Phrase right = phrases.remove(index);
		Phrase parent = new Phrase(left, right, Phrase.getFusedType(left, right), null);
		phrases.add(index, parent);
	}
	
	private int highestPriority(boolean[] mergeable, ArrayList<Phrase> phrases) {
		int highest = 0;
		int index = -1;
		
		for (int i = 0; i < mergeable.length; i++) {
			if (!mergeable[i]) {
				continue;
			}
			
			int priority = getPriority(phrases.get(i), phrases.get(i+1));
			if (priority > highest) {
				highest = priority;
				index = i;
			}
		}
		
		return index;
	}
	
	private int getPriority(Phrase left, Phrase right) {
		if (right.type == PhraseType.MARKER) {
			return 10;
		}
		
		if ((left.type == PhraseType.ADJ || left.type == PhraseType.SENTENCE) && (right.type == PhraseType.NOUN || right.type == PhraseType.SUBJECT || right.type == PhraseType.OBJECT)) {
			return 8;
		}
		
		if (left.type == PhraseType.ADV && right.type == PhraseType.ADJ) {
			return 9;
		}
		
		if (left.type == PhraseType.ADV && right.type == PhraseType.VERB) {
			return 7;
		}
		
		if (left.type == PhraseType.ADV && right.type == PhraseType.PREDICATE) {
			return 6;
		}
		
		if (right.type == PhraseType.AUX_VERB && left.type == PhraseType.SENTENCE) {
			return 3;
		}
		
		if (right.type == PhraseType.AUX_VERB && left.type == PhraseType.PREDICATE) {
			return 4;
		}
		
		if ((right.type == PhraseType.VERB || right.type == PhraseType.AUX_VERB) && left.type == PhraseType.OBJECT) {
			return 5;
		}
		
		if ((right.type == PhraseType.PREDICATE || right.type == PhraseType.VERB || right.type == PhraseType.AUX_VERB) && left.type == PhraseType.SUBJECT) {
			return 2;
		}
		
		return 0;
	}
	
	private ArrayList<Phrase> analyze(ArrayList<Token> tokens) {
		ArrayList<Phrase> ret = new ArrayList<>();
		for (Token t : tokens) {
			if (t.getPartOfSpeech().contains("助詞") || (t.getSurfaceForm().equals("な") && t.getPartOfSpeech().contains("助動詞"))) {
				ret.add(new Phrase(null, null, PhraseType.MARKER, t));
			} else if (t.getPartOfSpeech().contains("助動詞") || (t.getPartOfSpeech().contains("動詞") && t.getPartOfSpeech().contains("非自立"))) {
				ret.add(new Phrase(null, null, PhraseType.AUX_VERB, t));
			} else if (t.getPartOfSpeech().contains("副詞")) {
				ret.add(new Phrase(null, null, PhraseType.ADV, t));
			} else if (t.getPartOfSpeech().contains("名詞")) {
				ret.add(new Phrase(null, null, PhraseType.NOUN, t));
			} else if (t.getPartOfSpeech().contains("動詞")) {
				ret.add(new Phrase(null, null, PhraseType.VERB, t));
			} else if (t.getPartOfSpeech().contains("形容詞")) {
				if (t.getSurfaceForm().endsWith("く")) {
					ret.add(new Phrase(null, null, PhraseType.ADV, t));
				} else {
					ret.add(new Phrase(null, null, PhraseType.ADJ, t));
				}
			}
		}
		
		return ret;
	}
	
	public static class Phrase {
		public Phrase left, right;
		public String allText;
		public Token head;
		public PhraseType type;
		
		public Phrase(Phrase left, Phrase right, PhraseType type, Token head) {
			this.left = left;
			this.right = right;
			if (left != null && right != null) {
				this.allText = left.allText + right.allText;
			} else {
				this.allText = head.getSurfaceForm();
			}
			this.head = head;
			this.type = type;
		}
		
		public static PhraseType getFusedType(Phrase left, Phrase right) {
			if (right.type == PhraseType.MARKER) {
				if (right.head.getBaseForm().equals("が") || right.head.getBaseForm().equals("は")) {
					return PhraseType.SUBJECT;
				}
				
				if (right.head.getBaseForm().equals("を")) {
					return PhraseType.OBJECT;
				}
				
				if ((right.head.getSurfaceForm().equals("な") || right.head.getBaseForm().equals("の"))) {
					if (left.type == PhraseType.NOUN || left.type == PhraseType.ADV) {
						return PhraseType.ADJ;
					}
				}
				
				if (right.head.getBaseForm().equals("と") && right.head.getPartOfSpeech().contains("引用") && (left.type == PhraseType.SENTENCE || left.type == PhraseType.PREDICATE)) {
					return PhraseType.OBJECT;
				}
				
				if (right.head.getBaseForm().equals("に") && right.head.getPartOfSpeech().contains("副詞化") && left.type == PhraseType.NOUN) {
					return PhraseType.ADV;
				}
				
				if (right.head.getBaseForm().equals("て") && left.type == PhraseType.VERB) {
					return PhraseType.VERB;
				}
				
				if (left.type == PhraseType.NOUN || left.type == PhraseType.SENTENCE) {
					return PhraseType.OBJECT;
				}
			}
			
			if ((left.type == PhraseType.ADJ || left.type == PhraseType.SENTENCE) && right.type == PhraseType.NOUN) {
				return PhraseType.NOUN;
			}
			
			if ((left.type == PhraseType.ADJ || left.type == PhraseType.SENTENCE) && right.type == PhraseType.SUBJECT) {
				return PhraseType.SUBJECT;
			}
			
			if ((left.type == PhraseType.ADJ || left.type == PhraseType.SENTENCE) && right.type == PhraseType.OBJECT) {
				return PhraseType.OBJECT;
			}
			
			if (left.type == PhraseType.ADV && right.type == PhraseType.ADJ) {
				return PhraseType.ADJ;
			}
			
			if (left.type == PhraseType.ADV && right.type == PhraseType.VERB) {
				return PhraseType.VERB;
			}
			
			if (left.type == PhraseType.ADV && right.type == PhraseType.PREDICATE) {
				return PhraseType.PREDICATE;
			}
			
			if (right.type == PhraseType.AUX_VERB && left.type == PhraseType.SENTENCE) {
				return PhraseType.SENTENCE;
			}
			
			if (right.type == PhraseType.AUX_VERB && left.type == PhraseType.PREDICATE) {
				return PhraseType.PREDICATE;
			}
			
			if ((right.type == PhraseType.VERB || right.type == PhraseType.AUX_VERB) && left.type == PhraseType.OBJECT) {
				return PhraseType.PREDICATE;
			}
			
			if (right.type == PhraseType.PREDICATE && left.type == PhraseType.SUBJECT) {
				return PhraseType.SENTENCE;
			}
			
			if ((right.type == PhraseType.VERB || right.type == PhraseType.AUX_VERB) && left.type == PhraseType.SUBJECT) {
				return PhraseType.SENTENCE;
			}
			
			return PhraseType.NONE;
		}
	}
	
	public enum PhraseType {
		NOUN,
		VERB,
		AUX_VERB,
		ADJ,
		ADV,
		MARKER,
		SENTENCE,
		SUBJECT,
		OBJECT,
		PREDICATE,
		NONE
	}
	
	public static void main(String[] args) {
		ArrayList<Token> tokens = new ArrayList<>(Tokenizer.builder().mode(Mode.NORMAL).build().tokenize("一つのりんごがあります"));
		SyntacticAnalyzer sa = new SyntacticAnalyzer(tokens);
		
	}
}
