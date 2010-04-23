package com.canoo.grasp

import java.beans.PropertyChangeListener
import java.beans.PropertyChangeEvent
import javax.swing.JTable
import org.codehaus.groovy.runtime.MethodClosure

import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceResolvable
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.context.NoSuchMessageException



class GraspContext {

    static String lookup(String key) {
        try {
            return messageSource.getMessage(key)
        } catch(NoSuchMessageException nsme) {
            return key
        }
    }

    private static final String DEFAULT_I18N_FILE = 'messages'
    private static MessageSource messageSource
    private static List defaultPropnames = 'title value text'.tokenize()
    private static Map defaultConversion = [read: {it}, write: {it}, prop: 'value']

    static synchronized MessageSource getMessageSource() {
        if(!messageSource) messageSource = new ResourceBundleMessageSource()
        messageSource
    }

    static setupI18n(String[] basenames = null) {
        if(!basenames) basenames = [DEFAULT_I18N_FILE] as String[]        
        if(!basenames.find{it == DEFAULT_I18N_FILE}) basenames = [DEFAULT_I18N_FILE] + basenames
        getMessageSource().basenames = basenames as String[]

        MetaClass mc = MessageSource.metaClass
        mc.getMessage = {String message, List args, String defaultMessage, Locale locale ->
            messageSource.getMessage(message, args as Object[], defaultMessage, locale)
        }
        mc.getMessage = {String message, List args, String defaultMessage ->
            messageSource.getMessage(message, args as Object[], defaultMessage, GraspLocale.instance.locale)
        }
        mc.getMessage = {String message, List args, Locale locale ->
            messageSource.getMessage(message, args as Object[], locale)
        }
        mc.getMessage = {String message, List args ->
            messageSource.getMessage(message, args as Object[], GraspLocale.instance.locale)
        }
        mc.getMessage = {String message, Object[] args, String defaultMessage ->
            messageSource.getMessage(message, args, defaultMessage, GraspLocale.instance.locale)
        }
        mc.getMessage = {String message, Object[] args ->
            messageSource.getMessage(message, args, GraspLocale.instance.locale)
        }
        mc.getMessage = {MessageSourceResolvable resolvable ->
            messageSource.getMessage(resolvable, GraspLocale.instance.locale)
        }
    }

    static useBinding(Store store) {
        //todo dk: reset EMC after use...
        // MetaMethod before = Object.metaClass.getMetaMethod("methodMissing", [String, Object] as Class[])

        Object.metaClass.bind = { PresentationModelSwitch pmRef, Closure target ->
            // println pmRef
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
                } else if(extra.viewProperty) {
                    propname = extra.viewProperty
                } else {
                    propname = defaultPropnames.find { view.hasProperty(it) }
                }
                assert propname, "unable to retrieve property name from $defaultPropnames for $view"
            } else {                                        // attribute supplied in map
                propname = extra.keySet().find { extra[it] in Attribute }
                assert propname, "cannot find a property with attribute to use in binding"
                attribute = extra[propname]
            }

            def propertyName = convert.prop
            // update view on attribute value change
            def update = { view[propname] = convert.read(attribute[propertyName]) }
            attribute.addPropertyChangeListener(propertyName, update as PropertyChangeListener)
            update() // initial update needed

            // update attribute on view action
            def actions = convert.on?.tokenize()
            update = { attribute[propertyName] = convert.write(view[propname]) }
            if (!actions && view.respondsTo("addActionListener")) {   // default
                view.actionPerformed = update
            } else if (!actions && view.respondsTo("addPropertyChangeListener")) {   // default
                view.addPropertyChangeListener(propertyName, update as PropertyChangeListener)
            }
            for (action in actions) { view[action] = update }
            return view
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
