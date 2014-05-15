package zhennan.yu.wordreminder;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import zhennan.yu.wordreminder.StartActivity.XMLParsorCallBack;
import android.util.Xml;

public class StackOverflowXmlParser {
	// We don't use namespaces
	private static final String ns = null;
	String TAG = "StackOverflowXmlParser";
	
	public void parse(final InputStream in, final XMLParsorCallBack callBack) throws XmlPullParserException,
			IOException {
		new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					XmlPullParser parser = Xml.newPullParser();
					parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
					parser.setInput(in, null);
					parser.nextTag();
					readContent(parser, callBack);
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					callBack.onComplete();
				}
			}
			
		}.start();
	}

	private void readContent(XmlPullParser parser, XMLParsorCallBack callBack) throws XmlPullParserException,
			IOException {
		parser.require(XmlPullParser.START_TAG, ns, "wordbook");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the item tag
			if (name.equals("item")) {
				callBack.doit(readItem(parser));
			} else {
				skip(parser);
			}
		}
	}

	// Parses the contents of an item. If it encounters a word, trans, or
	// link tag, hands them off
	// to their respective "read" methods for processing. Otherwise, skips the
	// tag.
	private Word readItem(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "item");
		String word = null;
		String meaning = null;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("word")) {
				word = readWord(parser);
			} else if (name.equals("trans")) {
				meaning = readMeaning(parser);
			} else {
				skip(parser);
			}
		}
		return new Word(word, meaning);
	}

	// Processes word tags in the feed.
	private String readWord(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "word");
		String word = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "word");
		return word;
	}

	// Processes trans tags in the feed.
	private String readMeaning(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "trans");
		String trans = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "trans");
		return trans;
	}

	// For the tags word and trans, extracts their text values.
	private String readText(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}
	
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	 }

}