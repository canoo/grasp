package com.canoo.grasp

import groovy.model.DefaultTableModel
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeEvent
import javax.swing.JTable
import org.codehaus.groovy.runtime.MethodClosure

class GraspContext {

    static String lookup(String key) {
        key // todo: lookup in ResourceMap
    }

    private static List defaultPropnames = 'title value text'.tokenize()
    private static Map defaultConversion = [read: {it}, write: {it}]

    static useBinding(Store store) {
        //todo dk: reset EMC after use...
        // MetaMethod before = Object.metaClass.getMetaMethod("methodMissing", [String, Object] as Class[])

        Object.metaClass.bind = { PresentationModelSwitch pmRef, Closure target ->
            println pmRef
            def view = delegate

            def update = { PropertyChangeEvent e ->
                view.bind target(e.newValue)
            }
            pmRef.addPropertyChangeListener "adaptee", update as PropertyChangeListener
        }

        Object.metaClass.bind = {IAttribute attribute ->
            delegate.bind(Collections.EMPTY_MAP, attribute)
        }

        Object.metaClass.bind = {Map extra, IAttribute attribute = null ->
            def view = delegate
            Map convert = [*: defaultConversion, *: extra]    // extra read/write keys overwrite default

            def propname
            if (attribute) {                                // attribute argument given
                if (extra.field && extra.field instanceof MethodClosure) {
                    propname = extra.field.method
                } else {
                    propname = defaultPropnames.find { view.hasProperty(it) }
                }
                assert propname, "unable to retrieve property name from $defaultPropnames for $view"
            } else {                                        // attribute supplied in map
                propname = extra.keySet().find { extra[it] in Attribute }
                assert propname, "cannot find a property with attribute to use in binding"
                attribute = extra[propname]
            }

            // update view on attribute value change
            def update = { view[propname] = convert.read(attribute.value) }
            attribute.addPropertyChangeListener("value", update as PropertyChangeListener)
            update() // initial update needed

            // update attribute on view action
            def actions = convert.on?.tokenize()
            update = { attribute.value = convert.write(view[propname]) }
            if (!actions && view.respondsTo("addActionListener")) {   // default
                view.actionPerformed = update
            }
            for (action in actions) { view[action] = update }
            return view
        }

        Object.metaClass.syncWith = {PresentationModelSwitch pm ->
            JTable table = delegate
            DefaultTableModel model = table.model

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
            }
            pm.proxyAttributePerName.values().each { att ->
                if (att in AttributeSwitch){
                    att.addPropertyChangeListener detailChangedListener as PropertyChangeListener
                    return
                }
            }
        }

        Object.metaClass.syncList = { Class pmClass ->
            JTable table = delegate
            DefaultTableModel model = table.model

            def update = [
                added: { pm ->
                    model.rows << pm
                    int idx = model.rows.size() - 1
                    model.fireTableRowsInserted idx, idx
                },
                deleted: { pm -> model.rows.remove pm; model.fireTableDataChanged() }
            ] as IStoreListener
            store.addStoreListener pmClass, update
        }

        Object.metaClass.onSwitch = {PresentationModelSwitch pm, Closure callback=null ->
            def caller = delegate
            callback = callback ?: {it.enabled = pm.available() }
            def onSelectedPMChanged = { e -> callback caller }
            pm.addPropertyChangeListener onSelectedPMChanged as PropertyChangeListener
            callback caller
        }
    }
}
