package com.canoo.grasp.demo

import com.canoo.grasp.GraspContext
import com.canoo.grasp.PresentationModelSwitch
import com.canoo.grasp.Store
import com.canoo.grasp.demo.domain.Book
import groovy.swing.SwingBuilder
import static javax.swing.ListSelectionModel.SINGLE_SELECTION
import static javax.swing.WindowConstants.EXIT_ON_CLOSE

GraspContext.useBinding()
Store store = new Store()

["Groovy in Action", "Griffon in Action", "Grails in Action"].each {
    Book book = new Book(title: it, isbn: "0123456789")
    BookPM bookPM = new BookPM(model: book)
    store.save bookPM
}

List list = BookPM.list()
def selectedBook = new PresentationModelSwitch(new BookPM())

def master
SwingBuilder builder = new SwingBuilder()
def frame = builder.frame(defaultCloseOperation: EXIT_ON_CLOSE) {
    vbox {
        scrollPane {
            master = table(selectionMode: SINGLE_SELECTION) {
                tableModel(list: list) {
                    closureColumn header: "title",
                            read: {pm -> pm.title.value},
                            write: {pm, value -> pm.title.value = value  }
                    closureColumn header: "isbn",
                            read: {pm -> pm.isbn.value}
                    closureColumn header: "author",
                            read: {pm -> pm.author.value}
                }
            }
            master.syncWith selectedBook
        }
        hstrut 20
        label selectedBook.title.description
        textField(columns: 20).bind selectedBook.title, on: "keyReleased"
        label selectedBook.isbn.description
        textField(columns: 20).bind selectedBook.isbn, on: "keyReleased"
        hbox {
            button label: "top", actionPerformed: { selectedBook.adaptee = list[0] }
            button label: "new", actionPerformed: {
                BookPM newPM = new BookPM(model: new Book(title: 'new'))
                store.save newPM
                master.model.fireTableRowsInserted list.size()-1, list.size()-1 // todo: avoid dependency
                selectedBook.adaptee = newPM
            }
            button("remove", actionPerformed: { selectedBook.delete() }).onSwitch selectedBook //todo: not reliable

        }
    }
}

frame.pack()
frame.visible = true