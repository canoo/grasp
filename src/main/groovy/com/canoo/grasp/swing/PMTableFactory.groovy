package com.canoo.grasp.swing

import javax.swing.JTable
import com.canoo.grasp.PresentationModelSwitch
import com.canoo.grasp.Store
import java.beans.PropertyChangeListener
import com.canoo.grasp.AttributeSwitch

/**
 * Created by IntelliJ IDEA.
 * User: aalmiray
 * Date: Apr 22, 2010
 * Time: 12:21:00 AM
 * To change this template use File | Settings | File Templates.
 */
class PMTableFactory extends AbstractFactory {

    private static final String TABLE_MODEL = "_TABLE_MODEL_"

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

        def detailChangedListener = {e ->
            int row = table.selectedRow
            model.fireTableRowsUpdated row, row
        } as PropertyChangeListener

        def applyListener = null
        applyListener = {target ->
            target.proxyAttributePerName.values().each {att ->
                if (att in AttributeSwitch) {
                    att.addPropertyChangeListener detailChangedListener
                    return
                } else if (att in PresentationModelSwitch) {
                    // applyListener(att)
                }
            }
        }
        applyListener(pm)


        builder.context[TABLE_MODEL] = model
        builder.context[AttributeColumnFactory.TABLE_MODEL_TYPE] = type
        return table
    }

    void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        if (child instanceof AttributeColumn) {
            builder.parentContext[TABLE_MODEL].addAttributeColumn child
        }
    }

    void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
        node.model = builder.context.remove(TABLE_MODEL)
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
