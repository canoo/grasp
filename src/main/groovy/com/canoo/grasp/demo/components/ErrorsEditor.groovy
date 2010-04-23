package com.canoo.grasp.demo.components

import com.canoo.grasp.Attribute
import com.canoo.grasp.demo.domain.Errors
import groovy.xml.MarkupBuilder
import java.awt.Color
import javax.swing.SwingUtilities

/**
 * HTML label based display for Errors.  
 */
class ErrorsEditor implements GraspEditor {
    def boolean canHandle(Object attribute, Map map) {
        attribute instanceof Attribute && attribute.model instanceof Errors
    }

    def Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map map) {

        Attribute errorsAttribute = value

        def label = builder.label(foreground: Color.RED)
        label.bind(errorsAttribute,
                read: { newValue ->
                    SwingUtilities.invokeLater({SwingUtilities.getRoot(label).pack()})
                    if (!newValue) return ""

                    def out = new StringWriter()
                    new MarkupBuilder(out).html() {
                        ul() {
                            newValue.each { 
                                li(it.id)
                            }
                        }
                    }
                    out.toString()
                })
        label
    }
}
