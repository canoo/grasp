package com.canoo.grasp.swing

import javax.swing.JTable
import com.canoo.grasp.PresentationModelSwitch
import com.canoo.grasp.Store
import java.beans.PropertyChangeListener
import com.canoo.grasp.AttributeSwitch
import com.canoo.grasp.GraspLocale
import javax.swing.table.DefaultTableColumnModel

class PMTableFactory extends AbstractFactory {

    private static final String TABLE_MODEL = "_TABLE_MODEL_"
    private static final String COLUMN_MODEL = "_COLUMN_MODEL_"

    Object newInstance(FactoryBuilderSupport builder, Object name, Object table, Map attributes) {
        if (!table) table = new JTable()

        Class type = attributes.remove('type')
        PresentationModelSwitch pm = attributes.remove('selection')
        Store store = attributes.remove('store')
        Closure filter = attributes.remove('filter') ?: PMTableModel.DEFAULT_FILTER

        def model = new PMTableModel(store, type, filter)

        table.selectionModel.valueChanged = {
            int row = table.selectedRow
            pm.adaptee = (row == -1) ? null : model.rows[row] // todo dk: convert from view to row index
        }
        def onSelectedPMChanged = {e ->
            def pmRowIdx = model.rows.findIndexOf { it == pm.adaptee }  // todo dk: convert from row to view index
            // if not found, pmRowIdx==-1 and using that below works better than clearSelection()
            table.selectionModel.setSelectionInterval pmRowIdx, pmRowIdx
        }
        pm.addPropertyChangeListener onSelectedPMChanged as PropertyChangeListener

        GraspLocale.instance.addPropertyChangeListener('locale',{e->
             table.tableHeader.columnModel.columns.eachWithIndex {c, i ->
                 // println "${c.headerValue} ${model.getColumn(i).headerValue} ${model.getColumn(i).name()}" 
                 // c.setHeaderValue(model.getColumn(i).name())
             }
             table.tableHeader.repaint()
             table.repaint()
        } as PropertyChangeListener)

        def detailChangedListener = {e ->
            int row = table.selectedRow
            model.fireTableRowsUpdated row, row
        } as PropertyChangeListener

        def applyListener = null
        applyListener = {target ->
            target.proxyAttributePerName.values().each {att ->
                if (att in AttributeSwitch) {
                    if(target != pm) att.addPropertyChangeListener detailChangedListener
                } else if (att in PresentationModelSwitch) {
                    applyListener(att)
                }
            }
        }
        applyListener(pm)


        builder.context[TABLE_MODEL] = model
        builder.context[COLUMN_MODEL] = []
        builder.context[AttributeColumnFactory.TABLE_MODEL_TYPE] = type
        return table
    }

    void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        if (child instanceof AttributeColumn) {
            builder.parentContext[TABLE_MODEL].addAttributeColumn child
            builder.parentContext[COLUMN_MODEL] << child
        }
    }

    void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
        node.model = builder.context.remove(TABLE_MODEL)
        def columnModel = new DefaultTableColumnModel()
        builder.context[COLUMN_MODEL].each {c -> columnModel.addColumn(c)}
        // node.columnModel = columnModel
    }
}

class AttributeColumnFactory extends AbstractFactory {
    public static final String TABLE_MODEL_TYPE = "_TABLE_MODEL_TYPE_"

    Object newInstance(FactoryBuilderSupport builder, Object name, Object table, Map attributes) {
        Class type = builder.context[TABLE_MODEL_TYPE]
        if(!type && builder.parentContext) type = builder.parentContext[TABLE_MODEL_TYPE]
        if(!type) throw new IllegalArgumentException('attributeColumn must be nested inside a pmTable node.')
        return new AttributeColumn(type)
    }
}
