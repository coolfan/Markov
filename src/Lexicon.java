import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import java.io.*;
import java.util.*;

public class Lexicon {
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
		
		Set<String> posCount = new TreeSet<String>();
		
		for (Element word : words) {
			Element[] senses = word.getChildren("sense").toArray(new Element[0]);
			for (Element sense : senses) {
				Element[] poses = sense.getChildren("pos").toArray(new Element[0]);
				for (Element pos : poses) {
					posCount.add(pos.getText());
				}
			}
		}
		
		for (String pos : posCount) {
			System.out.println(pos);
		}
	}
	
	public static void main(String[] args) {
		Lexicon lex = new Lexicon("JMdict_e.xml");
	}
}
