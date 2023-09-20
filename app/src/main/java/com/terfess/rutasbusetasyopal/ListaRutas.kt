package com.terfess.rutasbusetasyopal


class ListaRutas {
    companion object {
        //esta lista es simplificada para mostrar en recycler view
        val busetaRuta = listOf(
            DatosRuta(
                2,
                "La Bendicion, Horo, Estancia, Bomberos",
                "2,000"
            ),
            DatosRuta(
                3,
                "Llano lindo, Alkosto, Clinica Casanare, Llano Lindo",
                "2,000"
            ),
            DatosRuta(
                6,
                "Estadio Atalayas, La Herradura, LACOR IPS SAS,La Herradura",
                "2,000"
            ),
            DatosRuta(
                7,
                "Villa David, Olimpica, Gobernación,IE Carlos Lleras",
                "2,000"
            ),
            DatosRuta(
                8,
                "Villa Lucia, ESE Juan Luis Londoño, Alkosto, Horo",
                "2,000"
            ),
            DatosRuta(
                9,
                "El Silencio, Hospital Central, CC Unicentro, Técnico Ambiental",
                "2,000"
            ),
            DatosRuta(10, "Sin sitios---", "2,000"),
            DatosRuta(11, "Sin sitios---", "2,000"),
            DatosRuta(13, "Sin sitios---", "2,000"),
        )

        //esta ruta es completa para buscar sitios y demas, especialmente al momento de filtrar/buscar
        val busetaSitios = listOf(
            DatosRuta(
                1,
                "Sin sitios--",
                "2,000"
            ),
            DatosRuta(
                2,
                "-La Bendicion, Estadio, Sena, Sisben, Contraloria, Horo, Estancia, Resurgimiento, Herradura, Bomberos, San Marcos, La Bendición.",
                "2,000"
            ),
            DatosRuta(
                3,
                "Llano lindo, Weatherford, Plaza de Mercado, Bomberos, Alkosto, Clinica Casanare, La Carpa, San Jorge, Llano Lindo.",
                "2,000"
            ),
            DatosRuta(4, "Sin sitios---", "2,000"),
            DatosRuta(
                5,
                "Llano Lindo, San Jorge, Veterinaria Vet Center, La Campiña, Casa de la Cultura, Triada, Alcaldia, La Herradura, Estancia, Alcaldia, La Carpa, Megacolegio, Llano Lindo.",
                "2,000"
            ),
            DatosRuta(
                6,
                "Estadio Atalayas, Super Mio, Super Metro, La Herradura, LACOR IPS SAS, Centro Social, Gobernación, Parque Estancia, La Herradura, Kairos Gym, Barrio Cataluña.",
                "2,000"
            ),
            DatosRuta(
                7,
                "Villa David, Las Americas, Casa de la Justicia, Alkosto, Olimpica, Gobernación, La Herradura, Olimpica, Alkosto, IE Carlos Lleras, Villa David.",
                "2,000"
            ),
            DatosRuta(
                8,
                "Villa Lucia, ESE Juan Luis Londoño, Alkosto, I.E Manuela Beltran, Alcaldia, Horo, Gobernación, Corporinoquia, I.E Simon Bolivar",
                "2,000"
            ),
            DatosRuta(
                9,
                "El Silencio, Megacolegio Progreso, San Mateo, Hospital Central, Exito, Alkosto, CC Unicentro, Clinica Casanare, Horo, Smarfit, Técnico Ambiental, Av. Primera, El Silencio.",
                "2,000"
            ),
            DatosRuta(10, "Sin sitios---", "2,000"),
            DatosRuta(11, "Sin sitios---", "2,000"),
            DatosRuta(12, "Sin sitios---", "2,000"),
            DatosRuta(13, "Sin sitios---", "2,000"),
        )
    }
}