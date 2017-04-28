// (c) 2014 uchicom
package com.uchicom.eml;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class AccountConfigDialog extends Dialog {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param arg0
     */
    public AccountConfigDialog(Frame arg0) {
	super(arg0,true);
	initComponents();

    }

    private void initComponents() {
	this.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
	       AccountConfigDialog.this.dispose();
	    }
	});
	DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
	TableColumn column = null;
	CellRenderer renderer = new CellRenderer();
	String[] titles = new String[]{"アカウント名", "ドメイン名", "ポート", "ユーザID","パスワード",  "APOP"};
	int[] widths = new int[]{100, 100, 100, 100, 100};
	for (int i = 0; i < 5; i++) {
		column = new TableColumn();
		column.setIdentifier(i);
		column.setModelIndex(i);
		column.setCellRenderer(renderer);
		column.setHeaderValue(titles[i]);
		column.setPreferredWidth(widths[i]);
		columnModel.addColumn(column);
	}
	String[][] data = new String[][] {
		new String[]{"a", "c", "110", "e","パスワード",  "APOP"},
		new String[]{"b", "d", "110", "f","パスワード",  "APOP"}
	};
	JTable table = new JTable(data, new String[]{"a", "b", "110", "d","パスワード",  "APOP"});
	table.setColumnModel(columnModel);
	table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	table.setEnabled(false);
	table.setGridColor(Color.WHITE);
	add(new JScrollPane(table));
	this.setTitle("アカウント設定");
    }


}
