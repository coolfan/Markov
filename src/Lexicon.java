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
		System.out.println(root.getName());
	}
	
	public static void main(String[] args) {
		Lexicon lex = new Lexicon("JMdict_e.xml");
	}
}
