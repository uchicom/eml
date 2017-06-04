// (c) 2017 uchicom
package com.uchicom.eml.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class Pop3Util {

	/**
	 * ログイン処理
	 * @param br
	 * @param ps
	 * @param username
	 * @param password
	 * @return
	 * @throws IOException
	 */
	public static boolean login(BufferedReader br, PrintStream ps, String username,
			String password) throws IOException {
		boolean success = false;
		ps.print("USER " + username + "\r\n");
		ps.flush();
		String line = br.readLine();
		if (isOK(line)) {
			ps.print("PASS " + password + "\r\n");
			ps.flush();
			line = br.readLine();
			if (isOK(line)) {
				success = true;
			}
		}
		return success;
	}

	public static void quit(BufferedReader br, PrintStream ps) throws IOException {
		ps.print("QUIT\r\n");
		ps.flush();
		System.out.println("QUIT:" + br.readLine());
	}

	public static int stat(BufferedReader br, PrintStream ps) throws IOException {
		ps.print("STAT\r\n");
		ps.flush();
		String line = br.readLine();
		System.out.println("STAT" + line);
		int index = -1;
		if (isOK(line)) {
			String[] splits = line.split(" ");
			index = Integer.parseInt(splits[1]);
		}
		return index;
	}

	public static String uidl(BufferedReader br, PrintStream ps, int index)
			throws IOException {
		ps.print("UIDL " + index + "\r\n");
		ps.flush();
		String line = br.readLine();
		System.out.println("UIDL:" + line);
		String id = null;

		if (isOK(line)) {
			String[] splits = line.split(" ");
			id = splits[1];
		}
		return id;
	}

	public static List<String> uidl(BufferedReader br, PrintStream ps)
			throws IOException {
		ps.print("UIDL\r\n");
		ps.flush();
		String line = br.readLine();
		List<String> idList = new ArrayList<String>();
		if (isOK(line)) {
			line = br.readLine();
			while (line != null && !".".equals(line)) {
				String[] splits = line.split(" ");
				idList.add(splits[1]);
				line = br.readLine();
			}
		}
		return idList;
	}

	public static boolean isOK(String line) {
		return line != null && line.startsWith("+OK");
	}

}
