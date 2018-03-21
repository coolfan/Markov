import java.util.*;

import org.atilika.kuromoji.*;
import org.atilika.kuromoji.Tokenizer.Mode;

public class Phrasifier {
	
	private Tokenizer tokenizer;
	
	public Phrasifier() {
		tokenizer = Tokenizer.builder().mode(Mode.NORMAL).build();
	}
	
	public ArrayList<Phrase> phrasify(String sentence) {
		ArrayList<Token> tokens = sanitize(tokenizer.tokenize(sentence));
		ArrayList<Phrase> ret = new ArrayList<>();
		
		if (tokens.isEmpty()) {
			return ret;
		}
		
		Phrase toBuild = new Phrase();
		int state = IDK;
		for (int i = 0; i < tokens.size(); i++) {
			String pos = tokens.get(i).getPartOfSpeech();
			String base = tokens.get(i).getBaseForm();
			String surface = tokens.get(i).getSurfaceForm();
			
			if (pos == null || base == null || surface == null) continue;
			
			switch (state) {
			case IDK:
			{
				toBuild.append(surface);
				
				if (pos.contains("副詞") || pos.contains("形容") && surface.endsWith("く")) {
					toBuild.setType(Phrase.Type.ADVERB);
					ret.add(toBuild);
					toBuild = new Phrase();
					state = IDK;
				} if (pos.contains("名詞") || pos.contains("形容") || pos.contains("連体詞")) {
					state = BUILDING_NOUN;
				} else if (pos.contains("動詞")) {
					state = BUILDING_VERB;
				} else {
					state = IDK;
					toBuild = new Phrase();
				}
			}
			break;
			case BUILDING_NOUN:
			{
				if (pos.contains("記号")) {
					toBuild.setType(Phrase.Type.SUBJECT);
					ret.add(toBuild);
					toBuild = new Phrase();
					state = IDK;
				}
				if (base.contains("する") || base.contains("いたす")) {
					state = BUILDING_VERB;
					toBuild.append(surface);
				} else if (pos.contains("名詞") || pos.contains("形容")) {
					state = BUILDING_NOUN;
					toBuild.append(surface);
				} else if (base.equals("の") || surface.equals("な")) {
					state = BUILDING_NOUN;
					toBuild.append(surface);
				} else if (pos.contains("動詞")) {
					toBuild.setType(Phrase.Type.SUBJECT);
					ret.add(toBuild);
					toBuild = new Phrase(surface);
					state = BUILDING_VERB;
				} else if (pos.contains("助")) {
					state = IDK;
					toBuild.append(surface);
					
					if (base.equals("は") || base.equals("が") || base.equals("も")) {
						toBuild.setType(Phrase.Type.SUBJECT);
					} else if (base.equals("を")) {
						toBuild.setType(Phrase.Type.OBJECT);
					} else if (base.equals("に") && pos.contains("副詞化")) {
						toBuild.setType(Phrase.Type.ADVERB);
					} else {
						toBuild.setType(Phrase.Type.IND_OBJECT);
					}
					
					ret.add(toBuild);
					toBuild = new Phrase();
				}
			}
			break;
			case BUILDING_VERB:
			{
				if (pos.contains("名詞")) {
					state = BUILDING_NOUN;
					toBuild.append(surface);
				} else if (pos.contains("記号")) {
					toBuild.setType(Phrase.Type.VERB);
					ret.add(toBuild);
					toBuild = new Phrase();
					state = IDK;
				} else {
					state = BUILDING_VERB;
					toBuild.append(surface);
				}
			}
			break;
			}
		}
		
		if (state == BUILDING_NOUN) {
			if (tokens.get(tokens.size() - 1).getPartOfSpeech().contains("形容")) {
				toBuild.setType(Phrase.Type.ADJECTIVE);
				ret.add(toBuild);
			} else {
				toBuild.setType(Phrase.Type.OBJECT);
				ret.add(toBuild);
			}
		} else if (state == BUILDING_VERB) {
			toBuild.setType(Phrase.Type.VERB);
			ret.add(toBuild);
		}
		
		return ret;
	}
	
	private ArrayList<Token> sanitize(List<Token> list) {
		ArrayList<Token> cleanList = new ArrayList<>();
		
		for (Token t : list) {
			if (!t.getPartOfSpeech().contains("空白") && !t.getPartOfSpeech().contains("括弧")) {
				cleanList.add(t);
			}
		}
		
		return cleanList;
	}
	
	private static final int BUILDING_NOUN = 0;
	private static final int BUILDING_VERB = 1;
	private static final int IDK = 4;
	
	public static void main(String[] args) {
		Phrasifier p = new Phrasifier();
		String text = "その一つの理由はあまりに多くの人が「行くのはよした方がいい」と忠告してくれたからです";
		ArrayList<Phrase> phrases = p.phrasify(text);
		for (Phrase ph : phrases) {
			System.out.println(ph.toString() + " " + ph.getType());
		}
	}
}
