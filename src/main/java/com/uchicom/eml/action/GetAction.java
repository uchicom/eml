// (c) 2014 uchicom
package com.uchicom.eml.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.uchicom.eml.window.MailListFrame;

public class GetAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	MailListFrame frame;
	public GetAction(MailListFrame frame) {
		putValue(NAME, "取得");
		this.frame = frame;
	}
	@Override
	public void actionPerformed(ActionEvent e) {

		// TODO Auto-generated method stub
		frame.search();
	}

}
