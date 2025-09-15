// (C) 2014 uchicom
package com.uchicom.eml;

public class Test {

  public static void main(String[] args) {
    int z = 0;
    long start = System.currentTimeMillis();
    int[] startVals = new int[] {'A', 'a'};
    for (int k = 0; k < 10000000; k++) {
      int startVal = startVals[k % 2];
      for (int i = startVal; i < startVal + 26; i++) {
        //			if (i == 'a' || i == 'A') {
        //				z++;
        //			}
        //			z = twin(i, 'a', 'A', z);//190[ms]
        //			if ((i | 0x20) == 'a') {
        //				z++;
        //			}
        z = or(i, 'a', z); // 160[ms] 正確性の問題からこちらが優位かな？

        //			if ((i & 0x5F) == 'A') {
        //				z++;
        //			}
        //			z = and(i, 'A', z);//160[ms]
      }
    }
    System.out.println("value=" + z);
    System.out.println((System.currentTimeMillis() - start) + "[ms]");
  }

  public static int twin(int ch, int compS, int compL, int z) {
    if (ch == compS || ch == compL) {
      z++;
    }
    return z;
  }

  public static int or(int ch, int comp, int z) {
    if ((ch | 0x20) == comp) {
      z++;
    }
    return z;
  }

  public static int and(int ch, int comp, int z) {
    if ((ch & 0x5F) == comp) {
      z++;
    }
    return z;
  }
}
