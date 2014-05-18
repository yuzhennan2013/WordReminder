package zhennan.yu.wordreminder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class RefreshContentIterator implements Serializable, Iterator<Content> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6749605327841095244L;
	HashMap<Character, HashSet<Short>> mContents;
	char start;
	short group;
	boolean hasNext;
	final static char[] CHARS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '-'};
	
	public RefreshContentIterator(HashMap<Character, HashSet<Short>> contents) {
		mContents = contents;
		start = 0;
		group = 0;
		hasNext = false;
	}
	
	@Override
	public boolean hasNext() {
		hasNext = hasNextInner();
		return hasNext;
	}

	private boolean hasNextInner(){
		if (mContents == null) {
			return false;
		}
		HashSet<Short> set;
		while (start < CHARS.length) {
			set = mContents.get(CHARS[start]);
			if (set == null) {
				start ++;
				group = 0;
			}
			else if(set.size() > 0){
				Iterator<Short> iterator = set.iterator();
				while (iterator.hasNext()) {
					short tgroup = iterator.next();
					if (tgroup > group) {
						group = tgroup;
						return true;
					}
				}
				start ++;
				group = 0;
			}
			else {
				group = 0;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Content next() {
		if (hasNext) {
			if (group == 0) {
				return new Content(CHARS[start ++]);
			}
			return new Content(CHARS[start], group);
		}
		return null;
	}

	@Override
	public void remove() {
		
	}

}
