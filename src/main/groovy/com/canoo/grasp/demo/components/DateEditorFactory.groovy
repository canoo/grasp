package com.canoo.grasp.demo.components

import org.jdesktop.swingx.JXDatePicker

/**
 * A Date Editor Factory. 
 */
class DateEditorFactory extends AbstractFactory {
    
    Object newInstance(FactoryBuilderSupport ignored, Object nodeName, Object attribute, Map map) {
        def picker = new JXDatePicker(attribute.value)
        picker.bind(attribute, on: "actionPerformed focusLost keyReleased", field: picker.&date)
        picker
    }
}
