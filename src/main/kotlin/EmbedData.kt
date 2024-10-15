package org.example

import java.awt.Color

data class EmbedData(val title:String? = null, val description:String? = null, var color: Color = Color.BLACK, var fields:Map<String,String>? = null)
