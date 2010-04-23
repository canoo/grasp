package com.canoo.grasp.demo

import groovy.swing.SwingBuilder
import javax.swing.WindowConstants
import com.canoo.grasp.demo.domain.Book
import com.canoo.grasp.demo.domain.Publisher

import com.canoo.grasp.Grasp

import com.canoo.grasp.demo.domain.Environment

import com.canoo.grasp.demo.components.GraspEditorFactory
import com.canoo.grasp.demo.pm.BookPM
import com.canoo.grasp.demo.pm.EnvironmentPM

Grasp.initialize()
Grasp.useBinding()

Book gina = new Book(title: "gina", isbn: "0123456789", author: null, publishDate: new Date(), publisher: new Publisher(name: "publisher"))
BookPM bookPM = new BookPM(model: gina)
Environment environment = new Environment(locale: Locale.GERMAN)
EnvironmentPM environmentPM = new EnvironmentPM(model: environment)

SwingBuilder builder = new SwingBuilder()
builder.registerFactory('graspEditor', new GraspEditorFactory())

def frame = builder.frame(defaultCloseOperation: WindowConstants.EXIT_ON_CLOSE, title: "How to use a default editors") {
    panel {
        vbox {
            hbox {
                label "Default String: "
                graspEditor(bookPM.title, columns: 20).bind bookPM.title, on: "keyReleased"
            }
            hbox {
                label "Bound String: "
                graspEditor(bookPM.title, columns: 20).bind bookPM.title, on: "keyReleased"
            }
            hbox() {
                label "Default Date: "
                graspEditor(bookPM.publishDate)
            }
            hbox {
                label "Default Locale: "
                graspEditor(environmentPM.locale)
            }
            hbox {
                label "Date With Locale: "
                graspEditor(bookPM.publishDate, locale: environmentPM.locale)
            }
            hbox {
                label "Default Boolean: "
                graspEditor(bookPM.inPrint)
            }
        }

    }
}
frame.pack()
frame.visible = true