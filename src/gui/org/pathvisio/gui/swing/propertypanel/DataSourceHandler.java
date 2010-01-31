// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.pathvisio.gui.swing.propertypanel;

import java.util.Map;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Iterator;
import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;

import org.pathvisio.model.PropertyType;
import org.pathvisio.model.StaticPropertyType;
import org.pathvisio.model.Pathway;
import org.pathvisio.gui.swing.SwingEngine;
import org.bridgedb.DataSource;
import org.bridgedb.bio.Organism;

/**
 * This class knows how to handle a datasource, which is context sensitive and needs to be updated before use.
 *
 * @author Mark Woon
 */
public class DataSourceHandler extends DefaultCellEditor implements ContextSensitiveEditor, TableCellRenderer,
		TypeHandler {
	private JComboBox renderer;
	private JComboBox editor;
	private Map<Object, Object> label2value = new HashMap<Object, Object>();
	private Map<Object, Object> value2label = new HashMap<Object, Object>();


	public DataSourceHandler() {
		super(new JComboBox(new String[0]));
		editor = (JComboBox)getComponent();
		editor.setBorder(BorderFactory.createEmptyBorder());
		renderer = new JComboBox();
	}


	//-- ContextSensitiveEditor methods --//

	public void updateEditor(SwingEngine swingEngine, Pathway pathway, PropertyView propHandler) {

		SortedSet<DataSource> dataSources = new TreeSet<DataSource>(new Comparator<DataSource>() {
			public int compare(DataSource arg0, DataSource arg1) {
				return ("" + arg0.getFullName()).toLowerCase().compareTo(("" + arg1.getFullName()).toLowerCase());
			}
		});
		dataSources.addAll(DataSource.getFilteredSet(true, null, Organism.fromLatinName(pathway.getMappInfo().getOrganism())));

		if (isDifferent(dataSources)) {
			renderer.removeAllItems();
			editor.removeAllItems();
			label2value.clear();
			value2label.clear();
			for (DataSource s : dataSources) {
				String name = s.getFullName() == null ? s.getSystemCode() : s.getFullName();
				renderer.addItem(name);
				editor.addItem(name);
				label2value.put(name, s);
				value2label.put(s, name);
			}
		}
	}

	private boolean isDifferent(SortedSet<DataSource> dataSources) {

		if (editor.getItemCount() != dataSources.size()) {
			return true;
		}
		Iterator<DataSource> it = dataSources.iterator();
		for (int x = 0; x < editor.getItemCount(); x++) {
			if (!editor.getItemAt(x).equals(it.next().getFullName())) {
				return true;
			}
		}
		return false;
	}



	//-- TypeHandler methods --//

	public PropertyType getType() {
		return StaticPropertyType.DATASOURCE;
	}

	public TableCellRenderer getLabelRenderer() {
		return null;
	}

	public TableCellRenderer getValueRenderer() {
		return this;
	}

	public TableCellEditor getValueEditor() {
		return this;
	}


	//-- TableCellRenderer methods --//

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		if (isSelected) {
			renderer.setForeground(table.getSelectionForeground());
			renderer.setBackground(table.getSelectionBackground());
		} else {
			renderer.setForeground(table.getForeground());
			renderer.setBackground(table.getBackground());
		}

		renderer.setSelectedItem(value2label.get(value));
		return renderer;
	}


	//-- TableCellEditor methods --//

	public Object getCellEditorValue() {

		return label2value.get(editor.getSelectedItem());
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		editor.setSelectedItem(value2label.get(value));
		return editor;
	}
}