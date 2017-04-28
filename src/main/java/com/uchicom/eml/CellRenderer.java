// (c) 2014 uchicom
package com.uchicom.eml;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author uchiyama
 *
 */
public class CellRenderer extends DefaultTableCellRenderer {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JLabel selectedField = new JLabel();
	private JLabel selectedFocusField = new JLabel();
	private JLabel selectedFocusInitField = new JLabel();
	private JLabel focusField = new JLabel();
	/** 通常表示のフィールド */
	private JLabel normalEvenField = new JLabel();
	private JLabel normalOddField = new JLabel();

	public CellRenderer() {
		super();
		selectedField.setBackground(Color.LIGHT_GRAY);
		selectedField.setFont(selectedField.getFont()
				.deriveFont(Font.PLAIN, 12));
		selectedField.setOpaque(true);
		selectedFocusField.setBackground(Color.DARK_GRAY);
		selectedFocusField.setForeground(Color.WHITE);
		selectedFocusField.setFont(selectedFocusField.getFont().deriveFont(
				Font.PLAIN, 12));
		selectedFocusField.setOpaque(true);
		selectedFocusInitField.setBackground(Color.RED);
		selectedFocusInitField.setForeground(Color.WHITE);
		selectedFocusInitField.setFont(selectedFocusInitField.getFont()
				.deriveFont(Font.PLAIN, 12));
		selectedFocusInitField.setOpaque(true);
		normalEvenField.setBackground(new Color(200, 200 , 200));
		normalEvenField.setFont(normalEvenField.getFont().deriveFont(Font.PLAIN, 12));
		normalEvenField.setOpaque(true);
		normalEvenField.setBackground(new Color(250, 250, 250));
		normalOddField.setFont(normalOddField.getFont().deriveFont(Font.PLAIN, 12));
		normalOddField.setOpaque(true);
		// Border border = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		// normalField.setBorder(border);

	}

	/*
	 * (非 Javadoc)
	 *
	 * @see
	 * javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent
	 * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if (isSelected) {
			if (hasFocus) {
				// フォーカスあり選択ありのセルを返す
				selectedFocusField.setText((String) value);
				return selectedFocusField;
			} else {
				// 選択ありのセルを返す
				selectedField.setText((String) value);
				return selectedField;
			}
		} else if (hasFocus) {
			// フォーカスあり状態のセルを返す
			focusField.setText((String) value);
			return focusField;
		} else if (row % 2 == 0){
			// 通常のセル表示を返す(偶数)
			normalEvenField.setText((String) value);
			return normalEvenField;
		} else {
			// 通常のセル表示を返す(奇数)
			normalOddField.setText((String) value);
			return normalOddField;
		}
	}

}
