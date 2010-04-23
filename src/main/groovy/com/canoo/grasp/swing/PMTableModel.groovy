package com.canoo.grasp.swing

import javax.swing.table.AbstractTableModel
import com.canoo.grasp.IStoreListener
import com.canoo.grasp.Store

class PMTableModel extends AbstractTableModel {
    private final Class type
    private final List source
    private final Closure filter
    private List attributeColumns = []

    public static final DEFAULT_FILTER = {pmodel -> true}

    PMTableModel(Store store, Class type, Closure filter = null) {
        this.type = type
        this.filter = filter ?: DEFAULT_FILTER
        this.source = type.list().findAll(this.filter)

        def update = [
                added: {pm ->
                    if (filter(pm)) {
                        source << pm
                        int idx = source.size() - 1
                        fireTableRowsInserted idx, idx
                    }
                },
                deleted: {pm ->
                    int idx = source.indexOf(pm)
                    if(idx != -1) {
                        source.remove pm
                        fireTableRowsDeleted idx, idx
                    }
                },
                updated: {pm ->
                    int idx = source.indexOf(pm)
                    if(idx != -1) {
                        fireTableRowsUpdated idx, idx
                    }
                }
        ] as IStoreListener
        store.addStoreListener type, update
    }

    void addAttributeColumn(AttributeColumn column) {
        if (column) attributeColumns << column
    }

    void removeAttributeColumn(AttributeColumn column) {
        if (column) attributeColumns.remove(column)
    }

    int getRowCount() {
        return source.size();
    }

    AttributeColumn getColumn(int columnIndex) {
        attributeColumns[columnIndex]
    }

    String getColumnName(int columnIndex) {
        return attributeColumns[columnIndex].name();
    }

    Class<?> getColumnClass(int columnIndex) {
        return attributeColumns[columnIndex].type();
    }

    boolean isCellEditable(int rowIndex, int columnIndex) {
        attributeColumns[columnIndex].isEditable(source[rowIndex])
    }

    Object getValueAt(int rowIndex, int columnIndex) {
        return attributeColumns[columnIndex].getValue(source[rowIndex]);
    }

    void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        attributeColumns[columnIndex].setValue(source[rowIndex], aValue);
    }

    int getColumnCount() {
        attributeColumns.size()
    }

    List getRows() {
        Collections.unmodifiableList(source)
    }
}
