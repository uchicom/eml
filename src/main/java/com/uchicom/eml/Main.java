// (c) 2014 uchicom
package com.uchicom.eml;

import javax.swing.SwingUtilities;

/**
 * 画面を起動するメインクラス. このメールの特徴は、タイトルや一覧の情報はキャッシュに置いておいて、
 * 基本はファイル管理。なので、メモリをあまり使用しない。というのが利点にしたい。
 *
 * @author Shigeki Uchiyama
 *
 */
public final class Main {

	/**
	 * メインメソッド.
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {
			MailFrame frame = new MailFrame(args);
			frame.setVisible(true);
		});

	}
}
