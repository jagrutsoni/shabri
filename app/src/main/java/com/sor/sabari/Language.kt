package com.sor.sabari

data class Language(

    val symbol: String ="",
    val name: String ="",
    var isChecked: Boolean = true ,
  //  var checkedState by remember { mutableStateOf(true) }
)
