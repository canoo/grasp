package com.canoo.grasp.demo

import groovy.swing.SwingBuilder
import static javax.swing.WindowConstants.EXIT_ON_CLOSE
import com.canoo.grasp.demo.domain.Book
import com.canoo.grasp.demo.domain.Publisher

import com.canoo.grasp.GraspContext
import com.canoo.grasp.Store

Book gina = new Book(
        title: "Groovy in Action",
        publisher: new Publisher(name: "Manning")
)
BookPM bookPM = new BookPM(model: gina)
def title = bookPM.title

GraspContext.useBinding(new Store())

def frame = new SwingBuilder().frame(defaultCloseOperation: EXIT_ON_CLOSE,
        pack: true,
        show:true) {
    panel(border: emptyBorder([10]*4)) {
        gridLayout cols: 2, rows:4
        label "Instant change"
        textField(columns:12).bind title, on: "keyReleased"
        label "Trigger change"
        textField().bind title, on: "actionPerformed focusLost"
        label "Reversed view"
        label().bind  title, read: { String bla -> bla.reverse() }
        button("Save").bind enabled: title, read: { title.dirty  }
        button("Undo").bind enabled: title,
                read: { title.dirty  },
                write:{ title.value = title.modelValue }
    }
}
frame.bind title