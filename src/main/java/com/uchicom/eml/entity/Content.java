// (c) 2017 uchicom
package com.uchicom.eml.entity;

import java.util.ArrayList;
import java.util.List;

import com.uchicom.eml.util.MailUtil;

/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class Content {

	private String contentType;
	private String contentTransferEncoding;
	private String contentDisposition;
	private String contentDescription;
	private String body;
	private String boundary;
	private String fileName;
	private List<Content> contentList;
	/**
	 * contentListを取得します.
	 *
	 * @return contentList
	 */
	public List<Content> getContentList() {
		return contentList;
	}

	public Content(String content) {
		analyze(content);
	}

	public static List<Content> createList(String body, String boundary) {
		List<Content> contentList = new ArrayList<>();
		String boundaryLine = "--" + boundary + "\r\n";
		int start = -1;
		int current = -1;
		while ((current = body.indexOf(boundaryLine, current + 1)) >= 0) {
			if (start >= 0) {
				Content content = new Content(body.substring(start, current));
				contentList.add(content);
			}
			start = current + boundaryLine.length();
		}
		return contentList;
	}
	private void analyze(String content) {
//		int headStartIndex = bodyBoundaryStartIndex + boundary.length() + 4;
		int headEndIndex = content.indexOf("\r\n\r\n", 0);
		String[] heads = content.substring(0, headEndIndex).split("\r\n");
		StringBuffer bHeadBuff = new StringBuffer(100);
		int status = 0;
		for (String prop : heads) {
			if (prop.startsWith("Content-Type")) {
				setHeader(status, bHeadBuff.toString());
				status = 1;
				bHeadBuff.setLength(0);
        		bHeadBuff.append(prop);
			} else if (prop.startsWith("Content-Transfer-Encoding")) {
				setHeader(status, bHeadBuff.toString());
				status = 2;
				bHeadBuff.setLength(0);
        		bHeadBuff.append(prop.substring(27));
			} else if (prop.matches("[Cc]ontent-[Dd]isposition\\: .*")) {
				setHeader(status, bHeadBuff.toString());
				status = 3;
				bHeadBuff.setLength(0);
        		bHeadBuff.append(prop);
        	} else if (prop.startsWith(" ") || prop.startsWith("\t")) {
        		bHeadBuff.append(prop);
			}
		}
		setHeader(status, bHeadBuff.toString());
		String bCharset = MailUtil.getCharset(contentType);
		if (contentType != null) {
			boundary = MailUtil.getBoundary(contentType);
		}
		if (contentDisposition != null) {
			fileName = MailUtil.getFileName(contentDisposition);
		}
		if (boundary != null) {
			contentList = createList(content.substring(headEndIndex + 4), boundary);
			body = createBody(contentList);
		} else {
			body = MailUtil.decode(content.substring(headEndIndex + 4), contentTransferEncoding, bCharset);
		}
	}

	private void setHeader(int status, String value) {
		switch(status) {
		case 0:
			break;
		case 1:
			contentType = value;
			break;
		case 2:
			contentTransferEncoding = value;
			break;
		case 3:
			contentDisposition = value;
			break;
		}
	}

	/**
	 * contentTypeを取得します.
	 *
	 * @return contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * contentTypeを設定します.
	 *
	 * @param contentType contentType
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * contentTransferEncodingを取得します.
	 *
	 * @return contentTransferEncoding
	 */
	public String getContentTransferEncoding() {
		return contentTransferEncoding;
	}

	/**
	 * contentTransferEncodingを設定します.
	 *
	 * @param contentTransferEncoding contentTransferEncoding
	 */
	public void setContentTransferEncoding(String contentTransferEncoding) {
		this.contentTransferEncoding = contentTransferEncoding;
	}

	/**
	 * contentDispositionを取得します.
	 *
	 * @return contentDisposition
	 */
	public String getContentDisposition() {
		return contentDisposition;
	}

	/**
	 * contentDispositionを設定します.
	 *
	 * @param contentDisposition contentDisposition
	 */
	public void setContentDisposition(String contentDisposition) {
		this.contentDisposition = contentDisposition;
	}

	/**
	 * contentDescriptionを取得します.
	 *
	 * @return contentDescription
	 */
	public String getContentDescription() {
		return contentDescription;
	}

	/**
	 * contentDescriptionを設定します.
	 *
	 * @param contentDescription contentDescription
	 */
	public void setContentDescription(String contentDescription) {
		this.contentDescription = contentDescription;
	}

	/**
	 * bodyを取得します.
	 *
	 * @return body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * bodyを設定します.
	 *
	 * @param body body
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * boundaryを取得します.
	 *
	 * @return boundary
	 */
	public String getBoundary() {
		return boundary;
	}

	/**
	 * boundaryを設定します.
	 *
	 * @param boundary boundary
	 */
	public void setBoundary(String boundary) {
		this.boundary = boundary;
	}

	/**
	 * contentListを設定します.
	 *
	 * @param contentList contentList
	 */
	public void setContentList(List<Content> contentList) {
		this.contentList = contentList;
	}

	public String createBody(List<Content> contentList) {
		if (contentList != null && contentList.size() > 0) {
			StringBuffer strBuff = new StringBuffer(1024);
			contentList.forEach(content->{
				strBuff.append(content.getBody());
			});
			return strBuff.toString();
		} else {
			return "";
		}
	}

	public String toString() {
		if (contentDisposition != null) {
			return contentDisposition;
		}
		return "";
	}
	public int getTempCount() {
		if (contentList == null || contentList.size() == 0) {
			if (contentDisposition == null) {
				return 0;
			} else {
				return 1;
			}
		} else {
			int tempCount = 0;
			for (Content content : contentList) {
				tempCount += content.getTempCount();
			}
			return tempCount;
		}
	}
}
