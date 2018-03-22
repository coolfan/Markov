import java.util.*;

import org.atilika.kuromoji.Token;

public class Phrase implements Comparable<Phrase> {
	
	private ArrayList<Token> text;
	private String str;
	private Type type;
	
	public Phrase() {
		text = new ArrayList<>();
		str = "";
	}
	
	public Phrase(Token text) {
		this();
		append(text);
	}
	
	public void append(Token more) {
		text.add(more);
		str += more.getSurfaceForm();
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public ArrayList<Token> getTokens() {
		return text;
	}
	
	@Override
	public String toString() {
		return str;
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
		return this.toString().compareTo(that.toString());
	}
}
