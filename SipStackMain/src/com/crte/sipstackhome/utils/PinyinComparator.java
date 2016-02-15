package com.crte.sipstackhome.utils;

import com.crte.sipstackhome.models.BaseBean;

import java.util.Comparator;

/**
 * 
 * @author xiaanming
 *
 */
public class PinyinComparator implements Comparator<BaseBean> {

	public int compare(BaseBean o1, BaseBean o2) {
		if (o1.sortLetters.equals("@")
				|| o2.sortLetters.equals("#")) {
			return -1;
		} else if (o1.sortLetters.equals("#")
				|| o2.sortLetters.equals("@")) {
			return 1;
		} else {
			return o1.sortLetters.compareTo(o2.sortLetters);
		}
	}

}
