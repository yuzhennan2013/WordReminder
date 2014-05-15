package zhennan.yu.wordreminder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.util.Log;

/**
 * @author Administrator
 * keeps which group should be refresh
 * for efficiency reason
 */
public class RefreshContentFilter implements Serializable, Iterable<Content>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 659164523679486403L;
	HashMap<Character, HashSet<Short>> contents;
	boolean refreshall;
	
	/**
	 * if refreshall = true: means you want to refresh all 26 initial chars
	 * otherwise, refresh on demands
	 * @param refreshall
	 */
	public RefreshContentFilter(boolean refreshall) {
		contents = new HashMap<Character, HashSet<Short>>(26);
		this.refreshall = refreshall;
		if (refreshall) {
			for (char character : RefreshContentIterator.CHARS) {
				addContent(new Content(character));
			}
		}
	}
	
	public int size() {
		if (refreshall) {
			return contents.size();
		}
		int cnt = 0;
		Set<HashMap.Entry<Character, HashSet<Short>>> set = contents.entrySet();
		for (Iterator<HashMap.Entry<Character, HashSet<Short>>> i = set.iterator(); i.hasNext();) 
		{
			HashMap.Entry<Character, HashSet<Short>> item = i.next();
			cnt += contents.get((char)(item.getKey())).size();
		}
		return cnt;
	}
	
	public void addContent(Content content) {
		if (contents.get(content.initialChar) == null) {
			if (refreshall) {
				contents.put(content.initialChar, new HashSet<Short>(1));			
			}
			else {
				contents.put(content.initialChar, new HashSet<Short>(20));	
			}
		}
		// when refreshall, content.groupid will always be 0
		if (content.groupid > 0) {
			contents.get(content.initialChar).add(content.groupid);	
		}
	}

	@Override
	public Iterator<Content> iterator() {
		return new RefreshContentIterator(contents);
	}
}
