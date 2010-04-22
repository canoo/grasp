package com.canoo.grasp.demo.components

import org.jdesktop.swingx.JXDatePicker

/**
 * A Date Editor Factory. 
 */
class DateEditorFactory extends AbstractFactory {
    
    Object newInstance(FactoryBuilderSupport ignored, Object nodeName, Object attribute, Map map) {
        def picker = new JXDatePicker(attribute.value)
        picker.bind(attribute, on: "actionPerformed focusLost keyReleased", field: picker.&date)
        if (map.locale) {
            picker.locale = map.locale.value
            picker.bind(map.locale,
                    on: "actionPerformed focusLost keyReleased",
                    field: picker.&locale,
                    read: {
                        println "read locale ${it.getClass()}";it
                    },
                    write: {
                        println "write locale ${it.getClass()}";it
                    }
            )
        }
        picker
    }
}
