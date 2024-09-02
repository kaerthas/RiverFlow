package com.inspur.workinfo.api;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.BitSet;


    public class URLEncodeUtils {
        static BitSet dontNeedEncoding = new BitSet(256);

        static {
            int i;
            for(i = 97; i <= 122; ++i) {
                dontNeedEncoding.set(i);
            }

            for(i = 65; i <= 90; ++i) {
                dontNeedEncoding.set(i);
            }

            for(i = 48; i <= 57; ++i) {
                dontNeedEncoding.set(i);
            }

            dontNeedEncoding.set(32);
            dontNeedEncoding.set(45);
            dontNeedEncoding.set(95);
            dontNeedEncoding.set(46);
            dontNeedEncoding.set(42);
            dontNeedEncoding.set(43);
            dontNeedEncoding.set(37);
        }

        public URLEncodeUtils() {
        }

        public static final boolean isURLEncoded(String str) {
            if (str != null && !"".equals(str)) {
                char[] chars = str.toCharArray();
                boolean containsPercent = false;
                char[] var6 = chars;
                int var5 = chars.length;

                for(int var4 = 0; var4 < var5; ++var4) {
                    char c = var6[var4];
                    if (Character.isWhitespace(c)) {
                        return false;
                    }

                    if (!dontNeedEncoding.get(c)) {
                        return false;
                    }

                    if (c == '%') {
                        containsPercent = true;
                    }
                }

                if (!containsPercent) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }

        public static final String encodeURL(String str) {
            try {
                return URLEncoder.encode(str, "utf-8");
            } catch (UnsupportedEncodingException var2) {
                throw new RuntimeException(var2);
            }
        }

        public static final String decodeURL(String str) {
            try {
                return URLDecoder.decode(str, "utf-8");
            } catch (UnsupportedEncodingException var2) {
                throw new RuntimeException(var2);
            }
        }
    }

