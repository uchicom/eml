//(c) 2014 uchicom
package com.uchicom.eml;

import java.io.File;

/**
 * 定数クラス.
 *
 * @author Shigeki Uchiyama
 *
 */
public class Constants {
	public static final File CONFIG_FILE = new File("conf/eml.properties");

	// リソースキー
	/** アプリケーションのタイトル */
	public static final String APPLICATION_TITLE = "application.title";

	/** アプリケーションのバージョン */
	public static final String APPLICATION_VERSION = "application.version";

	/** このアプリケーションについてのアクション名 */
	public static final String ACTION_NAME_ABOUT = "action.name.about";

	/** ヘルプ目次名のアクション名 */
	public static final String ACTION_NAME_HELP = "action.name.help";

	/** １６進数変換のアクション名 */
	public static final String ACTION_NAME_CONVERT_HEX = "action.name.convert.hex";

	/** 文字列変換のアクション名 */
	public static final String ACTION_NAME_CONVERT_STRING = "action.name.convert.string";

	/** ヘルプメニュー名 */
	public static final String MENU_NAME_HELP = "menu.name.help";
	/** ファイルメニュー名 */
	public static final String MENU_NAME_FILE = "menu.name.file";
	/** 送受信メニュー名 */
	public static final String MENU_NAME_TRANSFER = "menu.name.transfer";

	/** 文字列ラベル */
	public static final String LABEL_STRING = "label.string";

	/** １６進数ラベル */
	public static final String LABEL_HEX = "label.hex";

	/** 変換キャラクタセットのカンマ区切り文字列 */
	public static final String CHARSETS = "charsets";

	/** ヘルプの参照URL */
	public static final String URL_HELP = "url.help";

	/** このアプリケーションについての参照URL */
	public static final String URL_ABOUT = "url.about";

	public static final String TABLE_TITLES = "table.titles";

	//プロパティキー
	public static String PROP_ACCOUNTS = "accounts";
	public static String PROP_ACCOUNT = "account.";
	public static String PROP_USER = ".user";
	public static String PROP_NAME = ".name";
	public static String PROP_PASSWORD = ".password";
	public static String PROP_PATH = ".path";
	public static String PROP_RECEIVE = ".receive";
	public static String PROP_SEND = ".send";
	public static String PROP_HOST = ".host";
	public static String PROP_PORT = ".port";
	public static String PROP_SSL = ".ssl";


	//パス
	public static String MAILBOX = "mailbox";
	public static String UIDLMAP = "uidl.map";

	//初期値
	public static int DEFAULT_PORT_POP3 = 8115;
	public static int DEFAULT_PORT_SMTP = 25;

}
