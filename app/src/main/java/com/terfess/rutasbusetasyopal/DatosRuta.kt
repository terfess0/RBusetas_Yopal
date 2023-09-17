package com.terfess.rutasbusetasyopal

data class DatosRuta(
    val numRuta: Int,
    val sitios: String,
    val precio: String
    /*val numRuta: Task<DataSnapshot>,
    val sitios: Task<DataSnapshot>,
    val precio: Task<DataSnapshot>

    val numRuta: DatabaseReference = FirebaseDatabase.getInstance("https://rutasbusetas-default-rtdb.firebaseio.com/").getReference("features/0/geometry/ruta2/numRuta")*/
)
