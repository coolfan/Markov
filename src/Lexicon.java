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
		
		Element[] children = words[100000].getChildren().toArray(new Element[0]);
		
		Element k_ele = words[100000].getChild("k_ele").getChild("keb");
		
		System.out.println(k_ele.getText());
	}
	
	public static void main(String[] args) {
		Lexicon lex = new Lexicon("JMdict_e.xml");
	}
}
