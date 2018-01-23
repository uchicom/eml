// (c) 2014 uchicom
package com.uchicom.eml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.SwingUtilities;

import com.uchicom.eml.core.Mail;
import com.uchicom.eml.window.MailFrame;
import com.uchicom.eml.window.MailListFrame;

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
		
		if (args.length > 0) {
			
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[0]))))) {
				System.out.println(args[0]);
				Mail mail = Mail.analyze(reader, null, true);
				MailFrame mailFrame = new MailFrame(mail);
				mailFrame.setVisible(true);
			} catch (FileNotFoundException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}
		} else {
			SwingUtilities.invokeLater(() -> {
				MailListFrame frame = new MailListFrame(args);
				frame.setVisible(true);
			});
		}

	}
}
