package inspiracio.widget;

import android.text.Editable;
import android.text.InputFilter;

public class DirectEditable implements Editable{

	@Override public char charAt(int index) {
		return 0;
	}

	@Override public int length() {
		return 0;
	}

	@Override public CharSequence subSequence(int start, int end) {
		return null;
	}

	@Override public void getChars(int start, int end, char[] dest, int destoff) {
	}

	@Override public void removeSpan(Object what) {
	}

	@Override public void setSpan(Object what, int start, int end, int flags) {
	}

	@Override public int getSpanEnd(Object tag) {
		return 0;
	}

	@Override public int getSpanFlags(Object tag) {
		return 0;
	}

	@Override public int getSpanStart(Object tag) {
		return 0;
	}

	@Override public <T> T[] getSpans(int arg0, int arg1, Class<T> arg2) {
		return null;
	}

	@Override public int nextSpanTransition(int start, int limit, Class type) {
		return 0;
	}

	@Override public Editable append(CharSequence text) {
		return null;
	}

	@Override public Editable append(char text) {
		return null;
	}

	@Override public Editable append(CharSequence text, int start, int end) {
		return null;
	}

	@Override public void clear() {
	}

	@Override public void clearSpans() {
	}

	@Override public Editable delete(int st, int en) {
		return null;
	}

	@Override public InputFilter[] getFilters() {
		return null;
	}

	@Override public Editable insert(int where, CharSequence text) {
		return null;
	}

	@Override public Editable insert(int where, CharSequence text, int start, int end) {
		return null;
	}

	@Override public Editable replace(int st, int en, CharSequence text) {
		return null;
	}

	@Override public Editable replace(int st, int en, CharSequence source, int start, int end) {
		return null;
	}

	@Override public void setFilters(InputFilter[] filters) {
	}

}