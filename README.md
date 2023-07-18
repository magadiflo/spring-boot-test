# Sección 4: Spring Boot: Test de Servicios (Mockito)

---

## Creación del proyecto desde [Spring Initializr](https://start.spring.io/)

Al proyecto creado desde Spring Initilizr le agregaremos una única dependencia **Spring Web**, finalmente nuestro
**pom.xml** quedaría así:

````xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.1</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.magadiflo.app</groupId>
    <artifactId>spring-boot-test</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>spring-boot-test</name>
    <description>Test con Spring Boot</description>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Incluye JUnit 5 y Mockito -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
````

Observamos que además de la dependencia **spring-boot-starter-web** que nosotros agregamos, se nos agregó en automático
otra dependencia: **spring-boot-starter-test**, ``esta dependencia incluye JUnit 5 y Mockito.``

## Creando las clases del modelo

Nuestras clases de modelo serán similares a las que creamos en la sección de **JUnit 5**. Por ahora solo crearemos las
clases que posteriormente usaremos para realizar las pruebas:

````java
public class InsufficientMoneyException extends RuntimeException {
    public InsufficientMoneyException(String message) {
        super(message);
    }
}
````

````java
public class Bank {
    private Long id;
    private String name;
    private int totalTransfers;

    /* constructors, getters, setters and toString() */
}
````

````java
public class Account {
    private Long id;
    private String person;
    private BigDecimal balance;

    /* constructors, getters, setters */

    public void debit(BigDecimal amount) {
        BigDecimal newBalance = this.balance.subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientMoneyException("Dinero insuficiente en la cuenta");
        }
        this.balance = newBalance;
    }

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    /* equals(), hasCode() and toString() */
}
````
