package com.canoo.grasp.demo

import com.canoo.grasp.Grasp
import com.canoo.grasp.demo.components.DateEditorFactory
import com.canoo.grasp.demo.domain.Book
import com.canoo.grasp.demo.domain.Environment
import com.canoo.grasp.demo.domain.Publisher
import groovy.swing.SwingBuilder
import java.text.DateFormat
import javax.swing.WindowConstants

Grasp.initialize()
Grasp.useBinding()

Book gina = new Book(title: "gina", isbn: "0123456789", author: null, publishDate: new Date(), publisher: new Publisher(name: "publisher"))
Environment environment = new Environment(locale: Locale.GERMAN)

BookPM bookPM = new BookPM(model: gina)
EnvironmentPM environmentPM = new EnvironmentPM(model: environment)

SwingBuilder builder = new SwingBuilder()
builder.registerFactory('dateEditor', new DateEditorFactory())

def dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.GERMAN)
def frame = builder.frame(defaultCloseOperation: WindowConstants.EXIT_ON_CLOSE, title: "How to use an editor component") {
    panel {
        vbox {
            hbox() {
                label "Date: "
                textField(columns: 10).bind (bookPM.publishDate,
                        read:{dateFormat.format(it)},
                        write:{dateFormat.parse(it)},on:"keyReleased")

            }
            hbox {
                label "Locale: "
                textField(columns: 10).bind(environmentPM.locale, on:"keyReleased", write: { new Locale(it) })
            }
            hbox {
                label "Date and Locale: "
                dateEditor(bookPM.publishDate, locale: environmentPM.locale)
            }
        }

    }
}
frame.pack()
frame.visible = true