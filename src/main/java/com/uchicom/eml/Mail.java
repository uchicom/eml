// (c) 2014 uchicom
package com.uchicom.eml;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * メールに関するクラス.
 */
public class Mail {

	private File file;
	private String subject;
	private String body;
	private Date date;
	private String charset;
	private String encoding;
	private String contentType;
	private String boundary;
	private String from;
	private String to;
	private List<String> tempList;
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = decodeHeader(from);
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = decodeHeader(to);
	}



	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.charset = getCharset(contentType);
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
		this.contentType = contentType;
	}
	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset.replaceAll("\"","");
	}
	public String getSubject() {
		if (tempList != null && tempList.size() > 0) {
			return subject + tempList;
		} else {
			return subject;
		}
	}
	public void setSubject(String subject) {
		this.subject = decode(subject);
	}
	public static String decode(String value) {

		//ここで文字コード変換する
		int startIndex = value.indexOf("=?");
		int encodingEndIndex = value.indexOf("?", startIndex + 2);
		int typeEndIndex = value.indexOf("?", encodingEndIndex + 1);
		int endIndex = value.indexOf("?=", startIndex + 1);
		if (startIndex >= 0 && endIndex >= startIndex) {
			StringBuffer strBuff = new StringBuffer();
			if (startIndex > 0) {
				strBuff.append(value.substring(0, startIndex));
			}
			while (startIndex >= 0 && endIndex >= startIndex + 2) {
//				System.out.println(startIndex);
				String base64 = value.substring(startIndex + 2, endIndex);
//				System.out.println(subject);
//				System.out.println(base64);
				String[] splits = base64.split("\\?");
				try {
					if (splits.length  == 3) {
						if ("B".equals(splits[1])) {
//							System.out.println("test2");
							String val = new String(java.util.Base64.getDecoder().decode(splits[2]), splits[0]);
//							System.out.println(val);
//							System.out.println(val.length());
							strBuff.append(val);
						} else if ("Q".equals(splits[1])) {
//							System.out.println("test");
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
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		if (body == null || "".equals(body)) {
			this.body = null;
			return;
		}
//		System.out.println(charset + ":" + encoding);
//		System.err.println(contentType);
		if (boundary != null) {

			StringBuffer bodyBuff = new StringBuffer(body.length());
			int bodyBoundaryStartIndex = body.indexOf("--" + boundary + "\r\n");
			int headStartIndex = bodyBoundaryStartIndex + boundary.length() + 4;
			int headEndIndex = body.indexOf("\r\n\r\n", headStartIndex);
			String[] heads = body.substring(headStartIndex, headEndIndex).split("\r\n");
			StringBuffer bHeadBuff = new StringBuffer(100);
			int status = 0;
			String bContentType = null;
			String bContentTransferEncoding = null;
			String bContentDisposition = null;
			for (String prop : heads) {
				if (prop.startsWith("Content-Type")) {
					switch(status) {
					case 0:
						break;
					case 1:
						bContentType = bHeadBuff.toString();
						break;
					case 2:
						bContentTransferEncoding = bHeadBuff.toString();
						break;
					case 3:
						bContentDisposition = bHeadBuff.toString();
						break;
					}
					status = 1;
					bHeadBuff.setLength(0);
            		bHeadBuff.append(prop);
				} else if (prop.startsWith("Content-Transfer-Encoding")) {
					switch(status) {
					case 0:
						break;
					case 1:
						bContentType = bHeadBuff.toString();
						break;
					case 2:
						bContentTransferEncoding = bHeadBuff.toString();
						break;
					case 3:
						bContentDisposition = bHeadBuff.toString();
						break;
					}
					status = 2;
					bHeadBuff.setLength(0);
            		bHeadBuff.append(prop.substring(27));
				} else if (prop.startsWith("Content-Disposition")) {
					switch(status) {
					case 0:
						break;
					case 1:
						bContentType = bHeadBuff.toString();
						break;
					case 2:
						bContentTransferEncoding = bHeadBuff.toString();
						break;
					case 3:
						bContentDisposition = bHeadBuff.toString();
						break;
					}
					status = 3;
					bHeadBuff.setLength(0);
            		bHeadBuff.append(prop);
            	} else if (prop.startsWith(" ") || prop.startsWith("\t")) {
            		bHeadBuff.append(prop);
				}
			}
			switch(status) {
			case 0:
				break;
			case 1:
				bContentType = bHeadBuff.toString();
				break;
			case 2:
				bContentTransferEncoding = bHeadBuff.toString();
				break;
			case 3:
				bContentDisposition = bHeadBuff.toString();
				break;
			}
//			System.out.println(bContentTransferEncoding);
			String bCharset = getCharset(bContentType);
			int bEnd = body.indexOf("--" + boundary , headEndIndex);
//			System.out.println(bEnd);
			if (bEnd >= 0) {
				String val = body.substring(headEndIndex + 4, bEnd);
				bodyBuff.append(val);
			}
			//TODO
			//以降は添付ファイル(multipart/alternativeとかがあるので繰り返しで処理しなきゃダメ。
			//仕組みとしては、バウンダリのなかをきちんと調査する)
			int lastIndex = bEnd;
			int searchIndex = lastIndex;
			while ((searchIndex = body.indexOf("--" + boundary , lastIndex + 1)) > lastIndex) {
				if (tempList == null) {
					tempList = new ArrayList<>();
				}
				int headEnd = body.indexOf("\r\n\r\n", lastIndex + 1);
				if (headEnd > lastIndex + 1 && headEnd < searchIndex) {
					int dispo = body.indexOf("Content-Disposition", lastIndex + 1);
					if (dispo > lastIndex + 1 && headEnd > dispo) {
						int dispoName = body.indexOf("filename=\"", lastIndex + 1);
						if (dispoName + 10 < headEnd  && body.indexOf("\"\r\n", dispoName + 10) > dispoName + 10) {
							tempList.add(decode(body.substring(dispoName + 10, body.indexOf("\"\r\n", dispoName + 10))));
						}

					}
				}
				lastIndex = searchIndex;
			}
//			System.out.println(tempList);


//			System.out.println(heads.length + ":" + body.substring(headStartIndex, headEndIndex));

			if (bCharset == null) {
				this.body = bodyBuff.toString();
			} else if (bContentTransferEncoding == null || "7bit".equals(bContentTransferEncoding.toLowerCase())) {
				try {
					this.body = new String(bodyBuff.toString().getBytes(), bCharset);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if ("quoted-printable".equals(bContentTransferEncoding.toLowerCase())) {
				try {

					this.body = new String(decodeQ(bodyBuff.toString()), bCharset);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if ("base64".equals(bContentTransferEncoding.toLowerCase())) {
				try {

					this.body = new String(Base64.decode(bodyBuff.toString()), bCharset);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Base64DecodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					this.body = new String(bodyBuff.toString().getBytes(), bCharset);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else if (charset == null) {
				this.body = body;
		} else if (encoding == null) {
			try {
				this.body = new String(body.getBytes("ISO_8859-1"), charset);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if ("7bit".equals(encoding.toLowerCase())) {

			try {
				this.body = new String(body.getBytes("ISO_8859-1"), charset);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			try {
//				if ("ISO-2022-JP".equals(charset.toUpperCase())) {
//					StringBuffer isoBuff = new StringBuffer();
//					System.out.println("a");
//					int isoStartIndex = body.indexOf(new String(new char[]{0x14, 0x24, 0x42}));
//					if (isoStartIndex >= 0) {
//						System.out.println("b");
//						if (isoStartIndex > 0) {
//							System.out.println("c2");
//							isoBuff.append(body.substring(0, isoStartIndex));
//						}
//						int isoEndIndex = body.indexOf(new String(new char[]{0x14, 0x28, 0x42}), isoStartIndex);
//						if (isoEndIndex >= 0) {
//							System.out.println("c");
//						while (isoStartIndex < isoEndIndex) {
//							System.out.println("d:" + isoStartIndex + "," + isoEndIndex);
//							isoBuff.append(new String(body.substring(isoStartIndex, isoEndIndex + 3).getBytes("ISO_8859-1"), charset));
//							isoStartIndex = body.indexOf("$B", isoEndIndex);
//							if (isoStartIndex < 0) {
//								System.out.println("e");
//								isoBuff.append(body.substring(isoEndIndex));
//								break;
//							}
//							isoEndIndex = body.indexOf("(B", isoStartIndex);
//						}
//
//						this.body = isoBuff.toString();
//						} else {
//							System.out.println("f");
//							this.body = new String(body.getBytes("ISO_8859-1"), charset);
//						}
//					} else {
//						System.out.println("g");
//						this.body = new String(body.getBytes("ISO_8859-1"), charset);
//					}
//				} else {
//
//					this.body = new String(body.getBytes("ISO_8859-1"), charset);
//				}
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				System.err.println(contentType);
//				System.err.println("---");
//				System.err.println(body);
//				e.printStackTrace();
//			}
		} else if ("quoted-printable".equals(encoding.toLowerCase())) {

			try {

				this.body = new String(decodeQ(body), charset);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if ("base64".equals(encoding.toLowerCase())) {
			try {

				this.body = new String(Base64.decode(body), charset);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Base64DecodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}

	public static byte[] decodeQ(String message) {
//		System.out.println(message);
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

	private String getCharset(String contentType) {
		System.out.println(contentType);
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
		System.out.println("charset:" +charset);
		return charset;
	}
	private String decodeHeader(String value) {

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
//							System.out.println("test2");
							strBuff.append(new String(Base64.decode(splits[2]), splits[0]));
						} else if ("Q".equals(splits[1])) {
//							System.out.println("test");
							strBuff.append(new String(decodeQ(splits[2]), splits[0]));
						}
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (Base64DecodingException e) {
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
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public List<String> getTempList() {
		return tempList;
	}
	public void setTempList(List<String> tempList) {
		this.tempList = tempList;
	}
}
