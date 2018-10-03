package com.natife.arproject

import com.google.ar.core.Anchor
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment

class ObjectForList(anchor: Anchor, renderable: Renderable, arFragment: ArFragment) {

    var anchorNode: Anchor? = anchor
    var renderable: Renderable? = renderable
    var arFragment: ArFragment? = arFragment
}