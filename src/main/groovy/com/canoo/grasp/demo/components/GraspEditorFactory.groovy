package com.canoo.grasp.demo.components

/**
 * Handles creating generic components. 
 */
class GraspEditorFactory extends AbstractFactory {

    List<GraspEditor> editors = [
            new DateEditorFactory(),
            new StringEditor(),
            new LocaleEditor(),
            new BooleanEditor(),
            new ErrorsEditor() ]
    
    GraspEditorFactory(List<GraspEditor> additionalBindings = []) {
        editors.addAll(additionalBindings)
    }
    
    Object newInstance(FactoryBuilderSupport factoryBuilderSupport, Object name, Object value, Map attributes) {
        def candidates = editors.findAll {GraspEditor it -> it.canHandle(value, attributes) }
        if (!candidates) throw new IllegalStateException("Could not find matching editor for $value $attributes")
        if (candidates.size() != 1) throw new IllegalStateException("Too many matching editors for for $value $attributes. Found $candidates")

        candidates[0].newInstance(factoryBuilderSupport, name, value, attributes)
    }
}






