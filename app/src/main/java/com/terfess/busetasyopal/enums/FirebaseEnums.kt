package com.terfess.busetasyopal.enums

enum class FirebaseEnums {
    ERROR_CONNECTION,          // Error de conexión a la red
    ERROR_NOT_EXISTS,          // El recurso no existe en la base de datos
    ERROR_AUTH,                // Error general de autenticación
    NO_EXIST_OR_DISABLED,      // El recurso no existe o está deshabilitado
    ERROR_CREDENTIAL,          // Error de credenciales (por ejemplo, credenciales inválidas)
    ERROR_INVALID_USER,        // El usuario no es válido o no existe
    ERROR_USER_COLLISION,      // Conflicto de usuario (ya existe un usuario con los mismos datos)
    ERROR_DATABASE,            // Error relacionado con la base de datos
    ERROR_UNKNOWN,             // Error desconocido
    ROUTE_ALREADY_EXISTS       // La ruta ya existe en la base de datos
}
