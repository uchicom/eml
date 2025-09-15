// (C) 2014 uchicom
package com.uchicom.eml.window;

import com.uchicom.eml.table.CellRenderer;
import com.uchicom.ui.ResumeDialog;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

/**
 * @author uchicom: Shigeki Uchiyama
 */
public class ConfigDialog extends ResumeDialog {

  /** */
  private static final long serialVersionUID = 1L;

  /**
   * @param arg0
   */
  public ConfigDialog(Properties config, String windowKey) {
    super(config, windowKey);
    initComponents();
  }

  private void initComponents() {
    this.addWindowListener(
        new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            ConfigDialog.this.dispose();
          }
        });
    this.setTitle("設定");
    DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
    TableColumn column = null;
    CellRenderer renderer = new CellRenderer();
    String[] titles = new String[] {"設定値", "値"};
    int[] widths = new int[] {100, 100};
    for (int i = 0; i < titles.length; i++) {
      column = new TableColumn();
      column.setIdentifier(i);
      column.setModelIndex(i);
      column.setCellRenderer(renderer);
      column.setHeaderValue(titles[i]);
      column.setPreferredWidth(widths[i]);
      columnModel.addColumn(column);
    }
    String[][] data =
        new String[][] {
          new String[] {"ボーダーカラー", "デフォルト値"},
          new String[] {"文字の色", "#000000"},
          new String[] {"文字のフォント", "Corier"},
          new String[] {"業の高さ", "15"}
        };
    JTable table = new JTable(data, new String[] {"設定値", "APOP"});
    table.setColumnModel(columnModel);
    table.setGridColor(Color.decode("#000000"));
    add(new JScrollPane(table));
  }
}
