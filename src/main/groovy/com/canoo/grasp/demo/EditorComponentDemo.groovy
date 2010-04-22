package com.canoo.grasp.demo

import groovy.swing.SwingBuilder
import javax.swing.WindowConstants
import com.canoo.grasp.demo.domain.Book
import com.canoo.grasp.demo.domain.Publisher

import com.canoo.grasp.GraspContext

import java.text.DateFormat
import com.canoo.grasp.demo.components.DateEditorFactory

Book gina = new Book(title: "gina", isbn: "0123456789", author: null, publishDate: new Date(), publisher: new Publisher(name: "publisher"))
BookPM bookPM = new BookPM(model: gina)

GraspContext.useBinding()


SwingBuilder builder = new SwingBuilder()
builder.registerFactory('dateEditor', new DateEditorFactory())

def dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.ENGLISH)
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
                languageGroup = buttonGroup();
                radioButton(text:"German", buttonGroup:languageGroup, selected:true)
                radioButton(text:"English", buttonGroup:languageGroup)
            }
            hbox {
                label "Date and Locale: "
                dateEditor(bookPM.publishDate/*, locale: applicationContext.locale*/)
            }
        }

    }
}
frame.pack()
frame.visible = true