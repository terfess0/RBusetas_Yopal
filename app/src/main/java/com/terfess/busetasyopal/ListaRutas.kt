package com.terfess.busetasyopal


class ListaRutas {
    companion object {
        //esta lista es simplificada para mostrar en recycler view
        val busetaRuta = listOf(
            /*DatosPrimariosRuta(
                1,
                "-----",
                "-----",
                "-----",
                "-----",
                "-----",
                "-----",
                "-----"
            ),*/
            DatosPrimariosRuta(
                2,
                "La Bendición, Horo, Estancia, Bomberos",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "4:52 am - 6:30 pm",
                "4:52 am - 6:20 pm",
                "5:20 am - 6:00 pm"
            ),
            DatosPrimariosRuta(
                3,
                "Llano lindo, Alkosto, Clinica Casanare, Llano Lindo",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "Cada 15 minutos",
                "5:15 am - 6:00 pm",
                "5:00 am - 5:00 pm",
                "5:00 am - 5:00 pm"
            ),
            DatosPrimariosRuta(
                6,
                "Estadio Atalayas, La Herradura, LACOR IPS SAS, La Herradura",
                "Cada 12 minutos",
                "Cada 12 minutos",
                "SIN SERVICIO",
                "5:30 am - 5:30 pm",
                "5:30 am - 1:00 pm",
                "SIN SERVICIO"
            ),
            DatosPrimariosRuta(
                7,
                "Villa David, Olimpica, Gobernación, IE Carlos Lleras",
                "Cada 12 minutos",
                "Cada 15 minutos",
                "Cada 20 minutos",
                "5:25 am - 6:00 pm",
                "6:00 am - 6:00 pm",
                "6:00 am - 4:00 pm"
            ),
            DatosPrimariosRuta(
                8,
                "Villa Lucia, ESE Juan Luis Londoño, Alkosto, Horo",
                "Cada 10 minutos",
                "Cada 10 minutos",
                "Cada 10 minutos",
                "5:20 am – 6:00 pm",
                "5:30 am – 6:00 pm",
                "6:00 am – 12:00 pm"
            ),
            DatosPrimariosRuta(
                9,
                "El Silencio, Hospital Central, CC Unicentro, Técnico Ambiental",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "5:10am - 6:30 pm",
                "5:10 am - 5:30 pm",
                "6:00 am – 5:00 pm"
            ),
            DatosPrimariosRuta(
                10,
                "El Silencio, Hospital Central, CC Unicentro, Técnico Ambiental",
                "Cada 15 minutos",
                "Cada 15 minutos",
                "SIN SERVICIO",
                "5:20 am - 5:00 pm",
                "6:00 am - 12:00 pm",
                "SIN SERVICIO"
            ),
            DatosPrimariosRuta(
                11,
                "El Silencio, Hospital Central, CC Unicentro, Técnico Ambiental",
                "12 - 20 minutos",
                "12 - 20 minutos",
                "SIN SERVICIO",
                "5:30 am - 5:30 pm",
                "5:30 am - 5:30 pm",
                "SIN SERVICIO"
            ),
            DatosPrimariosRuta(
                13,
                "El Silencio, Hospital Central, CC Unicentro, Técnico Ambiental",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "4:58 am - 6:10 pm",
                "5:30 am - 6:00 pm",
                "5:30 am - 6:00 pm"
            )
        )

        //esta lista es para buscar sitios, especialmente al momento de filtrar/buscar
        val busetaSitios = listOf(
            /*DatosRuta(
                1,
                "Sin sitios--","---","---","---","---","---","---",
            ),*/
            DatosPrimariosRuta(
                2,
                "-La Bendición, Estadio, Sena, Sisben, Contraloria, Horo, Estancia, Resurgimiento, Herradura, Bomberos, San Marcos, La Bendición.","Cada 8 minutos",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "4:52 am - 6:30 pm",
                "4:52 am - 6:20 pm",
                "5:20 am - 6:00 pm"
            ),
            DatosPrimariosRuta(
                3,
                "Llano lindo, Weatherford, Plaza de Mercado, Bomberos, Alkosto, Clinica Casanare, La Carpa, San Jorge, Llano Lindo.",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "Cada 15 minutos",
                "5:15 am - 6:00 pm",
                "5:00 am - 5:00 pm",
                "5:00 am - 5:00 pm"
            ),
            /*DatosRuta(4, "Sin sitios---","---","---","---","---","---","---"),
            DatosRuta(
                5,
                "Llano Lindo, San Jorge, Veterinaria Vet Center, La Campiña, Casa de la Cultura, Triada, Alcaldia, La Herradura, Estancia, Alcaldia, La Carpa, Megacolegio, Llano Lindo.","---","---","---","---","---","---"
            ),*/
            DatosPrimariosRuta(
                6,
                "Estadio Atalayas, Super Mio, Super Metro, La Herradura, LACOR IPS SAS, Centro Social, Gobernación, Parque Estancia, La Herradura, Kairos Gym, Barrio Cataluña.",
                "Cada 12 minutos",
                "Cada 12 minutos",
                "SIN SERVICIO",
                "5:30 am - 5:30 pm",
                "5:30 am - 1:00 pm",
                "SIN SERVICIO"

            ),
            DatosPrimariosRuta(
                7,
                "Villa David, Las Americas, Casa de la Justicia, Alkosto, Olimpica, Gobernación, La Herradura, Olimpica, Alkosto, IE Carlos Lleras, Villa David.",
                "Cada 12 minutos",
                "Cada 15 minutos",
                "Cada 20 minutos",
                "5:25 am - 6:00 pm",
                "6:00 am - 6:00 pm",
                "6:00 am - 4:00 pm"

            ),
            DatosPrimariosRuta(
                8,
                "Villa Lucia, ESE Juan Luis Londoño, Alkosto, I.E Manuela Beltran, Alcaldia, Horo, Gobernación, Corporinoquia, I.E Simon Bolivar",
                "Cada 10 minutos",
                "Cada 10 minutos",
                "Cada 10 minutos",
                "5:20 am – 6:00 pm",
                "5:30 am – 6:00 pm",
                "6:00 am – 12:00 pm"
            ),
            DatosPrimariosRuta(
                9,
                "El Silencio, Megacolegio Progreso, San Mateo, Hospital Central, Exito, Alkosto, CC Unicentro, Clinica Casanare, Horo, Smarfit, Técnico Ambiental, Av. Primera, El Silencio.",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "5:10am - 6:30 pm",
                "5:10 am - 5:30 pm",
                "6:00 am – 5:00 pm"

            ),
            DatosPrimariosRuta(10, "Sin sitios---",
                "Cada 15 minutos",
                "Cada 15 minutos",
                "SIN SERVICIO",
                "5:20 am - 5:00 pm",
                "6:00 am - 12:00 pm",
                "SIN SERVICIO"),
            DatosPrimariosRuta(11, "Sin sitios---",
                "Cada 12 - 20 minutos",
                "Cada 12 - 20 minutos",
                "SIN SERVICIO",
                "5:30 am - 5:30 pm",
                "5:30 am - 5:30 pm",
                "SIN SERVICIO"),
            /*DatosRuta(12, "Sin sitios---","---","---","---","---","---","---"),*/
            DatosPrimariosRuta(13, "Sin sitios---",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "4:58 am - 6:10 pm",
                "5:30 am - 6:00 pm",
                "5:30 am - 6:00 pm"),
        )
    }
}