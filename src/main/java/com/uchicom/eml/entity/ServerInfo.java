// (C) 2017 uchicom
package com.uchicom.eml.entity;

/**
 * @author uchicom: Shigeki Uchiyama
 */
public class ServerInfo {

  private String name;
  private String host;
  private int port;
  private boolean ssl;

  public ServerInfo(String name, String host, int port, boolean ssl) {
    this.name = name;
    this.host = host;
    this.port = port;
    this.ssl = ssl;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public boolean isSsl() {
    return ssl;
  }

  public void setSsl(boolean ssl) {
    this.ssl = ssl;
  }
}
