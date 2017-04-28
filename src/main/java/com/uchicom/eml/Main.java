// (c) 2014 uchicom
package com.uchicom.eml;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * 画面を起動するメインクラス.
 * このメールの特徴は、タイトルや一覧の情報はキャッシュに置いておいて、
 * 基本はファイル管理。なので、メモリをあまり使用しない。というのが利点にしたい。
 * @author Shigeki Uchiyama
 *
 */
public final class Main
{

	/**
	 * メインメソッド.
	 * @param args
	 */
    public static void main(String[] args)
    {
    	File file = new File("./data/.lock");
    	boolean start = false;

		try {
			if (file.exists() || !file.createNewFile()) {
				int res = JOptionPane.showConfirmDialog(null, "ロックデータが存在しています。起動してもよいですか？", "２重起動エラー", JOptionPane.YES_NO_OPTION);
	    		System.out.println(res);
	    		if (res == JOptionPane.YES_OPTION) {
	    			start = true;
	    		}
			} else {
				start = true;
			}

    		if (start) {
    			SwingUtilities.invokeLater(()-> {
			    	MailFrame frame = new MailFrame(args);
			    	frame.setVisible(true);
    			});
    		}
		} catch (IOException e) {
			e.printStackTrace();
		}

    }
}

