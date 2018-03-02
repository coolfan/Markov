import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import javafx.geometry.NodeOrientation;

import org.atilika.kuromoji.*;
import org.atilika.kuromoji.Tokenizer.Mode;

import java.io.*;
import java.util.*;

public class Graph {
	
	private Tokenizer tokenizer;
	
	public Graph() {
		tokenizer = Tokenizer.builder().mode(Mode.NORMAL).build();
	}
	
	public static void main(String[] args) {
		Tokenizer tokenizer = Tokenizer.builder().mode(Mode.NORMAL).build();
		String sampleText = "行くのだ";
		
		List<Token> tokens = tokenizer.tokenize(sampleText);
		
		for (Token t : tokens) {
			System.out.println(t.getAllFeatures());
		}
	}
}
