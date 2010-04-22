package com.canoo.grasp.demo

import groovy.swing.SwingBuilder
import javax.swing.WindowConstants
import com.canoo.grasp.demo.domain.Book
import com.canoo.grasp.demo.domain.Publisher

import com.canoo.grasp.GraspContext
import org.jdesktop.swingx.JXDatePicker
import javax.swing.SwingConstants
import java.awt.event.ActionListener
import java.text.DateFormat

Book gina = new Book(title: "gina", isbn: "0123456789", author: null, publishDate: new Date(), publisher: new Publisher(name: "publisher"))
BookPM bookPM = new BookPM(model: gina)

GraspContext.useBinding()

SwingBuilder builder = new SwingBuilder()
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
                def picker = new JXDatePicker(new Date())
                picker.bind(bookPM.publishDate, on: "actionPerformed focusLost keyReleased", field: picker.&date)
                widget(picker)
            }

            //dateEditor(bookPM.publishDate, locale: applicationContext.locale)


           // view(bookPM.publishDate, locale: applicationContext.locale)
        }

    }
}
frame.pack()
frame.visible = true