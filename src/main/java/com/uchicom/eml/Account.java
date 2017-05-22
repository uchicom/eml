// (c) 2016 uchicom
package com.uchicom.eml;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Insets;
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
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.DefaultCellEditor;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.uchicom.eml.util.FileComparator;
import com.uchicom.eml.util.LineNumberView;

/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class Account {

	SimpleDateFormat format = new SimpleDateFormat(
			"EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
	SimpleDateFormat format2 = new SimpleDateFormat("d MMM yyyy HH:mm:ss Z",
			Locale.ENGLISH);
	private static final int NONE = 0;
	private static final int SUBJECT = 1;
	private static final int BODY = 2;
	private static final int CONTENT_TYPE = 3;
	private static final int OTHER = 99;// とりあえずその他

	private MailTableModel model = new MailTableModel(new ArrayList<>(), 5);
	private JTable table;

	private JProgressBar progressBar = new JProgressBar();
	private String name;
 	private	String domain;
	private String user;
	private String password;
	private String path;
	private int pop3Port = 110;
	private int smtpPort = 25;


	private JLabel statusLabel = new JLabel();
	private Map<String, String> uidlMap = new HashMap<String, String>();

	public Account(String name, String user, String domain, String password, String path) {
		this.name = name;
		this.user = user;
		this.domain = domain;
		this.password = password;
		this.path = path;
		if (path != null) {
			File file = new File(path);
			if (!file.exists()) {
				file.mkdirs();
			}
		}
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * domainを取得します.
	 *
	 * @return domain
	 */
	public String getDomain() {
		return domain;
	}
	/**
	 * domainを設定します.
	 *
	 * @param domain domain
	 */
	public void setDomain(String domain) {
		this.domain = domain;
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

		File uidlFile = new File(path, "uidl.map");
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
		File file = new File(path, ".lock");
		file.delete();
	}

	public void loadMail() {
		File mailboxFile = new File(path, "mailbox");
		if (mailboxFile.exists()) {
			File[] mails = mailboxFile.listFiles(new FileFilter() {

				@Override
				public boolean accept(File file) {
					return file.isFile() && !file.isHidden() && file.getName().endsWith(".gze");
				}

			});
			Arrays.sort(mails, new FileComparator());
			for (File file : mails) {
				try (GZIPInputStream inputStream = new GZIPInputStream(new FileInputStream(file));){
//					mailList.add(analyze(new BufferedReader(
//							new InputStreamReader(new FileInputStream(mail))), null));
					Mail mail = analyze(new BufferedReader(
							new InputStreamReader(inputStream)), null, true);
					mail.setBody(null);
					mail.setFile(file);
					this.model.addRow(mail);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("mail:" + model.getRowCount());
//			this.model.addList(mailList);
		}
	}


	private Mail analyze(BufferedReader br, OutputStream os, boolean retentionBody)
			throws IOException {

		long start = System.currentTimeMillis();
		long now = 0;
		String line = br.readLine();
		boolean isBody = false;
		StringBuffer bodyBuff = new StringBuffer();
		StringBuffer contentTypeBuff = new StringBuffer();
		Mail mail = new Mail();
		int status = NONE;
		StringBuffer subjectBuff = new StringBuffer();
		while (line != null && !".".equals(line)) {
			if (!isBody && "".equals(line)) {
				if (os == null && !retentionBody) {
					break;
				}
				isBody = true;
				status = BODY;
			} else if (isBody) {
				if (retentionBody) {
					if (line.length() > 0 && line.charAt(0) == '.') {
						bodyBuff.append(line.substring(1));
					} else {
						bodyBuff.append(line);
					}
					bodyBuff.append("\r\n");
				}
			} else if (line.matches("From: .*")) {
				status = OTHER;
				mail.setFrom(line.substring(6));
			} else if (line.matches("To: .*")) {
				status = OTHER;
				mail.setTo(line.substring(4));
			} else if (line.matches("Date: .*")) {
				status = OTHER;
				int comma = line.indexOf(',');
				if (comma >= 0) {
					try {
						mail.setDate(format.parse(line.substring(6)));
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
			} else if (line.matches("Content-Type: .*")) {
				// System.out.println(i + ":" +line);
				contentTypeBuff.append(line.substring(13));
				status = CONTENT_TYPE;
				// boundary = line.substring(line.indexOf("boundary=") + 9);
				// System.out.println(boundary);
			} else if (line.matches("Content-Transfer-Encoding\\: .*")) {
				// System.out.println(i + ":" +line);
				mail.setEncoding(line.substring(27));
			} else if (line.startsWith("Subject:")) {
				status = SUBJECT;
				if (line.length() > 9) {
					subjectBuff.append(line.substring(9));
				}
			} else if (line.startsWith(" ") || line.startsWith("\t")) {
				switch (status) {
				case SUBJECT:
					subjectBuff.append(line.substring(1));
					break;
				case CONTENT_TYPE:
					contentTypeBuff.append(line.substring(1));
					break;
				}
			} else {
				status = OTHER;
			}
			if (os != null) {
				os.write(line.getBytes());
				os.write("\r\n".getBytes());
			}
			line = br.readLine();
		}

		now = System.currentTimeMillis();
		System.out.println("retr:" + (now - start) + "[ms]");
		start = System.currentTimeMillis();

		mail.setContentType(contentTypeBuff.toString());
		mail.setSubject(subjectBuff.toString());
		mail.setBody(bodyBuff.toString());

		// 終了
		return mail;
	}
	/**
	 * USER ユーザID設定 PASS パスワード設定 またはAPOPを指定 ログイン後 STAT 最大のメールインデックスを取得 UIDL
	 * で最大のユニークIDを取得し、保存しているUIDLに含まれていなければ検索する,含まれていれば検索しない UIDL
	 * で全体のユニークIDを取得し、含まれていないメールインデックスを抽出 RETR または TOP でメール情報を取得
	 *
	 * @param args
	 * @return
	 */
	public List<Mail> getMail() {

		statusLabel.setText("接続");
		progressBar.setVisible(true);
		long start1 = System.currentTimeMillis();
		System.out.println("開始");
		List<Mail> mailList = new ArrayList<Mail>();


		long start = System.currentTimeMillis();
		long now = 0;
		try (Socket socket = new Socket();) {

			progressBar.setStringPainted(true);//%表示

			progressBar.setMinimum(0);
			progressBar.setMaximum(100);
			socket.connect(new InetSocketAddress(domain, 8115));
			now = System.currentTimeMillis();
			System.out.println("connect:" + (now - start) + "[ms]");
			start = System.currentTimeMillis();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			PrintStream ps = new PrintStream(socket.getOutputStream());
			String line = br.readLine();

			if (!login(br, ps, user, password)) {
				quit(br, ps);
			}

			statusLabel.setText("LOGIN");
			progressBar.setValue(1);
			now = System.currentTimeMillis();
			System.out.println("login:" + (now - start) + "[ms]");
			start = System.currentTimeMillis();

			int maxIndex = stat(br, ps);
			if (maxIndex < 1) {
				quit(br, ps);
			}

			statusLabel.setText("STAT");
			progressBar.setValue(2);
			now = System.currentTimeMillis();
			System.out.println("stat:" + (now - start) + "[ms]");
			start = System.currentTimeMillis();

			statusLabel.setText("新着チェック");
			progressBar.setValue(3);
			String id = uidl(br, ps, maxIndex);
			if (id == null) {
				quit(br, ps);
			} else {
				if (uidlMap.containsKey(id)) {
					// 新しいメールはない。
					return mailList;
				}
			}

			now = System.currentTimeMillis();
			System.out.println("uidl:" + (now - start) + "[ms]");
			start = System.currentTimeMillis();
			progressBar.setValue(4);
			statusLabel.setText("UIDL");

			List<String> idList = uidl(br, ps);
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
				quit(br, ps);
				return mailList;
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
				// ps.print("TOP " + (i + 1)+ " 0\r\n");
				ps.flush();
				File file = new File(mailboxFile, name);
				try (GZIPOutputStream pw = new GZIPOutputStream(new FileOutputStream(file));) {
					Mail mail = analyze(br, pw, true);
	//				mail.getTempList();
					mail.setBody(null);
					mail.setFile(file);
					this.model.addRow(mail);
					pw.flush();
				}
			}
			progressBar.setValue(100);
			statusLabel.setText("QUIT");
			quit(br, ps);
			// br.close();
			// ps.close();
			progressBar.setVisible(false);
			statusLabel.setText("");

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		System.out.println("終了:" + (System.currentTimeMillis() - start1) / 1000d);
		return mailList;
	}

	public JPanel dispList() {
		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
		TableColumn column = null;
		CellRenderer renderer = new CellRenderer();
		String[] titles = new String[] { "No", "差出人", "送信日時", "タイトル", "添付" };
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
						mail = analyze(reader, null, true);
						JTextArea area = new JTextArea(mail.getBody());
						area.setEditable(false);
						Insets inset = area.getInsets();
						inset.left = 5;
						inset.right = 5;
						area.setMargin(inset);

						LineNumberView view = new LineNumberView(area);
						JScrollPane pane = new JScrollPane(area);
						pane.setRowHeaderView(view);
						pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

						JFrame frame = new JFrame(mail.getSubject());
						frame.getContentPane().add(pane);
						frame.pack();
						frame.setVisible(true);
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

	public boolean login(BufferedReader br, PrintStream ps, String username,
			String password) throws IOException {
		boolean success = false;
		ps.print("USER " + username + "\r\n");
		ps.flush();
		String line = br.readLine();
		if (isOK(line)) {
			ps.print("PASS " + password + "\r\n");
			ps.flush();
			line = br.readLine();
			if (isOK(line)) {
				success = true;
			}
		}
		return success;
	}

	public void quit(BufferedReader br, PrintStream ps) throws IOException {
		ps.print("QUIT\r\n");
		ps.flush();
		System.out.println("QUIT:" + br.readLine());
	}

	public int stat(BufferedReader br, PrintStream ps) throws IOException {
		ps.print("STAT\r\n");
		ps.flush();
		String line = br.readLine();
		System.out.println("STAT" + line);
		int index = -1;
		if (isOK(line)) {
			String[] splits = line.split(" ");
			index = Integer.parseInt(splits[1]);
		}
		return index;
	}

	public String uidl(BufferedReader br, PrintStream ps, int index)
			throws IOException {
		ps.print("UIDL " + index + "\r\n");
		ps.flush();
		String line = br.readLine();
		System.out.println("UIDL:" + line);
		String id = null;

		if (isOK(line)) {
			String[] splits = line.split(" ");
			id = splits[1];
		}
		return id;
	}

	public List<String> uidl(BufferedReader br, PrintStream ps)
			throws IOException {
		ps.print("UIDL\r\n");
		ps.flush();
		String line = br.readLine();
		List<String> idList = new ArrayList<String>();
		if (isOK(line)) {
			line = br.readLine();
			while (line != null && !".".equals(line)) {
				String[] splits = line.split(" ");
				idList.add(splits[1]);
				line = br.readLine();
			}
		}
		return idList;
	}

	public boolean isOK(String line) {
		return line != null && line.startsWith("+OK");
	}

	public String createName(String uidl) {
		StringBuffer strBuff = new StringBuffer(uidl.length() * 2 + 4);
		for (byte uidlByte : uidl.getBytes()) {
			strBuff.append(Integer.toHexString(uidlByte));
		}
		strBuff.append(".gze");
		return strBuff.toString();
	}

}
