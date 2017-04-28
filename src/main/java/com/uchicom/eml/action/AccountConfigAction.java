// (c) 2014 uchicom
package com.uchicom.eml.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.uchicom.eml.MailFrame;

/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class AccountConfigAction extends AbstractAction {

    private MailFrame mailFrame;
    public AccountConfigAction(MailFrame mailFrame) {
	this.mailFrame = mailFrame;
	putValue(NAME, "アカウント設定");
    }
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
	mailFrame.accountConfig();

    }

}
