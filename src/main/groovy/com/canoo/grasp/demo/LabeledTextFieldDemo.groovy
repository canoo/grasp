package com.canoo.grasp.demo

import groovy.swing.SwingBuilder
import javax.swing.WindowConstants
import com.canoo.grasp.demo.domain.Book
import com.canoo.grasp.demo.domain.Publisher

import com.canoo.grasp.Grasp
import com.canoo.grasp.demo.pm.BookPM

Book gina = new Book(title: "gina", isbn: "0123456789", author: null, publisher: new Publisher(name: "publisher"))
BookPM bookPM = new BookPM(model: gina)


Grasp.initialize()
Grasp.useBinding()

SwingBuilder builder = new SwingBuilder()
def frame = builder.frame(defaultCloseOperation: WindowConstants.EXIT_ON_CLOSE) {
    panel {
        vbox {
            label bookPM.title.description
            label().bind text:bookPM.title, read:{it.reverse()}
            label "default"
            textField(columns: 20).bind bookPM.title
            label "actionPerformed focusLost"
            textField(columns: 20).bind bookPM.title, on: "actionPerformed focusLost"
            label "keyReleased"
            textField(columns: 20).bind bookPM.title, on: "keyReleased"
        }
    }
}
frame.bind bookPM.title, read:{it*2}
frame.pack()
frame.visible = true