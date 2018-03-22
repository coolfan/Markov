import org.atilika.kuromoji.Token;


public class Utils {
	public static Token toToken(String str) {
		return new Token(0, str, null, 0, null);
	}
}
