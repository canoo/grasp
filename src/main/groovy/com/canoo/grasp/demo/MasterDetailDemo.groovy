package com.canoo.grasp.demo

import com.canoo.grasp.GraspContext
import com.canoo.grasp.PresentationModelSwitch
import com.canoo.grasp.Store
import com.canoo.grasp.demo.domain.Book
import com.canoo.grasp.demo.domain.Publisher
import groovy.swing.SwingBuilder
import static javax.swing.ListSelectionModel.SINGLE_SELECTION
import static javax.swing.WindowConstants.EXIT_ON_CLOSE

Store store = new Store()
GraspContext.useBinding(store)

["Groovy in Action", "Griffon in Action", "Grails in Action"].eachWithIndex { it, idx ->
    final Publisher publisher = new Publisher(name: "Manning $idx")
    Book book = new Book(title: it, isbn: "0123456789-$idx", publisher: publisher)
    BookPM bookPM = new BookPM(model: book)
    store.save bookPM
}

List list = BookPM.list()
def selectedBook = new PresentationModelSwitch(new BookPM())

def frame = new SwingBuilder().frame(defaultCloseOperation: EXIT_ON_CLOSE, pack: true) {
    vbox {
        scrollPane {
            def master = table(selectionMode: SINGLE_SELECTION) {
                tableModel(list: list) {
                    closureColumn header: "Title",
                            read:  { pm -> pm.title.value },
                            write: { pm, value -> pm.title.value = value  }
                    closureColumn header: "ISBN",
                            read:  { pm -> pm.isbn.value }
                }
            }
            master.syncWith selectedBook
            master.syncList BookPM
        }
        hstrut 20 // detail view
        label selectedBook.title.description
        textField(columns: 20).bind selectedBook.title, on: "keyReleased"
        label selectedBook.isbn.description
        textField(columns: 20).bind selectedBook.isbn, on: "keyReleased"

        hbox {   // buttons
            button label: "top", actionPerformed: { selectedBook.adaptee = list.first() }
            button label: "new", actionPerformed: {
                BookPM newPM = new BookPM(model: new Book(title: 'new', publisher: new Publisher()))
                store.save newPM
                selectedBook.adaptee = newPM
            }
            //button("remove", actionPerformed: { selectedBook.delete() }).onSwitch selectedBook //todo: not reliable
        }
    }
}
frame.visible = true