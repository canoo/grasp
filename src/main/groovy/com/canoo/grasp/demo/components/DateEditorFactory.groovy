package com.canoo.grasp.demo.components

import org.jdesktop.swingx.JXDatePicker
import com.canoo.grasp.Attribute
import java.beans.PropertyChangeListener

/**
 * A Date Editor Factory. 
 */
class DateEditorFactory extends AbstractFactory implements GraspEditor {
    
    Object newInstance(FactoryBuilderSupport ignored, Object name, Object value, Map map) {
        Attribute dateAttribute = value
        Attribute localeAttribute = map?.remove("locale")

        Date date = dateAttribute.value

        def picker = new JXDatePicker(date)
        picker.addPropertyChangeListener "date", { dateAttribute.value = it.newValue} as PropertyChangeListener
        picker.bind(dateAttribute, on: "actionPerformed focusLost keyReleased", field: picker.&date)

        if (localeAttribute) {
            Locale theLocale = localeAttribute.value
            picker.locale = theLocale
            picker.bind(localeAttribute, on: "actionPerformed focusLost keyReleased", field: picker.&locale)
        }
        picker
    }

    def boolean canHandle(Object value, Map attributes) {
        (value instanceof Attribute && value.value instanceof Date)
    }
}
