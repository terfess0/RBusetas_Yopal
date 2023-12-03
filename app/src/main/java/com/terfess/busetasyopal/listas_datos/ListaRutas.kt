package com.terfess.busetasyopal.listas_datos

import com.terfess.busetasyopal.modelos_dato.DatosPrimariosRuta


class ListaRutas {
    companion object {
        //esta lista es simplificada para mostrar en recycler view
        val busetaRuta = listOf(
            DatosPrimariosRuta(
                2,
                "La Bendición, Llano Lindo, Calle 40, Centro, Horo, Estancia, Sena, Unitropico, Av Primera, La Bendicion",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "Cada 10 minutos",
                "4:44 am - 8:00 pm",
                "4:55 am - 7:30 pm",
                "4:55 am - 7:00 pm"
            ),
            DatosPrimariosRuta(
                3,
                "Llano lindo, Central de Abastos, Alkosto, Unicentro, Clinica Casanare, EDS La Carpa, Llano Lindo",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "Cada 10 minutos",
                "5:08 am - 7:00 pm",
                "5:30 am - 7:00 pm",
                "6:00 am - 7:00 pm"
            ),
            DatosPrimariosRuta(
                6,
                "Barrio Sueño Real, Estadio Atalayas, Calle 40, LACOR IPS SAS, La Herradura, Centro, Bomberos, Super Mio, Barrio Sueño Real",
                "Cada 8 minutos",
                "Cada 10 minutos",
                "Cada 12 minutos",
                "5:26 am - 7:00 pm",
                "5:40 am - 7:00 pm",
                "6:00 am - 7:00 pm"

            ),
            DatosPrimariosRuta(
                7,
                "Villa David, Las Americas, Nacua, Alkosto, Centro, I.E Carlos Lleras, Villa David",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "Cada 12 minutos",
                "7:20 am - 7:00 pm",
                "5:40 am - 7:00 pm",
                "6:00 am - 7:00 pm"
            ),
            DatosPrimariosRuta(
                8,
                "Coliseo Villa Lucia, Parque Llano Vargas, Hospital Luis Londoño, Centro, Horo, I.E Carlos Lleras",
                "Cada 8 minutos",
                "Cada 10 minutos",
                "Cada 12 minutos",
                "5:15 am – 7:00 pm",
                "5:40 am – 7:00 pm",
                "6:00 am – 7:00 pm"
            ),
            DatosPrimariosRuta(
                9,
                "El Silencio, Alkosto, Unicentro, Centro, Horo, Técnico Ambiental, Los Ocobos",
                "Cada 7 minutos",
                "Cada 8 minutos",
                "Cada 10 minutos",
                "5:15 am - 7:00 pm",
                "5:40 am - 7:00 pm",
                "6:00 am - 7:00 pm"
            ),
            DatosPrimariosRuta(
                10,
                "Villa David, Aeropuerto Alcaravan, La Herradura, Centro, Unicentro, Sena, Unitropico, Las Americas",
                "Cada 8 minutos",
                "Cada 10 minutos",
                "Cada 12 minutos",
                "5:20 am - 7:00 pm",
                "5:40 am - 7:00 pm",
                "6:00 am - 7:00 pm"
            ),
            DatosPrimariosRuta(
                11,
                "Villa David, Terminal, Olimpica, Clinica Casanare, Horo, Centro, Las Americas",
                "Cada 8 minutos",
                "Cada 10 minutos",
                "Cada 12 minutos",
                "5:30 am - 7:00 pm",
                "5:40 am - 7:00 pm",
                "6:00 am - 7:00 pm"
            ),
            DatosPrimariosRuta(
                13,
                "La Bendicion, Central de Abastos, EDS La Carpa, Bomberos, Centro, Calle 24, Llano Lindo, La Bendicion",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "Cada 10 minutos",
                "4:40 am - 8:00 pm",
                "4:50 am - 7:30 pm",
                "4:50 am - 7:00 pm"
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
                "-La Bendición, Llano lindo, San Marcos, Avenida Primera, El Silencio, Calle 40 #1, 7 de Agosto, Calle 40 #5, SuperMio Calle 40, Estadio Santiago Atalayas, Unitropico Transversal 15, Sena, Coliseo 20 de Julio, Carrera 18, Sisben, Bomberos, Cusiana Gas, La Ñapa, Transversal 18 #14, Estancia, Carrera 19, Centro, Famedic Nueva EPS, La Herradura, El Brasero, I.E Manuela Beltran, Calle 26, Carrera 18, Calle 35, Conjunto Casa Blanca 1, Glorieta Calle 40 #5, Avenida Primera, Llano Lindo, La Bendicion.",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "Cada 10 minutos",
                "4:44 am - 8:00 pm",
                "4:55 am - 7:30 pm",
                "4:55 am - 7:00 pm"
            ),
            DatosPrimariosRuta(
                3,
                "Llano Lindo, Cancha chavinave, Barrio San Jorge, Central de Abastos, Glorieta Calle 30, Almacenes Paraiso, \n" +
                        "Carrera 14, Bomberos, Creadimotos Honda, Banco Mundo Mujer, Alkosto, Carrera 29, Centro Comercial Hobo, Unicentro, Clinica Casanare, Famedic, Bancolombia, Gobernacion, La Estancia, LingoYes, Clinica Simalink, Calle 24, La Carpa-Petrobras, Carrera 5, Molinos Arroz Casanare, El Progreso 2, Llano Lindo.",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "Cada 10 minutos",
                "5:08 am - 7:00 pm",
                "5:30 am - 7:00 pm",
                "6:00 am - 7:00 pm"
            ),
            /*DatosPrimariosRuta(4, "Sin sitios---","---","---","---","---","---","---"),
            DatosPrimariosRuta(
                5,
                "Llano Lindo, San Jorge, Veterinaria Vet Center, La Campiña, Casa de la Cultura, Triada, Alcaldia, La Herradura, Estancia, Alcaldia, La Carpa, Megacolegio, Llano Lindo.","---","---","---","---","---","---"
            ),*/
            DatosPrimariosRuta(
                6,
                "Carrera 13, Calle 11, Barrio Bellavista, Acentamiento La Victoria, Barrio Cataluña, Calle 42, Estadio Santiago Atalayas, Calle 40, SuperMio, Transversal 7, Super Metro, Calle 30, Colegio Paraiso, Carrera 12, Fitness People Gym, Calle 24, Bomberos, Yamaha, Carrera 20, La Herradura, Calle 15, Carrera 24, Calle 7, Parque Santander, La Estancia, Carrera 19, Centro, La Herradura, Calle 24, Carrera 12b, Coliseo Casiquiare, Calle 30, Transversal 7, Calle 40, Barrio Cataluña, Calle 44, Sede Sitcol, Barrio Sueño Real",
                "Cada 8 minutos",
                "Cada 10 minutos",
                "Cada 12 minutos",
                "5:26 am - 7:00 pm",
                "5:40 am - 7:00 pm",
                "6:00 am - 7:00 pm"

            ),
            DatosPrimariosRuta(
                7,
                "Villa David, Las Americas, Serpet JR Y CIA S.A.S, Casa de la Justicia, Hospital Juan Luis Londoño, Calle 24, Alkosto, SAO Olimpica, Terminal, Carrera 23, Calle 7, Gobernación de Casanare, La Estancia, Centro, La Herradura, Calle 24, I.E Carlos Lleras Restrepo, Colegio Tereza de Calcuta, Villa David",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "Cada 12 minutos",
                "7:20 am - 7:00 pm",
                "5:40 am - 7:00 pm",
                "6:00 am - 7:00 pm"

            ),
            DatosPrimariosRuta(
                8,
                "Coliseo Villa Lucia, Parque Llano Vargas, Carrera 43, Calle 30, Hospital Juan Luis Londoño, Calle 24, Olimpica, Terminal, Bomberos, Carrera 16, Parque El Resurgimiento, La Herradura, La Estancia, I.E Luis Hernandez Vargas, Hospital HORO, Gobernacion de Casanare, Carrera 23, Super La Placita Campesina, I.E Carlos Lleras Restrepo, Calle 30",
                "Cada 8 minutos",
                "Cada 10 minutos",
                "Cada 12 minutos",
                "5:15 am – 7:00 pm",
                "5:40 am – 7:00 pm",
                "6:00 am – 7:00 pm"
            ),
            DatosPrimariosRuta(
                9,
                "El Silencio, Calle 40 #1, Barrio 7 de Agosto, EDS Terpel, Megacolegio Progreso II, Barrio Bellavista, Colegio Tecnico Ambiental, Hospital Central de Yopal, Calle 30, Exito Yopal, Alkosto, CC El Hobo, CC Unicentro, Calle 9, La Estancia, Hospital HORO, I.E Luis Hernandez Vargas, Gobernacion de Casanare, Calle 10, Clinica Casanare, La Placita Campesina, Alkosto, Glorieta Calle 40, Ocobos",
                "Cada 7 minutos",
                "Cada 8 minutos",
                "Cada 10 minutos",
                "5:15 am - 7:00 pm",
                "5:40 am - 7:00 pm",
                "6:00 am - 7:00 pm"
            ),
            DatosPrimariosRuta(
                10,
                "Las Americas, Parex Resources, ITEY, Gimnasio BHG, Parque Intra, Carrera 20, La Herradura, Calle 12, Clinica Casanare, Calle 7, Gobernacion, La Estancia, Centro, Carrera 20, Sena, Unitropico, Colegio Tereza de Calcuta, Villa David",
                "Cada 8 minutos",
                "Cada 10 minutos",
                "Cada 12 minutos",
                "5:20 am - 7:00 pm",
                "5:40 am - 7:00 pm",
                "6:00 am - 7:00 pm"
            ),
            DatosPrimariosRuta(
                11,
                "Las Americas, Parque Barrio El Oasis, Carrera 23, Terminal de Transporte, Carrera 26, Mascotilandia, Clinica Casanare, RapiRoy, Calle 9, Hospital HORO, Patinodromo, I.E Luis Hernandez Vargas, La Estancia, Calle 10, Insulab Diagnostic, Carrera 25, Canchas Multigol, Las Americas",
                "Cada 8 minutos",
                "Cada 10 minutos",
                "Cada 12 minutos",
                "5:30 am - 7:00 pm",
                "5:40 am - 7:00 pm",
                "6:00 am - 7:00 pm"
            ),
            /*DatosPrimariosRuta(12, "Sin sitios---","---","---","---","---","---","---"),*/
            DatosPrimariosRuta(
                13, "La Bendicion, Llano Lindo, Glorieta Calle 50, Central de Abastos, Glorieta calle 30, Servientrega Principal, Calle 24, Hotel Capitolio del Llano, Bomberos Yopal, Yamaha, Carrera 20, La Herradura, Calle 9, La Estancia, Centro, Brasero, Calle 24, Museo Centro Historico, EDS La Carpa, Molinos Arroz Casanare, Postobon S.A, Transversal 5, Llano Lindo, La Bendición.zqx2w+ ",
                "Cada 8 minutos",
                "Cada 8 minutos",
                "Cada 10 minutos",
                "4:40 am - 8:00 pm",
                "4:50 am - 7:30 pm",
                "4:50 am - 7:00 pm"
            ),
        )
    }
}