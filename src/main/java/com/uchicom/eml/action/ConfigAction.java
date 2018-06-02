// (c) 2014 uchicom
package com.uchicom.eml.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.uchicom.eml.window.MailListFrame;

/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class ConfigAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private MailListFrame mailListFrame;
    public ConfigAction(MailListFrame mailListFrame) {
	this.mailListFrame = mailListFrame;
	putValue(NAME, "設定");
    }
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
	mailListFrame.config();
    }

}
