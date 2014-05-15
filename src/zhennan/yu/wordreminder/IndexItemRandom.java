package zhennan.yu.wordreminder;


class IndexItemRandom extends IndexItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(category);
		sBuilder.append(" : ");
		sBuilder.append(count);
		return sBuilder.toString();
	}

}
