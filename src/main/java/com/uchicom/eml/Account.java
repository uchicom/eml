// (c) 2016 uchicom
package com.uchicom.eml;

/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class Account {

 	private	String domain;
	private String user;
	private String password;
	/**
	 * domainを取得します.
	 *
	 * @return domain
	 */
	public String getDomain() {
		return domain;
	}
	/**
	 * domainを設定します.
	 *
	 * @param domain domain
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
	/**
	 * userを取得します.
	 *
	 * @return user
	 */
	public String getUser() {
		return user;
	}
	/**
	 * userを設定します.
	 *
	 * @param user user
	 */
	public void setUser(String user) {
		this.user = user;
	}
	/**
	 * passwordを取得します.
	 *
	 * @return password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * passwordを設定します.
	 *
	 * @param password password
	 */
	public void setPassword(String password) {
		this.password = password;
	}
}
