package com.example.cocktailproject

import java.io.Serializable

data class Cocktail(var ctName:String, var ctPhoto: String, var ctDetail: List<CocktailDetail>) : Serializable {

}
