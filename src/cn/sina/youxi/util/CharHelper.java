package cn.sina.youxi.util;

public class CharHelper {
	public static char toUppercase(char lowercase) {
		if (lowercase >= 'a' && lowercase <= 'z') {
			return (char) (lowercase - ('a' - 'A'));
		}
		return lowercase;
	}
}
