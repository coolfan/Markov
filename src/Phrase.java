import java.util.*;

public class Phrase implements Comparable<Phrase> {
	
	private String text;
	private Type type;
	
	public Phrase() {
		text = "";
	}
	
	public Phrase(String text) {
		this.text = text;
	}
	
	public void append(String more) {
		text += more;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return text;
	}
	
	public Type getType() {
		return type;
	}
	
	public enum Type {
		SUBJECT,
		OBJECT,
		VERB,
		IND_OBJECT,
		ADJECTIVE,
		ADVERB
	}

	@Override
	public int compareTo(Phrase that) {
		return this.text.compareTo(that.text);
	}
}
