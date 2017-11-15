package cn.edu.pku.hongbenyun.util;

import com.github.promeg.pinyinhelper.Pinyin;

/**
 * Created by Mike_Hong on 2017/11/15.
 */

public class ChineseToPinyin {
    public static String toPinyin(String str)
    {
        String pinyin="";
        char[] charArray = str.toCharArray();
        for (int i = 0; i < charArray.length; i++)
        {
            pinyin+= Pinyin.toPinyin(charArray[i]);
        }
        return pinyin.toLowerCase();
    }
}
