package com.terfess.busetasyopal.listas_datos

import com.terfess.busetasyopal.modelos_dato.DatosPrimariosRuta


class ListaRutas {
    companion object {
        //esta lista es simplificada para mostrar en recycler view
        val busetaRuta = listOf(
            DatosPrimariosRuta(
                2,
                "La Bendición, Llano Lindo, Calle 40, Centro, Horo, Estancia, Sena, Unitropico, Av Primera, La Bendicion"
            ),
            DatosPrimariosRuta(
                3,
                "Llano lindo, Central de Abastos, Alkosto, Unicentro, Clinica Casanare, EDS La Carpa, Llano Lindo"
            ),
            DatosPrimariosRuta(
                6,
                "Barrio Sueño Real, Estadio Atalayas, Calle 40, LACOR IPS SAS, La Herradura, Centro, Bomberos, Super Mio, Barrio Sueño Real"
            ),
            DatosPrimariosRuta(
                7,
                "Villa David, Las Americas, Nacua, Alkosto, Centro, I.E Carlos Lleras, Villa David"
            ),
            DatosPrimariosRuta(
                8,
                "Coliseo Villa Lucia, Parque Llano Vargas, Hospital Luis Londoño, Centro, Horo, I.E Carlos Lleras"
            ),
            DatosPrimariosRuta(
                9,
                "El Silencio, Alkosto, Unicentro, Centro, Horo, Técnico Ambiental, Los Ocobos"
            ),
            DatosPrimariosRuta(
                10,
                "Villa David, Aeropuerto Alcaravan, La Herradura, Centro, Unicentro, Sena, Unitropico, Las Americas"
            ),
            DatosPrimariosRuta(
                11,
                "Villa David, Terminal, Olimpica, Clinica Casanare, Horo, Centro, Las Americas"
            ),
            DatosPrimariosRuta(
                13,
                "La Bendicion, Central de Abastos, EDS La Carpa, Bomberos, Centro, Calle 24, Llano Lindo, La Bendicion"
            ),
        )


        //esta ruta es para buscar sitios especialmente al momento de filtrar/buscar
        val busetaSitios = listOf(
            /*DatosPrimariosRuta(
                1,
                "Sin sitios--","---","---","---","---","---","---",
            ),*/
            DatosPrimariosRuta(
                2,
                "La Bendición, Llano lindo, San Marcos, Avenida Primera, El Silencio, 7 de Agosto, Estadio Santiago Atalayas, " +
                        "Unitropico, Sena, 20 de Julio, Bomberos, Cusiana Gas, Estancia, Centro, Famedic, La Herradura, " +
                        "I.E Manuela Beltran, Conjunto Casa Blanca 1, Llano Lindo, La Bendicion."
            ),
            DatosPrimariosRuta(
                3,
                "Llano Lindo, Barrio San Jorge, Central de Abastos, Almacenes Paraiso, Bomberos, Creadimotos Honda, " +
                        "Banco Mundo Mujer, Alkosto, CC Hobo, Unicentro, Clinica Casanare, Famedic, Gobernación, La Estancia, " +
                        "Clinica Simalink, La Carpa, Molinos Arroz Casanare, El Progreso 2, Llano Lindo."
            ),
            /*DatosPrimariosRuta(4, "Sin sitios---","---","---","---","---","---","---"),
            DatosPrimariosRuta(
                5,
                "Llano Lindo, San Jorge, Veterinaria Vet Center, La Campiña, Casa de la Cultura, Triada, Alcaldia, La Herradura, Estancia, Alcaldia, La Carpa, Megacolegio, Llano Lindo.","---","---","---","---","---","---"
            ),*/
            DatosPrimariosRuta(
                6,
                "Barrio Bellavista, Acentamiento La Victoria, Barrio Cataluña, Estadio Santiago Atalayas, SuperMio, Super Metro, " +
                        "Colegio Paraiso, Bomberos, La Herradura, Parque Santander, La Estancia, Carrera 19, Centro, La Herradura, " +
                        "Coliseo Casiquiare, Barrio Cataluña, Sede Sitcol, Barrio Sueño Real"
            ),
            DatosPrimariosRuta(
                7,
                "Villa David, Las Americas, Casa de la Justicia, Hospital Juan Luis Londoño, " +
                        "Alkosto, SAO Olimpica, Terminal, Gobernación, La Estancia, Centro, " +
                        "La Herradura, I.E Carlos Lleras Restrepo, Colegio Tereza de Calcuta, Villa David"
            ),
            DatosPrimariosRuta(
                8,
                "Coliseo Villa Lucia, Parque Llano Vargas, Hospital Juan Luis Londoño, " +
                        "Olimpica, Terminal, Bomberos, Parque El Resurgimiento, La Herradura, La Estancia, " +
                        "I.E Luis Hernandez Vargas, HORO, Gobernacion, La Placita Campesina, I.E Carlos Lleras Restrepo"
            ),
            DatosPrimariosRuta(
                9,
                "El Silencio, Barrio 7 de Agosto, Megacolegio Progreso II, Barrio Bellavista, " +
                        "Colegio Tecnico Ambiental, Hospital Central de Yopal, Exito, Alkosto, CC Hobo, " +
                        "CC Unicentro, La Estancia, HORO, I.E Luis Hernandez Vargas, Gobernacion, " +
                        "Clinica Casanare, La Placita Campesina, Alkosto, Glorieta Calle 40, Ocobos"
            ),
            DatosPrimariosRuta(
                10,
                "Las Americas, ITEY, Parque Intra, La Herradura, " +
                        "Clinica Casanare, Gobernacion, La Estancia, Centro, Sena, Unitropico, " +
                        "Colegio Tereza de Calcuta, Villa David"
            ),
            DatosPrimariosRuta(
                11,
                "Getsemani, Villa David, Las Americas, Parque Barrio El Oasis, Terminal de Transporte, Mascotilandia, " +
                        "Clinica Casanare, RapiRoy, Hospital HORO, Patinodromo, I.E Luis Hernandez Vargas, La Estancia, " +
                        "Insulab Diagnostic, Canchas Multigol, Las Americas, Cementerio de Yopal, Getsemani"
            ),
            /*DatosPrimariosRuta(12, "Sin sitios---","---","---","---","---","---","---"),*/
            DatosPrimariosRuta(
                13, "La Bendicion, Llano Lindo, Central de Abastos, " +
                        "Servientrega Principal, Hotel Capitolio del Llano, Bomberos Yopal, " +
                        "La Herradura, La Estancia, Centro, Brasero, Museo Centro Historico, EDS La Carpa, " +
                        "Molinos Arroz Casanare, Postobon S.A, Llano Lindo, La Bendición"
            ),
        )
    }
}