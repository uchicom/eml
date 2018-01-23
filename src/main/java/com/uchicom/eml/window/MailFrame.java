// (c) 2018 uchicom
package com.uchicom.eml.window;

import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.uchicom.eml.core.Mail;
import com.uchicom.eml.util.LineNumberView;

/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class MailFrame extends JFrame {

	private Mail mail;
	public MailFrame(Mail mail) {
		super(mail.getSubject());
		this.mail = mail;
		initComponents();
	}
	private void initComponents() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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

		getContentPane().add(pane);
		pack();
	}
}
