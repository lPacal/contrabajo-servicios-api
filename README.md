# Contrabajo - Microservicio de Servicios (MS_Servicios)

Este microservicio es el motor principal de negocio de la plataforma **Contrabajo**. Se encarga de la gestión del ciclo de vida de las ofertas de trabajo publicadas por los trabajadores, la solicitud de citas por parte de los clientes, los estados del servicio y el sistema de valoraciones. Además, implementa un sistema de seguridad distribuida validando tokens inter-microservicios.

---

## Estado actual de integración

* El catálogo base de estados ya se repuebla de forma idempotente al iniciar el servicio.
* El backend expone los catálogos de categorías y tipos de precio para el front.
* La respuesta de ofertas ya entrega los ids de categoría y tipo de precio para sincronizar edición y detalle.
* El microservicio sigue alineado con la rama `integracion` y con los snapshots backend de esta iteración.

## Tecnologías y Arquitectura

El microservicio está construido bajo un enfoque de escalabilidad, seguridad y desacoplamiento:
* **Lenguaje:** Java 21
* **Framework:** Spring Boot 3.4.x
* **Seguridad:** Spring Security con validación de JWT y RBAC (Role-Based Access Control)
* **Gestión de Configuración:** Spring Cloud Config (Client) - Endpoint: `http://localhost:8888`
* **Persistencia:** Spring Data JPA
* **Base de Datos:** SQL Server
* **Productividad:** Lombok

## Estructura del Proyecto

El código sigue una estructura de capas estándar para facilitar el mantenimiento:
* `model/`: Entidades JPA que representan las tablas de la base de datos (OfertaServicio, CitaServicio, CategoriaServicio, Estado, etc.).
* `repository/`: Interfaces que gestionan la persistencia de datos.
* `service/`: Lógica de negocio, validaciones transaccionales y reglas de publicación.
* `controller/`: Endpoints REST protegidos para la interacción con el frontend.
* `config/`: Configuraciones de seguridad (SecurityConfig), filtros de JWT, interceptores y poblado inicial de datos (DataSeeder).
* `utils/`: Utilidades criptográficas y extracción de claims (JwtUtil).

## Configuración del Entorno

Este servicio utiliza un archivo `application.yml` configurado con la dependencia del servidor de configuración. Para iniciar la aplicación localmente, es **obligatorio** tener el servidor de configuración (`config_server`) activo. 

Además, para que las funcionalidades de seguridad y creación de ofertas operen correctamente, este microservicio requiere comunicación en tiempo real con `MS_Usuarios` (para la validación del estado del token en base de datos).
