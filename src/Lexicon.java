import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import java.io.*;
import java.util.*;

public class Lexicon {
	
	private Word[] lexicon;
	
	public Lexicon(String filepath) {
		File lexFile = new File(filepath);
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try {
			doc = builder.build(lexFile);
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
		
		Element root = doc.getRootElement();
		Element[] words = root.getChildren().toArray(new Element[0]);
		
		ArrayList<Word> wordlist = new ArrayList<>();
		
		for (Element word : words) {
			Element[] r_ele = word.getChildren("r_ele").toArray(new Element[0]);
			Element[] k_ele = word.getChildren("k_ele").toArray(new Element[0]);
			Element[] sense = word.getChildren("sense").toArray(new Element[0]);
			
			String text = "";
			if (k_ele.length != 0) {
				text = k_ele[0].getText();
			} else if (r_ele.length != 0){
				text = r_ele[0].getText();
			} else {
				continue;
			}
			
			Set<String> readings = new TreeSet<>();
			for (Element e : r_ele) {
				readings.add(e.getText());
			}
			
			Set<Word.PartOfSpeech> poses = new TreeSet<>();
			for (Element s : sense) {
				for (Element pos : s.getChildren("pos")) {
					Word.PartOfSpeech[] templist = Word.PartOfSpeech.decipher(pos.getText());
					for (Word.PartOfSpeech p : templist) {
						poses.add(p);
					}
				}
			}
			
			Word w = new Word(text, readings.toArray(new String[0]),
					poses.toArray(new Word.PartOfSpeech[0]));
			
			wordlist.add(w);
		}
		
		lexicon = wordlist.toArray(new Word[0]);
		System.out.println(lexicon.length);
	}
	
	public static void main(String[] args) {
		Lexicon lex = new Lexicon("JMdict_e.xml");
	}
}
