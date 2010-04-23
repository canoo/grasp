package com.canoo.grasp

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import org.codehaus.groovy.runtime.MethodClosure
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceResolvable
import org.springframework.context.NoSuchMessageException
import org.springframework.context.support.ResourceBundleMessageSource

class Grasp {

    static void initialize() {
         enhanceClasses()
        // additional stuff that must happen before anything else
    }

    private static boolean enhacerHasRun = false

    static void enhanceClasses() {
        synchronized (Grasp) {
            if (!enhacerHasRun) {
                enhacerHasRun = true
            } else {
                return
            }
        }

        MetaClass mc = MessageSource.metaClass

        mc.getMessage = {String message ->
            getMessageSource().getMessage(message, [] as Object[], GraspLocale.instance.locale)
        }
        mc.getMessage = {String message, List args, String defaultMessage, Locale locale ->
            getMessageSource().getMessage(message, args as Object[], defaultMessage, locale)
        }
        mc.getMessage = {String message, List args, String defaultMessage ->
            getMessageSource().getMessage(message, args as Object[], defaultMessage, GraspLocale.instance.locale)
        }
        mc.getMessage = {String message, List args, Locale locale ->
            getMessageSource().getMessage(message, args as Object[], locale)
        }
        mc.getMessage = {String message, List args ->
            getMessageSource().getMessage(message, args as Object[], GraspLocale.instance.locale)
        }
        mc.getMessage = {String message, Object[] args, String defaultMessage ->
            getMessageSource().getMessage(message, args, defaultMessage, GraspLocale.instance.locale)
        }
        mc.getMessage = {String message, Object[] args ->
            getMessageSource().getMessage(message, args, GraspLocale.instance.locale)
        }
        mc.getMessage = {MessageSourceResolvable resolvable ->
            getMessageSource().getMessage(resolvable, GraspLocale.instance.locale)
        }
    }

    static String lookup(String key) {
        try {
            return getMessageSource().getMessage(key, [] as Object[], GraspLocale.instance.locale)
        } catch (NoSuchMessageException nsme) {
            return key
        }
    }

    private static final String DEFAULT_I18N_FILE = 'messages'
    private static MessageSource messageSource
    private static List defaultPropnames = 'title value text'.tokenize()
    private static Map defaultConversion = [read: {it}, write: {it}, prop: 'value']

    static synchronized MessageSource getMessageSource() {
        if (!messageSource) messageSource = new ResourceBundleMessageSource()
        messageSource
    }

    static setupI18n(String[] basenames = null) {
        if (!basenames) basenames = [] as String[]
        println basenames
        // if (!basenames.find {it == DEFAULT_I18N_FILE}) basenames = [DEFAULT_I18N_FILE, *basenames]
        getMessageSource().basenames = basenames as String[]
    }

    static useBinding(Store store) {
        //todo dk: reset EMC after use...
        // MetaMethod before = Object.metaClass.getMetaMethod("methodMissing", [String, Object] as Class[])

        Object.metaClass.bind = {PresentationModelSwitch pmRef, Closure target ->
            // println pmRef
            def view = delegate

            def update = {PropertyChangeEvent e ->
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
                } else if (extra.viewProperty) {
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

            // does this thing validate? If so, wire in some validation listeners
            if (extra.validateOn && attribute) {
                def validateActions = convert.validateOn?.tokenize()
                def validateFunction = { enforceValidation(attribute) }
                if (!validateActions && view.respondsTo("addActionListener")) {   // default
                    view.actionPerformed = validateFunction
                } else if (!validateActions && view.respondsTo("addPropertyChangeListener")) {   // default
                    view.addPropertyChangeListener(propertyName, validateFunction as PropertyChangeListener)
                }
                for (action in validateActions) { view[action] = validateFunction }
            }
            return view
        }

        Object.metaClass.onSwitch = {PresentationModelSwitch pm, Closure callback = null ->
            def caller = delegate
            callback = callback ?: {it.enabled = pm.available() }
            def onSelectedPMChanged = {e -> callback caller }
            pm.addPropertyChangeListener onSelectedPMChanged as PropertyChangeListener
            callback caller
        }
    }

    static void enforceValidation(Attribute attribute) {
        def domainModel = attribute.model
        if (domainModel.properties.containsKey("constraints")) {
            Closure constraints = domainModel.constraints
            def delegate = new ConstraintsCapture()
            constraints.delegate = delegate
            constraints.setResolveStrategy Closure.DELEGATE_ONLY
            constraints()
            def constraintList = delegate.constraints[attribute.propertyName]
            constraintList?.each {
                if (it.size) {
                    Range range = it.size
                    if (!range.containsWithinBounds(attribute.value.size())) {
                        addErrorToPresentationModel(domainModel, attribute)
                    } else {
                        removeErrorFromPresentationModel(domainModel, attribute)
                    }
                }
                if (it.match) {
                    if (!attribute.value.toString().matches(it.match)) {
                        addErrorToPresentationModel(domainModel, attribute)
                    } else {
                        removeErrorFromPresentationModel(domainModel, attribute)
                    }
                }
            }
        }

    }
    static void addErrorToPresentationModel(domainModel, Attribute attribute) {
        def newError = new com.canoo.grasp.demo.domain.Error(id: domainModel.getClass().getName() + "." + attribute.propertyName, source: attribute)
        if (attribute.hasProperty("presentationModel")) {
            def newErrorsList = [] as Set
            newErrorsList.addAll attribute.presentationModel.errors.value
            newErrorsList.add newError
            attribute.presentationModel.errors.value = newErrorsList
        }
    }
    static void removeErrorFromPresentationModel(domainModel, Attribute attribute) {
        def newError = new com.canoo.grasp.demo.domain.Error(id: domainModel.getClass().getName() + "." + attribute.propertyName, source: attribute)
        def newErrorsList = [] as Set
        newErrorsList.addAll attribute.presentationModel.errors.value
        newErrorsList.remove newError
        attribute.presentationModel.errors.value = newErrorsList
    }
    
    static void enforceValidation(AttributeSwitch attribute) {
        enforceValidation(attribute.getValue())
    }
}

class ConstraintsCapture implements GroovyInterceptable {
    Map constraints = [:]
    def invokeMethod(String name, args) {
        constraints."$name" = args
    }
}
