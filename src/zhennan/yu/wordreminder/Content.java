package zhennan.yu.wordreminder;

import java.io.Serializable;

import cn.sina.youxi.util.CharHelper;

public class Content implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4804741982695348154L;

	char initialChar;
	short groupid;
	
	public Content(char initialChar, short groupid) {
		this.initialChar = CharHelper.toUppercase(initialChar);
		this.groupid = groupid;
	}
	
	public Content(char initialChar) {
		this.initialChar = initialChar;
	}
	
	public char getInitialChar() {
		return initialChar;
	}

	public short getGroupid() {
		return groupid;
	}

	public boolean hasGroup(){
		return groupid > 0;
	}
}
