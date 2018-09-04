package com.natife.arproject

class Model {

    companion object {
        val TEXT_TYPE = 0
        val FOLDER_TYPE = 1
        val IMAGE_TYPE = 2
    }

    var type: Int = 0
    var image: Int = 0
    var text: String = ""
    var vrImage: String = ""

    constructor(type: Int, text: String, image: Int, vrImage: String) {
        this.type = type
        this.text = text
        this.image = image
        this.vrImage = vrImage
    }
}