# Sección 4: Spring Boot: Test de Servicios (Mockito)

# Dependencia de Swagger usado en este proyecto

```
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-boot-starter</artifactId>
    <version>3.0.0</version>
</dependency>
```

### Objeto JSON del API Rest de este proyecto

```
http://localhost:8080/v2/api-docs
```

### API Rest del proyecto en html (swagger-ui)

```
http://localhost:8080/swagger-ui/index.html
```

# Covertura de código (Code Coverage)

Para ver la covertura de las pruebas realizadas, es decir, qué porcentaje de todo el código
estamos probando, levantaremos el proyecto de la siguiente manera

```
Click secundario en el proyecto/ Run 'All Tests' with Coverage
```

Esperamos que se ejecuten todos los test. Finalizado la prueba, en la parte derecha del editor saldrán los resultados  
Podemos exportar los resultados en documentos HTML, clickando en la opción

```
Generate Coverage Report...
```

# Excluir clase de pruebas mediante tag

Para excluir alguna clase, podemos usar tags, es decir anotar cada clase de prueba
con algún tag, por ejemplo

```
@Tag(value = "integracion_rest_template")  <<<<<------------------------ Tag
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CuentaControllerTestRestTemplateTest {
    ....
    ....
}
```

Luego en **Edit configurations...**   
seleccionar del combo box la opción **tags**  
y en la casilla del costado agregar el nombre del tag a excluir con el signo
de admiración (!) **!integracion_rest_template**

# Ejecutar pruebas unitarias desde la consola de windows

Ir mediante consola a la raíz del proyecto y ejecutar el siguiente comando

```
mvnw test
```

Ahora si ocurren problemas porque hay conflictos entre pruebas de integración,
podemos excluir alguna prueba de integración con los tags

```
mvnw test -Dgroups="!integracion_web_client"
```

## Solucionar al error de caracteres en cmd

Puede que al ejecutar el test mediante consola nos muestre que no detecta la
codificación de caracteres. Para eso, ir al archivo **import.sql** tanto del
proyecto como del test, ambos archivos import, iremos seleccionando uno por uno.

```
Ir a File / File Properties / File Encoding
```

Seleccionar el **ISO-8859-1** y click en hacer conversión  
Luego volvemos a ejecutar la prueba desde cmd y debería haberse solucionado

**IMPORTANTE:** Si se va a volver a hacer las pruebas desde el ide de Intellij idea,
debemos volver a convertir los archivos a sus caracteres originales (UTF-8) sino
las pruebas ejecutadas desde el IDE fallarán