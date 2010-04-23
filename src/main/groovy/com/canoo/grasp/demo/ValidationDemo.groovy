package com.canoo.grasp.demo

import com.canoo.grasp.GraspContext
import com.canoo.grasp.demo.components.GraspEditorFactory
import com.canoo.grasp.demo.domain.Book
import com.canoo.grasp.demo.domain.Publisher
import groovy.swing.SwingBuilder
import javax.swing.WindowConstants

GraspContext.useBinding()

Book gina = new Book(title: "gina", isbn: "0123456789", author: null, publishDate: new Date(), publisher: new Publisher(name: "publisher"))
BookPM bookPM = new BookPM(model: gina)

SwingBuilder builder = new SwingBuilder()
builder.registerFactory('graspEditor', new GraspEditorFactory())

def frame = builder.frame(defaultCloseOperation: WindowConstants.EXIT_ON_CLOSE, title: "Validation Demo") {
    panel {
        vbox {
            hbox {
                // bind a generic editor to the Spring Errors interface that all PMs have
                // get notification when validation succeeds fails
                graspEditor(bookPM.errors)
            }
            hbox {
                label "Default String: "
                graspEditor(bookPM.title, columns: 20).bind bookPM.title,
                        on: "keyReleased",
                        validateOn: "keyRelease focusLost"  // when does validation occur?
            }
        }

    }
}
frame.pack()
frame.visible = true

/*
// bind a specific editor to the Errors and get notification when
// validation succeeds to fails.
graspEditor(bookPM.title).bind (
    validation: { errors, widget ->
        widget.setBackgroundColor( errors ? Color.RED : Color.WHITE )
    },
    validateOn: "keyRelease focusLost"
)

// all PMs have an Attribute errors
// all Attributes have an Errors

Story: Generic Validation Handler on Form
Domain model defines constraints
User Defines a generic error handler component (HTML label?)
User Defines a "validateOn" value (keyRelease focusLost)
Grasp enforces validation on those events
generic editor is invoked with Errors list (could be empty)

Story: Widget Specific Error Handling on Form
Domain model defines constraints
User defines a text widget with widget specific error handling
User defines a "validateOn" value (keyRelease focusLost)
Grasp enforces validation on those events
the widget is invoked with Errors list (could be empty) and a self-reference (for manipulation)
*/