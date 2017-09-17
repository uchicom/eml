// (c) 2017 uchicom
package com.uchicom.eml.util;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Base64;


/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class MailUtil {


	public static String getCharset(String contentType) {
		String charset = null;
		if (contentType != null) {
			int charsetStartIndex = contentType.indexOf("charset");
			if (charsetStartIndex >= 0) {
	    		int charsetEndIndex = contentType.indexOf(";", charsetStartIndex);
	    		if (charsetEndIndex > charsetStartIndex) {
	    			charset = contentType.substring(charsetStartIndex + 8, charsetEndIndex).replaceAll("=* *\"", "");
	    		} else {
	    			charset = contentType.substring(charsetStartIndex + 8).replaceAll("=* *\"",  "");
	    		}
			}
		}
		return charset;
	}
	public static String getBoundary(String contentType) {
		String boundary = null;
		//バウンダリー設定
		int boundaryStartIndex = contentType.indexOf("boundary=");
		if (boundaryStartIndex >= 0) {
			int boundaryEndIndex = contentType.indexOf(";", boundaryStartIndex);
			if (boundaryEndIndex > 0) {
				boundary = contentType.substring(boundaryStartIndex + 9, boundaryEndIndex).replaceAll("\"", "");
			} else {
				boundary = contentType.substring(boundaryStartIndex + 9).replaceAll("\"", "");
			}
		}
		System.out.println(boundary);
		System.out.println(contentType);
		return boundary;
	}
	public static String decode64(String value) {

		//ここで文字コード変換する
		int startIndex = value.indexOf("=?");
		int encodingEndIndex = value.indexOf("?", startIndex + 2);
		int typeEndIndex = value.indexOf("?", encodingEndIndex + 1);
		int endIndex = value.indexOf("?=", typeEndIndex + 1);
		if (startIndex >= 0 && endIndex >= startIndex) {
			StringBuffer strBuff = new StringBuffer();
			if (startIndex > 0) {
				strBuff.append(value.substring(0, startIndex));
			}
			while (startIndex >= 0 && endIndex >= startIndex + 2) {
				String base64 = value.substring(startIndex + 2, endIndex);
				String[] splits = base64.split("\\?");
				try {
					if (splits.length  == 3) {
						if ("B".equals(splits[1])) {
							String val = new String(java.util.Base64.getDecoder().decode(splits[2]), splits[0]);
							strBuff.append(val);
						} else if ("Q".equals(splits[1])) {
							String val = new String(decodeQ(splits[2]), splits[0]);
							strBuff.append(val);
						}
					}
				} catch (UnsupportedEncodingException e) {

					e.printStackTrace();
//				} catch (Base64DecodingException e) {
//					e.printStackTrace();
				}
				startIndex = value.indexOf("=?", endIndex);
				encodingEndIndex = value.indexOf("?", startIndex + 2);
				typeEndIndex = value.indexOf("?", encodingEndIndex + 1);
				endIndex = value.indexOf("?=", typeEndIndex + 1);
			}
			return strBuff.toString();
		} else {
			return value;
		}
	}

	public static byte[] decodeQ(String message) {
		System.out.println(message);
		char[] chars = message.toCharArray();
		byte[] bytes = new byte[chars.length];

		int index = 0;
		for (int i = 0; i < chars.length; i++) {
			char ch = chars[i];
			if (ch == '=' && i < chars.length - 2) {
				int val2 = (int)chars[i + 1];
				int val3 = (int)chars[i + 2];
				if ((isNum(val2) || isAF(val2)) && (isNum(val3) || isAF(val3))) {
					if (isNum(val2)) {
						bytes[index] |= (val2 - '0') << 4;
					} else if (isAF(val2)) {
						bytes[index] |= (val2- 'A' + 10) << 4;
					}
					if (isNum(val3)) {
						bytes[index] |= (val3 - '0');
					} else if (isAF(val3)) {
						bytes[index] |= (val3- 'A' + 10);
					}
					i+=2;
					index++;
				} else if (val2 == '\r' && val3 == '\n') {
					//なにもしない
					i+=2;
				} else {
					bytes[index] = (byte) ch;
					index++;
				}
			} else {
				bytes[index] = (byte) ch;
				index++;
			}
		}
		return Arrays.copyOf(bytes, index);
	}
	private static boolean isNum(int val) {
		return val >= '0' && val <= '9';
	}
	private static boolean isAF(int val) {
		return val >= 'A' && val <= 'F';
	}

	/**
	 *
	 * @param encoded
	 * @param contentTransferEncoding
	 * @param charset
	 * @return
	 */
	public static String decode(String encoded, String contentTransferEncoding, String charset) {
		String decoded = null;
		try {
			//文字列変換
			if (charset == null) {
				decoded = encoded;
			} else if (contentTransferEncoding == null || "7bit".equals(contentTransferEncoding.toLowerCase())) {
				decoded = new String(encoded.getBytes(), charset);
			} else if ("quoted-printable".equals(contentTransferEncoding.toLowerCase())) {
				decoded = new String(MailUtil.decodeQ(encoded), charset);
			} else if ("base64".equals(contentTransferEncoding.toLowerCase())) {
				decoded = new String(Base64.getMimeDecoder().decode(encoded), charset);
			} else {
				decoded = new String(encoded.getBytes(), charset);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return decoded;
	}
	public static String decodeHeader(String value) {

		//ここで文字コード変換する
		int startIndex = value.indexOf("=?");
		int encodingEndIndex = value.indexOf("?", startIndex + 2);
		int typeEndIndex = value.indexOf("?", encodingEndIndex + 1);
		int endIndex = value.indexOf("?=", typeEndIndex + 1);
		if (startIndex >= 0 && endIndex >= startIndex) {
			StringBuffer strBuff = new StringBuffer();
			if (startIndex > 0) {
				strBuff.append(value.substring(0, startIndex));
			}
			while (endIndex >= startIndex) {
				String base64 = value.substring(startIndex + 2, endIndex);
//				System.out.println(subject);
//				System.out.println(base64);
				String[] splits = base64.split("\\?");
				try {
					if (splits.length  == 3) {
						if ("B".equals(splits[1])) {
							strBuff.append(new String(Base64.getDecoder().decode(splits[2]), splits[0]));
						} else if ("Q".equals(splits[1])) {
							strBuff.append(new String(MailUtil.decodeQ(splits[2]), splits[0]));
						}
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				startIndex = value.indexOf("=?", endIndex);
				if (startIndex < 0) {
					break;
				}
				encodingEndIndex = value.indexOf("?", startIndex + 2);
				typeEndIndex = value.indexOf("?", encodingEndIndex + 1);
				endIndex = value.indexOf("?=", typeEndIndex + 1);
			}
			if (endIndex >= 0) {
				strBuff.append(value.substring(endIndex + 2));
			}
			return strBuff.toString();
		} else {
			return value;
		}
	}
	public static String getFileName(String contentDisposition) {
		int dispoName = contentDisposition.indexOf("filename=\"");
		if (contentDisposition.indexOf("\"\r\n", dispoName + 10) > dispoName + 10) {
			return MailUtil.decode64(contentDisposition.substring(dispoName + 10, contentDisposition.indexOf("\"\r\n", dispoName + 10)));
		}
		return "";
	}
}
