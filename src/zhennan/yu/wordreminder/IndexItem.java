package zhennan.yu.wordreminder;

import java.io.Serializable;
/**
 * @author Administrator class that describe the structure of start activity
 *         item
 * Serializable class cannot be inner class,
 * do not put this class in StartActivity.java
 */
public class IndexItem implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6958076529554120357L;
	/**
	 * 
	 */

	char initialchar;
	short groupid;
	// 4 categories: untested, forgotten, remembered, random test
	String category;
	short count;

	@Override
	public String toString() {
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(initialchar);
		sBuilder.append(groupid);
<<<<<<< HEAD
		sBuilder.append("(");
		sBuilder.append(category);
		sBuilder.append(":");
		sBuilder.append(count);
		sBuilder.append(")");
=======
		sBuilder.append("( ");
		sBuilder.append(category);
		sBuilder.append(" : ");
		sBuilder.append(count);
		sBuilder.append(" )");
>>>>>>> fc3f929bc6fcf86a3f48d5eacf20bf4afa4e3e85
		return sBuilder.toString();
	}
}
