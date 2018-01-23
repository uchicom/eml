// (c) 2014 uchicom
package com.uchicom.eml.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.uchicom.eml.window.MailListFrame;

/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class AccountConfigAction extends AbstractAction {

    private MailListFrame mailListFrame;
    public AccountConfigAction(MailListFrame mailListFrame) {
	this.mailListFrame = mailListFrame;
	putValue(NAME, "アカウント設定");
    }
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
	mailListFrame.accountConfig();

    }

}
