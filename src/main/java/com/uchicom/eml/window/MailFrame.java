// (c) 2014 uchicom
package com.uchicom.eml.window;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

import com.uchicom.eml.Constants;
import com.uchicom.eml.action.AccountConfigAction;
import com.uchicom.eml.action.ConfigAction;
import com.uchicom.eml.action.GetAction;
import com.uchicom.eml.core.Account;
import com.uchicom.eml.entity.ServerInfo;
import com.uchicom.eml.util.ResourceUtil;

public class MailFrame extends JFrame {

	List<Account> accountList = new ArrayList<>();

	private Properties config = new Properties();

	public MailFrame(String[] args) {
		super(ResourceUtil.getString(Constants.APPLICATION_TITLE));
		loadProperties();
		loadAccount();
		initComponents(args);
	}

	/**
	 * アカウント情報読み込み
	 */
	private void loadAccount() {
		createAccountList().forEach(account -> {
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
		// プロパティからアカウント情報抽出（パスワードは暗号化しておかないと）
		// gzipプロパティにして暗号化
		String accountKyes = config.getProperty(Constants.PROP_ACCOUNTS);
		if (accountKyes != null) {
			for (String key : accountKyes.split(",")) {
				String accountKey = Constants.PROP_ACCOUNT + key;
				Account account = new Account(config.getProperty(accountKey + Constants.PROP_NAME),
						config.getProperty(accountKey + Constants.PROP_USER),
						config.getProperty(accountKey + Constants.PROP_PASSWORD),
						config.getProperty(accountKey + Constants.PROP_PATH),
						createServerInfo(config.getProperty(accountKey + Constants.PROP_RECEIVE)),
						createServerInfo(config.getProperty(accountKey + Constants.PROP_SEND)));
				if (checkInput(account)) {
					accountList.add(account);
				}
			}

		}
		return accountList;
	}

	private ServerInfo createServerInfo(String key) {
		String serverKey = "server." + key;
		return new ServerInfo(config.getProperty(serverKey + Constants.PROP_NAME),
				config.getProperty(serverKey + Constants.PROP_HOST),
				Integer.parseInt(config.getProperty(serverKey + Constants.PROP_PORT)),
				Boolean.parseBoolean(config.getProperty(serverKey + Constants.PROP_SSL)));
	}

	/**
	 * 入力内容チェック
	 *
	 * @param account
	 * @return
	 */
	private boolean checkInput(Account account) {
		if (account.getUser() == null) {
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
		accountList.forEach((account) -> {
			Thread thread = new Thread(() -> {
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
		try (FileInputStream fis = new FileInputStream(Constants.CONFIG_FILE)) {
			config.load(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 画面初期化
	 *
	 * @param args
	 */
	private void initComponents(String[] args) {
		// メニュー設定
		setJMenuBar(createJMenuBar());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// 終了処理
				accountList.forEach((account) -> {
					account.saveUidlMap();
				});
			}
		});


		JTabbedPane pane = new JTabbedPane();
		accountList.forEach((account) -> {
			pane.add(account.getName(), account.dispList());
		});
		getContentPane().add(pane);

		pack();

	}

	/**
	 * メニュー作成
	 *
	 * @return
	 */
	public JMenuBar createJMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu(ResourceUtil.getString(Constants.MENU_NAME_FILE));
		JMenuItem menuItem = new JMenuItem(new ConfigAction(this));
		menu.add(menuItem);
		menuItem = new JMenuItem(new AccountConfigAction(this));
		menu.add(menuItem);
		menuBar.add(menu);
		menu = new JMenu(ResourceUtil.getString(Constants.MENU_NAME_TRANSFER));
		menuItem = new JMenuItem(new GetAction(this));
		menu.add(menuItem);
		menuBar.add(menu);

		return menuBar;
	}

	/**
	 * アカウント画面表示
	 */
	public void accountConfig() {
		AccountConfigDialog dialog = new AccountConfigDialog(this);
		dialog.pack();
		dialog.setVisible(true);
	}

	/**
	 * 設定画面表示
	 */
	public void config() {
		ConfigDialog dialog = new ConfigDialog(this);
		dialog.pack();
		dialog.setVisible(true);
	}
}
