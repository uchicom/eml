// (c) 2014 uchicom
package com.uchicom.eml;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.table.DefaultTableModel;

public class MailTableModel extends DefaultTableModel {

	private List<Mail> rowList;
	public MailTableModel(List<Mail> rowList, int columnCount) {
		super(rowList.size(), columnCount);
		this.rowList = rowList;
	}
	Format format = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	public Object getValueAt(int row, int col) {
		Mail mail = rowList.get(row);
		String val = null;
		switch (col) {
		case 0:
			val = String.valueOf(row + 1);
			break;
		case 1:
			val = mail.getFrom();
			break;
		case 2:
			if (mail.getDate() != null) {
				val = format.format(mail.getDate());
			} else {
				val = "----/--/-- --:--";
			}
			break;
		case 3:
			val = mail.getSubject();
			break;
		case 4:
			if (mail.getTempList() != null) {
				val = String.valueOf(mail.getTempList().size());
			} else {
				val = "";
			}
			break;
//		case 4:
//			val = mail.getTo();
//			break;
//		case 5:
//			val = mail.getBody();
//			break;
		default:
			val = "";
		}

		return val;
	}

	public void addList(List<Mail> mailList) {
		int rowSize = rowList.size();
		int addSize = mailList.size();
		this.setRowCount(rowSize + addSize);
		this.rowList.addAll(mailList);
		this.fireTableDataChanged();
		this.fireTableRowsInserted(rowSize, rowSize + addSize);
	}
	public void addRow(Mail mail) {
		int size = rowList.size();
		rowList.add(mail);
		this.setRowCount(size + 1);
		this.fireTableDataChanged();
		this.fireTableRowsInserted(size, size);
	}
	/**
	 * 指定行のMailを取得する.
	 */
	public Mail getRow(int row) {
		return rowList.get(row);
	}
}
