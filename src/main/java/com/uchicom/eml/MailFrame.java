// (c) 2014 uchicom
package com.uchicom.eml;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.uchicom.eml.action.AccountConfigAction;
import com.uchicom.eml.action.ConfigAction;
import com.uchicom.eml.action.GetAction;

public class MailFrame extends JFrame {



	private JButton searchButton = new JButton(new GetAction(this));

	List<Account> accountList = new ArrayList<>();

	private Properties config = new Properties();


	public MailFrame(String[] args) {
		super("EML");
		loadProperties();
		loadAccount();
		initComponents(args);
	}

	/**
	 * アカウント情報読み込み
	 */
	private void loadAccount() {
		createAccountList().forEach(account->{
			// 検索済みのメールをロードする。
			account.loadMail();
			// UIDLマップをロードする。
			account.loadUidlMap();
		});
	}
	/**
	 * アカウント情報リストを取得します
	 */
	private List<Account> createAccountList() {
		//プロパティからアカウント情報抽出（パスワードは暗号化しておかないと）
		//gzipプロパティにして暗号化
		String accountKyes = config.getProperty("accounts");
		if (accountKyes != null) {
			for (String key : accountKyes.split(",")) {
				Account account = new Account(config.getProperty(key + ".name"),
						config.getProperty(key + ".user"),
						config.getProperty(key + ".domain"),
						config.getProperty(key + ".password"),
						config.getProperty(key + ".path"));
				if (checkInput(account)) {
					accountList.add(account);
				}
			}

		}
		return accountList;
	}
	/**
	 * 入力内容チェック
	 * @param account
	 * @return
	 */
	private boolean checkInput(Account account) {
		if (account.getUser() == null) {
			return false;
		} else if (account.getDomain() == null) {
			return false;
		} else if (account.getName() == null) {
			return false;
		} else if (account.getPassword() == null) {
			return false;
		} else {
			return true;
		}
	}
	/**
	 * 検索処理
	 */
	public void search() {
		accountList.forEach((account)-> {
			Thread thread = new Thread(()->{
				account.downloadAllMail();
			});
			thread.setDaemon(true);
			thread.start();
		});
	}

	/**
	 * プロパティファイル読込
	 */
	private void loadProperties() {
		try (FileInputStream fis = new FileInputStream("conf/eml.properties")) {
			config.load(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 画面初期化
	 * @param args
	 */
	private void initComponents(String[] args) {
		// メニュー設定
		setJMenuBar(createJMenuBar());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				//終了処理
				accountList.forEach((account)-> {
					account.saveUidlMap();
				});
			}
		});
		JPanel northPanel = new JPanel(new GridLayout(1, 4));
//		northPanel.add(domainField);
//		northPanel.add(userField);
//		northPanel.add(passwordField);
		northPanel.add(searchButton);

		Container root = getContentPane();
		root.setLayout(new BorderLayout());
		root.add(northPanel, BorderLayout.NORTH);



		JTabbedPane pane = new JTabbedPane();
		accountList.forEach((account)-> {
			pane.add(account.getName(), account.dispList());
		});
		getContentPane().add(pane, BorderLayout.CENTER);

		pack();

	}

	/**
	 * メニュー作成
	 * @return
	 */
	public JMenuBar createJMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("ファイル");
		JMenuItem menuItem = new JMenuItem(new ConfigAction(this));
		menu.add(menuItem);
		menuItem = new JMenuItem(new AccountConfigAction(this));
		menu.add(menuItem);
		menuBar.add(menu);

		return menuBar;
	}

//	public boolean isArrival() {
//		boolean arrival
//	}

	public void accountConfig() {
		AccountConfigDialog dialog = new AccountConfigDialog(this);
		dialog.pack();
		dialog.setVisible(true);
	}

	public void config() {
		ConfigDialog dialog = new ConfigDialog(this);
		dialog.pack();
		dialog.setVisible(true);
	}
}
