package zhennan.yu.wordreminder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;



/**
 * @author Administrator structure is like this
 * 
 *         A1U,A1R,A1F ^ | A-> A1 -> A2 -> A3 ... B-> .....
 */
public class WordGroupContainer {
	HashMap<Character, ArrayList<WordGroup>> wordGroupContainer;

	public WordGroupContainer() {
		wordGroupContainer = new HashMap<Character, ArrayList<WordGroup>>(RefreshContentIterator.CHARS.length);
	}

	/**
	 * traverse WordGroupContainer and fill global arrayList with items
	 * arrayList is used for adapter to display the main page
	 */
	public void traverse(ArrayList<IndexItem> arrayList) {
		for (char character : RefreshContentIterator.CHARS) {
			ArrayList<WordGroup> groupArr = wordGroupContainer.get(character);
			if (groupArr != null) {
				Collections.sort(groupArr, new Comparator<WordGroup>() {

					@Override
					public int compare(WordGroup lhs, WordGroup rhs) {
						return lhs.groupid - rhs.groupid;
					}
				});
				for (int i1 = 0; i1 < groupArr.size(); i1++) {
					WordGroup group = groupArr.get(i1);
					if (group.getUntested() != null) {
						arrayList.add(group.getUntested());
					}
					if (group.getForgotten() != null) {
						arrayList.add(group.getForgotten());
					}
					if (group.getRemembered() != null) {
						arrayList.add(group.getRemembered());
					}
				}
			}
		}
	}

	/**
	 * delete a char branch, like delete 'a' and 'b' branch
	 * so that 'a' and 'b' will never be on the mainpage 
	 * @param initialchar
	 */
	public void deleteBranch(char initialchar) {
		wordGroupContainer.remove(initialchar);
	}
	
	public void deleteGroup(WordGroup wordGroup) {
		
		ArrayList<WordGroup> groups = wordGroupContainer.get(wordGroup.initialchar);
		if (groups == null) {
			return;
		}
		
		for (WordGroup wordGroup2 : groups) {
			if (wordGroup2.groupid == wordGroup.groupid) {
				groups.remove(wordGroup2);
				break;
			}
		}
	}
	
	/**
	 * this must be called after contains check
	 * @param wordGroup
	 */
	public void updateGroup(WordGroup wordGroup, short index) {
		ArrayList<WordGroup> groups = wordGroupContainer.get(wordGroup.initialchar);
		groups.set(index, wordGroup);
	}
	
	public short indexOf(WordGroup wordGroup) {
		
		ArrayList<WordGroup> groups = wordGroupContainer.get(wordGroup.initialchar);
		if (groups == null) {
			return -1;
		}
		
		for (short i = 0; i < groups.size(); i++) {
			if (groups.get(i).groupid == wordGroup.groupid) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * this must be called after contains check
	 * @param wordGroup
	 */
	public void addGroup(WordGroup wordGroup) {
		if (wordGroupContainer.get(wordGroup.initialchar) == null) {
			// here for performance reason, i dont query DB for how many
			// groups
			// of current initialchar, but just set a upper limit
			// 20 means there are 2000 words under this single initialchar
			// that's really enough
			wordGroupContainer.put(wordGroup.initialchar, new ArrayList<WordGroup>(20));
		}
		wordGroupContainer.get(wordGroup.initialchar).add(wordGroup);
	}

	public void destroy() {
		if (wordGroupContainer != null) {
			Iterator<Entry<Character, ArrayList<WordGroup>>> iter = wordGroupContainer.entrySet().iterator();
			while (iter.hasNext()) {
				@SuppressWarnings("rawtypes")
				Map.Entry entry = (Map.Entry) iter.next();
				@SuppressWarnings("unchecked")
				ArrayList<WordGroup> groups = (ArrayList<WordGroup>) entry.getValue();
				if (groups != null) {
					for (int i = 0; i < groups.size(); i++) {
						groups.get(i).destroy();
					}
					groups.clear();
				}
			}
			wordGroupContainer.clear();
			wordGroupContainer = null;
		}
	}
}