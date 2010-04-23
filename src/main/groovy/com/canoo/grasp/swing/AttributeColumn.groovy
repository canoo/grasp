package com.canoo.grasp.swing

import javax.swing.table.TableColumn
import com.canoo.grasp.Attribute
import com.canoo.grasp.PresentationModel
import com.canoo.grasp.AttributeSwitch
import com.canoo.grasp.GraspLocale
import java.beans.PropertyChangeListener

class AttributeColumn extends TableColumn {
    private Closure binding
    Closure read = {attr -> attr.value }
    Closure write = {attr, value -> attr.value = value }
    private Boolean editable = null

    private final Class presentationModelClass

    AttributeColumn(Class presentationModelClass) {
        this.presentationModelClass = presentationModelClass
    }

    private static Attribute normalize(attr) {
        attr in AttributeSwitch ? attr.attribute : attr
    }

    String name() {
        setHeaderValue(rebind(PresentationModel.fetchPrototype(presentationModelClass)).label)
        getHeaderValue()
        // rebind(PresentationModel.fetchPrototype(presentationModelClass)).label
    }

    Class type() {
        rebind(PresentationModel.fetchPrototype(presentationModelClass)).valueType
    }

    void setBind(Closure binding) {
        this.binding = binding
        // name()
    }

    void setEditable(boolean b) {
        editable = b
    }

    def getValue(PresentationModel pm) {
        read(rebind(pm))
    }

    void setValue(PresentationModel pm, value) {
        if (isEditable(pm)) write(rebind(pm), value)
    }

    boolean isEditable(PresentationModel pm) {
        boolean readOnly = rebind(pm).readOnly
        if (readOnly) return false
        if (editable != null) return editable
        return true
    }

    private Attribute rebind(PresentationModel pm) {
        normalize(binding(pm))
    }
}
