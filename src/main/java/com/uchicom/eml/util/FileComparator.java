// (C) 2013 uchicom
package com.uchicom.eml.util;

import java.io.File;
import java.util.Comparator;

/**
 * ファイルの変更日時で並び替えするコンペアレータ.
 *
 * @author uchicom: Shigeki Uchiyama
 */
public class FileComparator implements Comparator<File> {

  protected static final FileComparator instance = new FileComparator();

  /* (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare(File arg0, File arg1) {
    if (arg0 == null) {
      if (arg1 == null) {
        return 0;
      } else {
        return 1;
      }
    } else {
      if (arg1 == null) {
        return -1;
      } else {
        return arg0.compareTo(arg1);
      }
    }
  }
}
