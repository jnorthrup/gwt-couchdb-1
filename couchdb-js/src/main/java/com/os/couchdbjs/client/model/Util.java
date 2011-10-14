package com.os.couchdbjs.client.model;

public class Util {
	/**
	 * Null-safe equals implementation
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean equals(Object a, Object b) {
		return null == a && null == b || null != a && a.equals(b);
	}

	/**
	 * Null-safe hashcode implementation. Returns 0 if o is <code>null</code>.
	 * 
	 * @param o
	 * @return
	 */
	public static int safeHashcode(Object o) {
    return null == o ? 0 : o.hashCode();
	}

	public static boolean hasText(String s) {

    if (null == s || 0 == s.length()) {
			return false;
		}
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(' ' < (int) c) {
				return true;
			}
		}
		return false;
	}
}
