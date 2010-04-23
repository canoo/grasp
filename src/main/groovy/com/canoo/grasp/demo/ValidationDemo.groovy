package com.canoo.grasp.demo

import com.canoo.grasp.Grasp
import com.canoo.grasp.GraspLocale
import com.canoo.grasp.demo.components.GraspEditorFactory
import com.canoo.grasp.demo.domain.Author
import com.canoo.grasp.demo.domain.Book
import com.canoo.grasp.demo.domain.Publisher
import com.canoo.grasp.demo.pm.BookPM
import groovy.swing.SwingBuilder
import java.awt.Color
import javax.swing.WindowConstants

Grasp.initialize()
Grasp.useBinding()
Grasp.setupI18n(["com.canoo.grasp.demo.messages"] as String[])

Book aBook = new Book(title: "The Singularity is Near", isbn: "0123456789", author: new Author(name: "Ray Kurzweil"), publishDate: new Date(), publisher: new Publisher(name: "New World Press"))
BookPM bookPM = new BookPM(model: aBook)

SwingBuilder builder = new SwingBuilder()
builder.registerFactory('graspEditor', new GraspEditorFactory())

def frame = builder.frame(defaultCloseOperation: WindowConstants.EXIT_ON_CLOSE, title: "Validation Demo") {
    panel {
        vbox {
            hbox {
                label "Title: "
                graspEditor(bookPM.title, columns: 20).bind bookPM.title, on: "keyReleased",
                        validation: { errors, view ->
                            view.background = (errors ? Color.PINK : Color.WHITE)
                        },
                        validateOn: "keyReleased focusLost"  // when does validation occur?
            }
            hbox {
                label "ISBN: "
                graspEditor(bookPM.isbn, columns: 20).bind bookPM.isbn, on: "keyReleased",
                        validation: { errors, view ->
                            view.background = (errors ? Color.PINK : Color.WHITE)
                        },
                        validateOn: "keyReleased focusLost"  // when does validation occur?
            }
            hbox {
                // bind a generic editor to the Spring Errors interface that all PMs have
                // get notification when validation succeeds fails
                graspEditor(bookPM.errors)
            }
            hbox {
                button('Change Locale', actionPerformed: {
                    if (GraspLocale.instance.locale == Locale.GERMAN) {
                        GraspLocale.instance.locale = Locale.US
                    } else {
                        GraspLocale.instance.locale = Locale.GERMAN
                    }

                })
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
Only inform widget when the error has to do with his attribute
*/