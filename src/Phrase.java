import java.util.*;

public class Phrase {
	public Phrase(String text, String[] readings, PartOfSpeech[] pos) {
		
	}
	
	public static enum PartOfSpeech {
		VERB,
		ADJECTIVE,
		NOUN,
		ADVERB,
		OTHER;
		
		public static PartOfSpeech[] decipher(String s) {
			ArrayList<PartOfSpeech> list = new ArrayList<>();
			
			if (s.contains("verb")) {
				list.add(VERB);
			}
			
			if (s.contains("adject")) {
				list.add(ADJECTIVE);
			}
			
			if (s.contains("adverb")) {
				list.add(ADVERB);
			}
			
			if (s.contains("noun")) {
				list.add(NOUN);
			}
			
			if (s.isEmpty()) {
				list.add(OTHER);
			}
			
			return list.toArray(new PartOfSpeech[0]);
		}
	}
}
