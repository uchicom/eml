// (c) 2014 uchicom
package com.uchicom.eml;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.uchicom.eml.entity.Content;
import com.uchicom.eml.util.MailUtil;

/**
 * メールに関するクラス.
 */
public class Mail {

	public static SimpleDateFormat format = new SimpleDateFormat(
			"EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
	public static SimpleDateFormat format2 = new SimpleDateFormat("d MMM yyyy HH:mm:ss Z",
			Locale.ENGLISH);
	private static final int NONE = 0;
	private static final int SUBJECT = 1;
	private static final int BODY = 2;
	private static final int CONTENT_TYPE = 3;
	private static final int CONTENT_TRANSFER_ENCODING = 4;
	private static final int OTHER = 99;// とりあえずその他

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
	private String temps;
	private List<Content> contentList;
	private int tempCount;
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = MailUtil.decodeHeader(from);
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = MailUtil.decodeHeader(to);
	}



	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.charset = MailUtil.getCharset(contentType);
		//バウンダリー設定
		this.boundary = MailUtil.getBoundary(contentType);
		this.contentType = contentType;
		System.out.println(boundary);
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
		if (contentList != null && contentList.size() > 0) {
			return subject + "[" + temps + "]";
		} else {
			return subject;
		}
	}
	public void setSubject(String subject) {
		try {
			if (subject.indexOf("=?") >= 0) {
				this.subject = MailUtil.decode64(subject);
			} else {
				this.subject = MailUtil.decode(subject, encoding, charset);
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.subject = subject;
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
			contentList = Content.createList(body, boundary);
			StringBuffer strBuff = new StringBuffer(1024);
			StringBuffer tempBuff = new StringBuffer(1024);
			if (contentList != null && contentList.size() > 0) {
				contentList.forEach(content->{
					strBuff.append(content.getBody());
					if (tempBuff.length() > 0) {
						tempBuff.append(",");
					}
					tempBuff.append(content.toString());
					tempCount += content.getTempCount();
				});
			}
			temps = tempBuff.toString();
			this.body = strBuff.toString();
		} else {
			this.body = MailUtil.decode(body, encoding, charset);
		}

	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}

	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	/**
	 * tempCountを取得します.
	 *
	 * @return tempCount
	 */
	public int getTempCount() {
		return tempCount;
	}
	/**
	 * tempCountを設定します.
	 *
	 * @param tempCount tempCount
	 */
	public void setTempCount(int tempCount) {
		this.tempCount = tempCount;
	}

	public static Mail analyze(BufferedReader br, OutputStream os, boolean retentionBody)
			throws IOException {

		long start = System.currentTimeMillis();
		long now = 0;
		String line = null;
		boolean isBody = false;
		StringBuffer bodyBuff = new StringBuffer();
		StringBuffer contentTypeBuff = new StringBuffer();
		StringBuffer contentTransferEncodingBuff = new StringBuffer();
		Mail mail = new Mail();
		int status = NONE;
		StringBuffer subjectBuff = new StringBuffer();
		int count=0;
		while ((line = br.readLine()) != null && !".".equals(line)) {
			if (retentionBody && count < 50) {
				System.out.println(line);count++;
			}
			if (!isBody && "".equals(line)) {
				if (os == null && !retentionBody) {
					break;
				}
				isBody = true;
				status = BODY;
			} else if (isBody) {
				if (retentionBody) {
//					if (count < 10) {System.out.println(line);count++;}
					if (line.length() > 0 && line.charAt(0) == '.') {
						bodyBuff.append(line.substring(1));
					} else {
						bodyBuff.append(line);
					}
					bodyBuff.append("\r\n");
				}
			} else if (line.matches("[Ff]rom\\:.*")) {
				status = OTHER;
				mail.setFrom(line.substring(5).trim());
			} else if (line.matches("[tT]o\\:.*")) {
				status = OTHER;
				mail.setTo(line.substring(3).trim());
			} else if (line.matches("[dD]ate\\:.*")) {
				status = OTHER;
				int comma = line.indexOf(',');
				if (comma >= 0) {
					try {
						mail.setDate(format.parse(line.substring(5).trim()));
					} catch (ParseException e) {
						System.err.println(e.getMessage());
					}
				} else {

					try {
						mail.setDate(format2.parse(line.substring(6)));
					} catch (ParseException e) {
						System.err.println(e.getMessage());
					}
				}
			} else if (line.matches("[Cc]ontent-[tT]ype\\:.*")) {
				contentTypeBuff.append(line.substring(12).trim());
				status = CONTENT_TYPE;
				// boundary = line.substring(line.indexOf("boundary=") + 9);
				// System.out.println(boundary);
			} else if (line.matches("[Cc]ontent-[Tt]ransfer-[Ee]ncoding\\:.*")) {
				// System.out.println(i + ":" +line);
				contentTransferEncodingBuff.append(line.substring(26).trim());
				status = CONTENT_TRANSFER_ENCODING;
			} else if (line.matches("[Ss]ubject\\:.*")) {
				status = SUBJECT;
				if (line.length() > 7) {
					subjectBuff.append(line.substring(8).trim());
				}
			} else if (line.startsWith(" ") || line.startsWith("\t")) {
				switch (status) {
				case SUBJECT:
					subjectBuff.append(line.substring(1));
					break;
				case CONTENT_TYPE:
					contentTypeBuff.append(line.substring(1));
					break;
				case CONTENT_TRANSFER_ENCODING:
					contentTransferEncodingBuff.append(line.substring(1));
					break;
				}
			} else {
				status = OTHER;
			}
			if (os != null) {
				os.write(line.getBytes());
				os.write("\r\n".getBytes());
			}
		}

		now = System.currentTimeMillis();
		System.out.println("analyze:" + (now - start) + "[ms]");
		start = System.currentTimeMillis();

		mail.setContentType(contentTypeBuff.toString());
		mail.setEncoding(contentTransferEncodingBuff.toString());
		mail.setSubject(subjectBuff.toString());
		mail.setBody(bodyBuff.toString());

		// 終了
		return mail;
	}
}
