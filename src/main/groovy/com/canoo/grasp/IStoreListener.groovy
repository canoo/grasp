package com.canoo.grasp

interface IStoreListener {
    
    void added(PresentationModel pm)

    void deleted(PresentationModel pm)
}
