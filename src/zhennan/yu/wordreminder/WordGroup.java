package zhennan.yu.wordreminder;

import java.util.HashMap;

public class WordGroup {
	short groupid;
	char initialchar;

	public void destroy() {
		if (wordGroup != null) {
			wordGroup.clear();
		}
	}

	HashMap<String, IndexItem> wordGroup;

	public WordGroup(short groupid, char initialchar) {
		this.wordGroup = new HashMap<String, IndexItem>(3);
		this.groupid = groupid;
		this.initialchar = initialchar;
	}

	public boolean isEmpty() {
		return 
				(wordGroup.get(Config.CATEGORY_UNTESTED) == null)
				&& (wordGroup.get(Config.CATEGORY_REMEMBERED) == null)
				&& (wordGroup.get(Config.CATEGORY_FORGOTTEN) == null);
	}
	
	public void addUntested(IndexItem indexItem) {
		wordGroup.put(Config.CATEGORY_UNTESTED, indexItem);
	}

	public IndexItem getUntested() {
		return wordGroup.get(Config.CATEGORY_UNTESTED);
	}

	public void addRemembered(IndexItem indexItem) {
		wordGroup.put(Config.CATEGORY_REMEMBERED, indexItem);
	}

	public IndexItem getRemembered() {
		return wordGroup.get(Config.CATEGORY_REMEMBERED);
	}

	public void addForgotten(IndexItem indexItem) {
		wordGroup.put(Config.CATEGORY_FORGOTTEN, indexItem);
	}

	public IndexItem getForgotten() {
		return wordGroup.get(Config.CATEGORY_FORGOTTEN);
	}
}