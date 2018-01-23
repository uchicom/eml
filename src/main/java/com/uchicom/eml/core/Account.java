// (c) 2016 uchicom
package com.uchicom.eml.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.uchicom.eml.Constants;
import com.uchicom.eml.entity.ServerInfo;
import com.uchicom.eml.table.CellRenderer;
import com.uchicom.eml.table.MailTableModel;
import com.uchicom.eml.util.Pop3Util;
import com.uchicom.eml.util.ResourceUtil;
import com.uchicom.eml.window.MailFrame;

/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class Account {


	private MailTableModel model = new MailTableModel(new ArrayList<>(), 5);
	private JTable table;

	private JProgressBar progressBar = new JProgressBar();
	private String name;
	private String user;
	private String password;
	private String path;

	private ServerInfo receive;
	private ServerInfo send;

	private JLabel statusLabel = new JLabel();
	private Map<String, String> uidlMap = new HashMap<String, String>();

	public Account(String name, String user, String password, String path, ServerInfo receive, ServerInfo send) {
		this.name = name;
		this.user = user;
		this.password = password;
		this.path = path;
		if (path != null) {
			File file = new File(path);
			if (!file.exists()) {
				file.mkdirs();
			}
		}
		this.receive = receive;
		this.send = send;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * userを取得します.
	 *
	 * @return user
	 */
	public String getUser() {
		return user;
	}
	/**
	 * userを設定します.
	 *
	 * @param user user
	 */
	public void setUser(String user) {
		this.user = user;
	}
	/**
	 * passwordを取得します.
	 *
	 * @return password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * passwordを設定します.
	 *
	 * @param password password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	@SuppressWarnings("unchecked")
	public void loadUidlMap() {
		File uidlFile = new File(path, "uidl.map");
		if (uidlFile.exists()) {
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(uidlFile));){
				uidlMap = (Map<String, String>)ois.readObject();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	public void saveUidlMap() {

		File uidlFile = new File(path, Constants.UIDLMAP);
		ObjectOutputStream oos = null;
		try {
			if (!uidlFile.exists()) {
				uidlFile.createNewFile();
			}
			oos = new ObjectOutputStream(new FileOutputStream(uidlFile));
			oos.writeObject(uidlMap);
			oos.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public void loadMail() {
		File mailboxFile = new File(path, Constants.MAILBOX);
		if (mailboxFile.exists()) {
			File[] mails = mailboxFile.listFiles(new FileFilter() {

				@Override
				public boolean accept(File file) {
					return file.isFile() && !file.isHidden() && file.getName().endsWith(".gze");
				}

			});
			for (File file : mails) {
//				Thread thread = new Thread(()->{
				try (GZIPInputStream inputStream = new GZIPInputStream(new FileInputStream(file));){
//					mailList.add(analyze(new BufferedReader(
//							new InputStreamReader(new FileInputStream(mail))), null));
					Mail mail = Mail.analyze(new BufferedReader(
							new InputStreamReader(inputStream)), null, false);
					mail.setBody(null);
					mail.setFile(file);
					this.model.addRow(mail);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
//				});
//				thread.setDaemon(true);
//				thread.start();
			}
			System.out.println("mail:" + model.getRowCount());
//			this.model.addList(mailList);
		}
	}



	/**
	 * USER ユーザID設定 PASS パスワード設定 またはAPOPを指定 ログイン後 STAT 最大のメールインデックスを取得 UIDL
	 * で最大のユニークIDを取得し、保存しているUIDLに含まれていなければ検索する,含まれていれば検索しない UIDL
	 * で全体のユニークIDを取得し、含まれていないメールインデックスを抽出 RETR または TOP でメール情報を取得
	 *
	 * @param args
	 * @return
	 */
	public void downloadAllMail() {

		statusLabel.setText("接続");
		progressBar.setVisible(true);
		long start1 = System.currentTimeMillis();
		System.out.println("開始");


		long start = System.currentTimeMillis();
		long now = 0;
		try (Socket socket = new Socket();) {

			progressBar.setStringPainted(true);//%表示

			progressBar.setMinimum(0);
			progressBar.setMaximum(100);
			socket.connect(new InetSocketAddress(receive.getHost(), receive.getPort()));
			now = System.currentTimeMillis();
			System.out.println("connect:" + (now - start) + "[ms]");
			start = System.currentTimeMillis();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			PrintStream ps = new PrintStream(socket.getOutputStream());
			String line = br.readLine();
			System.out.println("connect:" + line);
			if (!Pop3Util.login(br, ps, user, password)) {
				Pop3Util.quit(br, ps);
			}

			statusLabel.setText("LOGIN");
			progressBar.setValue(1);
			now = System.currentTimeMillis();
			System.out.println("login:" + (now - start) + "[ms]");
			start = System.currentTimeMillis();

			int maxIndex = Pop3Util.stat(br, ps);
			if (maxIndex < 1) {
				Pop3Util.quit(br, ps);
			}

			statusLabel.setText("STAT");
			progressBar.setValue(2);
			now = System.currentTimeMillis();
			System.out.println("stat:" + (now - start) + "[ms]");
			start = System.currentTimeMillis();

			statusLabel.setText("新着チェック");
			progressBar.setValue(3);
			String id = Pop3Util.uidl(br, ps, maxIndex);
			if (id == null) {
				Pop3Util.quit(br, ps);
			} else {
				if (uidlMap.containsKey(id)) {
					// 新しいメールはない。
					return;
				}
			}

			now = System.currentTimeMillis();
			System.out.println("uidl:" + (now - start) + "[ms]");
			start = System.currentTimeMillis();
			progressBar.setValue(4);
			statusLabel.setText("UIDL");

			List<String> idList = Pop3Util.uidl(br, ps);
			List<String> retrList = new ArrayList<String>();
			for (String uidl : idList) {
				if (!uidlMap.containsKey(uidl)) {
					retrList.add(uidl);
				}
			}
			if (retrList.size() <= 0) {
				// 新しいメールはない。
				System.out.println("retrList new mail 0.");

				progressBar.setValue(100);
				statusLabel.setText("QUIT");
				Pop3Util.quit(br, ps);
				return;
			}
			File mailboxFile = new File(path, "mailbox");
			if (!mailboxFile.exists()) {
				mailboxFile.mkdirs();
			}

			char[] chars = new char[1024];
			int iMaxList = retrList.size();
			String hexTime = Long.toHexString(System.currentTimeMillis());
			for (int i = 0; i < iMaxList; i++) {

				progressBar.setValue(4 + (i+1) * 96 / iMaxList);
				String uidl = retrList.get(i);
				String name = createName(uidl);
				uidlMap.put(uidl, name);
				now = System.currentTimeMillis();
				System.out.println((i + 1) + "/" + iMaxList + ":"
						+ (now - start) + "[ms]");
				start = System.currentTimeMillis();

//				 if (i > 42) break;

					statusLabel.setText("RETR" + (i + 1) );
				ps.print("RETR " + (i + 1) + "\r\n");

				System.out.println("RETR:" + br.readLine());
				// ps.print("TOP " + (i + 1)+ " 0\r\n");
				ps.flush();
				File file = new File(mailboxFile, name);
				try (GZIPOutputStream pw = new GZIPOutputStream(new FileOutputStream(file));) {
					Mail mail = Mail.analyze(br, pw, true);
	//				mail.getTempList();
					mail.setBody(null);
					mail.setFile(file);
					this.model.addRow(mail);
					pw.flush();
				}
			}
			progressBar.setValue(100);
			statusLabel.setText("QUIT");
			Pop3Util.quit(br, ps);
//			 br.close();
//			 ps.close();
			progressBar.setVisible(false);
			statusLabel.setText("");

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("終了:" + (System.currentTimeMillis() - start1) / 1000d);
	}

	public JPanel dispList() {
		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
		TableColumn column = null;
		CellRenderer renderer = new CellRenderer();
		String[] titles = ResourceUtil.getStrings(Constants.TABLE_TITLES);
		int[] widths = new int[] { 50, 200, 100, 400, 50 };

		JTextField textField = new JTextField();
		textField.setEditable(false);
		TableCellEditor selectableEditor = new DefaultCellEditor(textField) ;
		TableCellEditor viewEditor = new DefaultCellEditor(new JTextField()) {
			public boolean isCellEditable(EventObject anEvent) {
				return false;
			}
		};
		for (int i = 0; i < titles.length; i++) {
			column = new TableColumn();
			column.setIdentifier(i);
			column.setModelIndex(i);
			column.setCellRenderer(renderer);
			column.setHeaderValue(titles[i]);
			column.setPreferredWidth(widths[i]);
			if (i == 0) {
				column.setCellEditor(viewEditor);
			} else {
				column.setCellEditor(selectableEditor);
			}
			columnModel.addColumn(column);
		}
//		model = new MailTableModel(new ArrayList<Mail>(), 4);
		table = new JTable(model);
		table.setColumnModel(columnModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setEnabled(true);
		table.setGridColor(Color.WHITE);
		table.setRowHeight(30);
		table.setRowSelectionAllowed(true);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				//一覧をダブルクリックすると別ウィンドウでメールを表示する。
				if (e.getClickCount() >= 2) {
					Mail mail = model.getRow(table.getSelectedRow());
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(mail.getFile()))))) {
						mail = Mail.analyze(reader, null, true);
						MailFrame mailFrame = new MailFrame(mail);
						mailFrame.setVisible(true);
					} catch (FileNotFoundException e1) {
						// TODO 自動生成された catch ブロック
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO 自動生成された catch ブロック
						e1.printStackTrace();
					}
				}
			}
		});
		JPanel root = new JPanel(new BorderLayout());
		JPanel southPanel = new JPanel(new GridLayout(1, 2));
		southPanel.add(statusLabel);
		southPanel.add(progressBar);
		progressBar.setVisible(false);
		root.add(new JScrollPane(table), BorderLayout.CENTER);
		root.add(southPanel, BorderLayout.SOUTH);
		return root;
	}

	public static String createName(String uidl) {
		StringBuffer strBuff = new StringBuffer(uidl.length() * 2 + 4);
		for (byte uidlByte : uidl.getBytes()) {
			strBuff.append(Integer.toHexString(uidlByte));
		}
		strBuff.append(".gze");
		return strBuff.toString();
	}

}
