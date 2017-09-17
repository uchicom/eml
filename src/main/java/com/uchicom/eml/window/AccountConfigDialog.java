// (c) 2014 uchicom
package com.uchicom.eml.window;

import java.awt.Color;
import java.util.Properties;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import com.uchicom.eml.table.CellRenderer;
import com.uchicom.ui.ResumeDialog;

/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class AccountConfigDialog extends ResumeDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param arg0
	 */
	public AccountConfigDialog(Properties config, String windowKey) {
		super(config, windowKey);
		initComponents();
	}

	private void initComponents() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//		this.addWindowListener(new WindowAdapter() {
//			public void windowClosing(WindowEvent e) {
//				AccountConfigDialog.this.dispose();
//			}
//		});
		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
		TableColumn column = null;
		CellRenderer renderer = new CellRenderer();
		//表示を受信設定と送信設定で分ける。
		//送信時のサーバー設定はあくまで接続先サーバなので、送信内容は別。
		//送信サーバ情報に紐づけておくアカウントはこれ、と決めておく。送信時にサーバを変更することも可能。
		String[] titles = new String[] { "アカウント名", "受信サーバー", "ポート", "ユーザID", "パスワード", "APOP"};
		//送信アカウント名、受信アカウント名を区切り文字で設定して、紐づけておく、あくまでデフォルト値、送信時に変更することは可能。
		//受信アカウントに紐づけるデフォルト送信アカウントをリストで選択できるようにする。
		//送信元情報は、受信アカウント

		//上下で表を分ける。基本は受信アカウントしかいじらないだろう。大量のアカウントがあると便利。
		int[] widths = new int[] { 100, 100, 100, 100, 100 };
		for (int i = 0; i < 5; i++) {
			column = new TableColumn();
			column.setIdentifier(i);
			column.setModelIndex(i);
			column.setCellRenderer(renderer);
			column.setHeaderValue(titles[i]);
			column.setPreferredWidth(widths[i]);
			columnModel.addColumn(column);
		}
		String[][] data = new String[][] { new String[] { "a", "c", "110", "e", "パスワード", "APOP" },
				new String[] { "b", "d", "110", "f", "パスワード", "APOP" } };
		JTable table = new JTable(data, new String[] { "a", "b", "110", "d", "パスワード", "APOP" });
		table.setColumnModel(columnModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setEnabled(false);
		table.setGridColor(Color.WHITE);
		add(new JScrollPane(table));
		this.setTitle("アカウント設定");
	}

}
