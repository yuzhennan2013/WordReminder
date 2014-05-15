package zhennan.yu.wordreminder;

public class Word implements Comparable<Word>{
	public String word, meaning;
	public short difficulty; 
	boolean expanded;
	public long created_time;
	public long last_rem_time;
	public long last_test_time;
	public short last_test_result;
	public short removed;
	public short groupid;
	
	public Word(){}
	public Word(String word, String meaning){
		this.word = word;
		this.meaning = meaning;
	}
	
	public Word(String word, String meaning, short difficulty){
		this.word = word;
		this.meaning = meaning;
		this.difficulty = difficulty;
	}
	@Override
	public int compareTo(Word another) {
		// TODO Auto-generated method stub
		return word.compareTo(another.word);
	}
}
