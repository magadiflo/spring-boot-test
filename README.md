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

## Creando los repositorios

Repositorio para nuestro modelo **Account**:

````java
public interface IAccountRepository {
    List<Account> findAll();

    Optional<Account> findById(Long id);

    Account update(Account account);
}
````

Repositorio para nuestro modelo **Bank**:

````java
public interface IBankRepository {
    List<Bank> findAll();

    Optional<Bank> findById(Long id);

    Bank update(Bank bank);
}
````

Interfaz para la capa de servicio:

````java
public interface IAccountService {
    Optional<Account> findById(Long id);

    int reviewTotalTransfers(Long bancoId);

    BigDecimal reviewBalance(Long accountId);

    void transfer(Long bankId, Long accountIdOrigen, Long accountIdDestination, BigDecimal amount);
}
````

## Implementando la clase de servicio (Service)

````java
public class AccountServiceImpl implements IAccountService {
    private final IAccountRepository accountRepository;
    private final IBankRepository bankRepository;

    public AccountServiceImpl(IAccountRepository accountRepository, IBankRepository bankRepository) {
        this.accountRepository = accountRepository;
        this.bankRepository = bankRepository;
    }

    @Override
    public Optional<Account> findById(Long id) {
        return this.accountRepository.findById(id);
    }

    @Override
    public int reviewTotalTransfers(Long bancoId) {
        Bank bank = this.bankRepository.findById(bancoId)
                .orElseThrow(() -> new NoSuchElementException("No existe el banco buscado"));
        return bank.getTotalTransfers();
    }

    @Override
    public BigDecimal reviewBalance(Long accountId) {
        Account account = this.accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("No existe la cuenta buscada"));
        return account.getBalance();
    }

    @Override
    public void transfer(Long bankId, Long accountIdOrigen, Long accountIdDestination, BigDecimal amount) {
        Account accountOrigen = this.accountRepository.findById(accountIdOrigen)
                .orElseThrow(() -> new NoSuchElementException("No existe el id de la cuenta origen"));
        Account accountDestination = this.accountRepository.findById(accountIdDestination)
                .orElseThrow(() -> new NoSuchElementException("No existe el id de la cuenta destino"));

        accountOrigen.debit(amount);
        accountDestination.credit(amount);

        Bank bank = this.bankRepository.findById(bankId)
                .orElseThrow(() -> new NoSuchElementException("No existe el id del banco"));

        bank.setTotalTransfers(bank.getTotalTransfers() + 1);

        this.accountRepository.update(accountOrigen);
        this.accountRepository.update(accountDestination);
        this.bankRepository.update(bank);
    }
}
````

---

## Creando nuestros datos de prueba

Crearemos algunos datos de prueba para comenzar a realizar los test a las distintas capas.

````java
public class DataTest {
    public static Optional<Account> account001() {
        return Optional.of(new Account(1L, "Martín", new BigDecimal("2000")));
    }

    public static Optional<Account> account002() {
        return Optional.of(new Account(2L, "Alicia", new BigDecimal("1000")));
    }

    public static Optional<Bank> bank() {
        return Optional.of(new Bank(1L, "Banco de la Nación", 0));
    }
}
````

## Escribiendo nuestros tests con JUnit y Mockito

Antes de continuar, revisemos la clase de test que Spring Boot crea cuando creamos un nuevo proyecto de Spring Boot:

````java

@SpringBootTest
class SpringBootTestApplicationTests {
    @Test
    void contextLoads() {
    }
}
````

**DONDE**

- Por defecto nos crea una clase de prueba que tiene el nombre de la aplicación.
- Crea un método anotado con **@Test** que es una anotación de **JUnit 5** para indicarnos que será un método a testear.
- Anota la clase con **@SpringBootTest**, ¿Qué hace esta anotación?

**@SpringBootTest**

> Spring Boot proporciona esta anotación para las **pruebas de integración**. Esta anotación crea un contexto para la
> aplicación y carga el contexto completo de la aplicación.
>
> **@SpringBootTest** arranca el contexto completo de la aplicación, lo que significa que podemos usar el **@Autowired**
> para poder usar inyección de dependencia.
>
> **@SpringBootTest** inicia el servidor embebido, crea un entorno web y a continuación, permite a los métodos test
> realizar pruebas de integración.
>
> De forma predeterminada, **@SpringBootTest** no inicia un servidor, necesitamos agregar el atributo **webEnvironment**
> para refinar aún más cómo se ejecutan sus pruebas.
>
> Indica que la clase de prueba es una prueba de Spring Boot y proporciona una serie de características y
> configuraciones específicas para realizar **pruebas de integración** en una aplicación de Spring Boot.
>
> Al utilizar la anotación **@SpringBootTest**, se **cargará el contexto de la aplicación de Spring Boot completo** para
> la prueba. Esto significa que se inicializarán todos los componentes, configuraciones y dependencias de la aplicación,
> de manera similar a como se ejecutaría la aplicación en un entorno de producción. Esto permite realizar pruebas de
> integración más realistas, donde se pueden probar las interacciones y el comportamiento de la aplicación en un entorno
> similar al de producción.

Por lo tanto, como iniciaremos con **Pruebas Unitarias** eliminaremos esa clase de prueba creada automáticamente, para
dar paso a la creación de nuestra propia clase.

## Pruebas Unitarias para el service AccountServiceImpl - Mockeo manual

A continuación se muestra la creación de nuestra clase de prueba para el servicio **AccountServiceImpl** con la creación
de un test unitario:

````java
class AccountServiceImplUnitTest {

    IAccountRepository accountRepository;
    IBankRepository bankRepository;

    AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        this.accountRepository = mock(IAccountRepository.class);
        this.bankRepository = mock(IBankRepository.class);

        this.accountService = new AccountServiceImpl(this.accountRepository, this.bankRepository);
    }

    @Test
    void canTransferBetweenAccounts() {
        Long accountIdOrigen = 1L;
        Long accountIdDestination = 2L;
        Long bankId = 1L;

        when(this.accountRepository.findById(accountIdOrigen)).thenReturn(DataTest.account001());
        when(this.accountRepository.findById(accountIdDestination)).thenReturn(DataTest.account002());
        when(this.bankRepository.findById(bankId)).thenReturn(DataTest.bank());

        BigDecimal balanceOriginal = this.accountService.reviewBalance(accountIdOrigen);
        BigDecimal balanceDestination = this.accountService.reviewBalance(accountIdDestination);

        assertEquals(2000D, balanceOriginal.doubleValue());
        assertEquals(1000D, balanceDestination.doubleValue());

        this.accountService.transfer(bankId, accountIdOrigen, accountIdDestination, new BigDecimal("500"));

        balanceOriginal = this.accountService.reviewBalance(accountIdOrigen);
        balanceDestination = this.accountService.reviewBalance(accountIdDestination);

        assertEquals(1500D, balanceOriginal.doubleValue());
        assertEquals(1500D, balanceDestination.doubleValue());
    }
}
````

**NOTA**

- En el curso, el profesor **Andrés Guzmán** dejó la clase anotada con **@SpringBootTest**, pero según lo que investigué
  y coloqué la información en la parte superior, esta anotación es para **Pruebas de Integración**, es por eso que yo
  omito esa anotación, ya que ahora estamos en **pruebas unitarias.**
- Por el momento, **no hemos agregado ninguna anotación** sobre nuestra clase de prueba.
- En el método anotado con **@BeforeEach**, que es el método del ciclo de vida de **JUnit** estamos creando manualmente
  las instancias **mockeadas** de las dependencias de nuestra clase de servicio. Para ello usamos el método estático de
  Mockito: ``mock(IAccountRepository.class)`` y el ``mock(IBankRepository.class)``.
- Usamos las **dependencias mockeadas manualmente** para crear nuestro objeto de prueba, el cual requiere que se le pase
  por constructor las dependencias de cuenta y banco:
  ``new AccountServiceImpl(this.accountRepository, this.bankRepository)``.
- Como resultado tenemos, nuestras dos dependencias mockeadas: ``this.accountRepository`` y ``this.bankRepository`` y
  nuestra clase de servicio que es el que será sometido a los tests: ``this.accountService``.
- Sobre el método test, no hace falta explicar, es como lo hemos venido trabajando hasta ahora. Si ejecutamos esta clase
  de prueba, veremos que **el test pasará exitosamente**.

## Test Verify

En el test anterior no realizamos los verify, es importante también usarlos, porque con ellos nos aseguramos de que se
estén llamando los métodos mockeados el número de veces determinados:

````java
class AccountServiceImplUnitTest {

    IAccountRepository accountRepository;
    IBankRepository bankRepository;

    AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        this.accountRepository = mock(IAccountRepository.class);
        this.bankRepository = mock(IBankRepository.class);

        this.accountService = new AccountServiceImpl(this.accountRepository, this.bankRepository);
    }

    @Test
    void canTransferBetweenAccounts() {
        Long accountIdOrigen = 1L;
        Long accountIdDestination = 2L;
        Long bankId = 1L;

        when(this.accountRepository.findById(accountIdOrigen)).thenReturn(DataTest.account001());
        when(this.accountRepository.findById(accountIdDestination)).thenReturn(DataTest.account002());
        when(this.bankRepository.findById(bankId)).thenReturn(DataTest.bank());

        BigDecimal balanceOriginal = this.accountService.reviewBalance(accountIdOrigen);
        BigDecimal balanceDestination = this.accountService.reviewBalance(accountIdDestination);

        assertEquals(2000D, balanceOriginal.doubleValue());
        assertEquals(1000D, balanceDestination.doubleValue());

        this.accountService.transfer(bankId, accountIdOrigen, accountIdDestination, new BigDecimal("500"));

        balanceOriginal = this.accountService.reviewBalance(accountIdOrigen);
        balanceDestination = this.accountService.reviewBalance(accountIdDestination);

        assertEquals(1500D, balanceOriginal.doubleValue());
        assertEquals(1500D, balanceDestination.doubleValue());

        int total = this.accountService.reviewTotalTransfers(bankId);
        assertEquals(1, total);

        verify(this.accountRepository, times(3)).findById(accountIdOrigen);
        verify(this.accountRepository, times(3)).findById(accountIdDestination);
        verify(this.accountRepository, times(2)).update(any(Account.class));

        verify(this.bankRepository, times(2)).findById(bankId);
        verify(this.bankRepository).update(any(Bank.class));
    }
}
````

## Escribiendo tests assertThrow para afirmar que la excepción lanzada sea correcta

Crearemos un test para verificar que se esté lanzando nuestra excepción personalizada **InsufficientMoneyException**
cuando el monto a transferir sea mayor que el saldo disponible de la cuenta origen.

````java
class AccountServiceImplUnitTest {
    IAccountRepository accountRepository;
    IBankRepository bankRepository;

    AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        this.accountRepository = mock(IAccountRepository.class);
        this.bankRepository = mock(IBankRepository.class);

        this.accountService = new AccountServiceImpl(this.accountRepository, this.bankRepository);
    }

    @Test
    void willThrowExceptionWhenBalanceIsLessThanAmountToBeTransfer() {
        Long accountIdOrigen = 1L;
        Long accountIdDestination = 2L;
        Long bankId = 1L;

        when(this.accountRepository.findById(accountIdOrigen)).thenReturn(DataTest.account001());
        when(this.accountRepository.findById(accountIdDestination)).thenReturn(DataTest.account002());
        when(this.bankRepository.findById(bankId)).thenReturn(DataTest.bank());

        BigDecimal balanceOriginal = this.accountService.reviewBalance(accountIdOrigen);
        BigDecimal balanceDestination = this.accountService.reviewBalance(accountIdDestination);

        assertEquals(2000D, balanceOriginal.doubleValue());
        assertEquals(1000D, balanceDestination.doubleValue());

        InsufficientMoneyException exception = assertThrows(InsufficientMoneyException.class, () -> {
            this.accountService.transfer(bankId, accountIdOrigen, accountIdDestination, new BigDecimal("2500"));
        });

        assertEquals(InsufficientMoneyException.class, exception.getClass());

        balanceOriginal = this.accountService.reviewBalance(accountIdOrigen);
        balanceDestination = this.accountService.reviewBalance(accountIdDestination);

        assertEquals(2000D, balanceOriginal.doubleValue());
        assertEquals(1000D, balanceDestination.doubleValue());

        int total = this.accountService.reviewTotalTransfers(bankId);
        assertEquals(0, total);

        verify(this.accountRepository, times(3)).findById(accountIdOrigen);
        verify(this.accountRepository, times(3)).findById(accountIdDestination);
        verify(this.accountRepository, never()).update(any(Account.class));

        verify(this.bankRepository, times(1)).findById(bankId);
        verify(this.bankRepository, never()).update(any(Bank.class));
    }
}
````

Ahora, como estamos probando que lance la excepción vemos que algunos métodos mockeados no se van a ejecutar, por lo
tanto, el número de veces del **verify** va a cambiar.

**NOTA**

> Inicialmente, habíamos creado nuestra clase **DataTest** conteniendo métodos estáticos que nos devuelven valores para
> poder usarlos en nuestras pruebas. Pero **¿por qué tienen que ser métodos estáticos?**, si usamos los métodos
> estáticos, cada vez que lo llamemos nos va a devolver una nueva instancia de lo que esté devolviendo. Mientras que si
> los datos los hacemos atributos estáticos, podría suceder que en un método test modifiquemos el atributo y cuando se
> use en otro método test, no tendrá los valores originales, ya que fueron modificados, entonces para evitar ese
> problema fue que los datos de prueba los definimos dentro de métodos estáticos.

## Escribiendo test con los assertSame

Con el **assertSame()** verificamos que ambos objetos comparados sean el mismo.

````java
class AccountServiceImplUnitTest {

    IAccountRepository accountRepository;
    IBankRepository bankRepository;

    AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        this.accountRepository = mock(IAccountRepository.class);
        this.bankRepository = mock(IBankRepository.class);

        this.accountService = new AccountServiceImpl(this.accountRepository, this.bankRepository);
    }

    @Test
    void canVerifyThatTwoInstancesAreTheSame() {
        when(this.accountRepository.findById(1L)).thenReturn(DataTest.account001());

        Account account1 = this.accountService.findById(1L).get();
        Account account2 = this.accountService.findById(1L).get();

        assertSame(account1, account2);
        assertEquals("Martín", account1.getPerson());
        assertEquals("Martín", account2.getPerson());
        verify(this.accountRepository, times(2)).findById(1L);
    }
}
````

## Pruebas Unitarias para el service AccountServiceImpl - Mockeo con anotaciones de Mockito

Crearemos una nueva clase de prueba para nuestro servicio **AccountServiceImpl** al que le llamaremos
**AccountServiceImplWithMockitoAnnotationsUnitTest**. Esta nueva clase de prueba contendrá los mismos métodos test que
creamos en la clase de prueba **AccountServiceImplUnitTest**. ¿Y para qué estamos creando otra clase con los mismos
métodos?, pues en la clase **AccountServiceImplUnitTest creamos los mocks de las dependencias de manera manual** tal
como se muestra a continuación: ``mock(IAccountRepository.class)``, entonces lo que buscamos con esta nueva clase de
test es **crear los Mocks de otra manera, utilizando las anotaciones de Mockito**, de esta manera los mocks se crearán
de manera automática.

````java

@ExtendWith(MockitoExtension.class)             // (1)
class AccountServiceImplWithMockitoAnnotationsUnitTest {
    @Mock                                       // (2)
    IAccountRepository accountRepository;
    @Mock                                       // (3)
    IBankRepository bankRepository;
    @InjectMocks                                // (4)
    AccountServiceImpl accountService;

    @Test
    void canTransferBetweenAccounts() { /* omitted code */ }

    @Test
    void willThrowExceptionWhenBalanceIsLessThanAmountToBeTransfer() { /* omitted code */ }

    @Test
    void canVerifyThatTwoInstancesAreTheSame() { /*omitted code */ }
}
````

**DONDE**

- **(1)** por norma general, para generar los test unitarios sobre los servicios y/o componentes simplemente debemos de
  definir tests que funcionen con la extensión de Mockito **@ExtendWith(MockitoExtension.class)**. Esta anotación nos
  permite habilitar las anotaciones de mockito: **@Mock, @InjectMocks, @Spy, entre otras.**.
- **(2) y (3)** son anotaciones de mockito que nos permite **mockear** las dependencias sobre las que están anotadas. En
  nuestro caso, nos permite crear un mock del **IAccountRepository** y del **IBankRepository**.
- **(4)** nos permite inyectar los mocks anteriormente definidos: **IAccountRepository** y el **IBankRepository** en la
  instancia del **AccountServiceImpl**. ¡Importante!, el **@InjectMocks** tiene que estar anotada sobre una clase
  concreta.

Y eso es todo, si ejecutamos los test de nuestra nueva clase, veremos que todo seguirá funcionando como antes, pero esta
vez estamos creando los **Mocks** usando las anotaciones de **Mockito**.

## Pruebas Unitarias para el service AccountServiceImpl - Mockeo con anotaciones de Spring Boot

Creamos una nueva clase de prueba con los mismos test que hemos venido realizando hasta ahora, lo único que cambiará
serán las anotaciones que usaremos:

````java

@SpringBootTest
class AccountServiceImplWithSpringBootAnnotationsUnitTest {
    @MockBean
    IAccountRepository accountRepository;
    @MockBean
    IBankRepository bankRepository;
    @Autowired
    IAccountService accountService;

    @Test
    void canTransferBetweenAccounts() { /* omitted code */ }

    @Test
    void willThrowExceptionWhenBalanceIsLessThanAmountToBeTransfer() { /* omitted code */ }

    @Test
    void canVerifyThatTwoInstancesAreTheSame() { /* omitted code */ }
}
````

Anteriormente, ya habíamos explicado el uso de la anotación **@SpringBootTest**, donde decíamos que Spring Boot
proporciona esta anotación para las **pruebas de integración**. Esta anotación crea un contexto para la aplicación y
carga el contexto completo de la aplicación.

En este caso lo utilizaremos para realizar **pruebas unitarias**, pero más adelante determinaremos que lo mejor para
realizar pruebas unitarias a servicios y/o componentes será **hacer uso del Mockito.mock() o la anotación @Mock.**

**@MockBean**

Si hacemos uso de la anotación **@SpringBootTest** podemos hacer uso de la anotación de Spring Boot, el **@MockBean**.
La anotación **@MockBean** agrega objetos simulados al contexto de la aplicación de Spring (**@SpringBootTest crea ese
contexto para la aplicación**). El objeto mockeado reemplazará a cualquier bean existente del mismo tipo en el contexto
de la aplicación. Si no se define un bean del mismo tipo, se agregará uno nuevo. **En resumen, cuando utilicemos la
anotación @MockBean en un campo, el mock se inyectará en el campo, además de registrarse en el contexto de la
aplicación.**

**@Autowired**

Nos permite inyectar una implementación concreta en la instancia **IAccountService**, pero para eso necesitamos agregar
a la implementación concreta **AccountServiceImpl** la anotación **@Service**:

````java

@Service
public class AccountServiceImpl implements IAccountService {
    /* omitted code */
}
````

Listo, si ejecutamos nuestra nueva clase de test **AccountServiceImplWithSpringBootAnnotationsUnitTest** veremos que
se ejecutará exitosamente, con una observación: **Ahora se está creando un contexto para la aplicación**, es por esa
razón que cuando ejecutamos el test de dicha clase, vemos que en la consola aparece la presentación clásica de
Spring Boot.

## Pruebas unitarias a servicios: @Mock o Mockito.mock() vs @MockBean

> Cuando realicemos pruebas a nuestra clase de servicio o componente, lo recomendable sería utilizar lo concerniente a
> Mockito, ya sea **@Mock** o **Mockito.mock()**, puesto que estos test no levantan ningún contexto de Spring, por lo
> que su tiempo de ejecución es muy rápido. [Fuente: cloudAppi](https://cloudappi.net/testing-en-spring-boot/)

Mientras que, **@MockBean** anotado en un campo de una clase de prueba, Spring Boot creará automáticamente un
objeto simulado (mock) de la dependencia correspondiente y lo inyectará en la clase. Esto permite simular el
comportamiento de la dependencia y definir respuestas predefinidas para los métodos llamados durante la prueba.
``Generalmente, se usará esta anotación en otras capas de la aplicación, como el controlador.``

# Sección 5: Spring Boot: Test de Repositorios (DataJpaTest)

---

## Configurando el contexto de persistencia JPA y clases entities para test

Necesitamos agregar las dependencias de **h2 y jpa** a nuestro proyecto. Por el momento trabajaremos con una base de
datos en memoria para realizar los test, posteriormente trabajaré con una base de datos real, tan solo para ver su
funcionamiento y las configuraciones que se necesitan para eso, pero por el momento trabajaremos con **h2**:

````xml

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

Ahora, en nuestro directorio **/test** crearemos un subdirectorio llamado **/resources** similar al que tenemos en el
directorio **/main** y también le agregaremos **application.properties**. Este archivo de propiedades contendrá
configuraciones relacionadas a las pruebas realizadas en la aplicación.

**NOTA**
> En IntelliJ IDEA demos click derecho al directorio **/resource** creado y vamos a la opción de **Mark Directory as**,
> debemos observar que esté con: "Unmark as Test Resources Root". De esa manera confirmamos que dicho directorio **sí
> esta marcado como raíz de recursos de prueba.**

Configuramos nuestra base de datos **h2** en el **application.properties** de nuestro directorio **/test/resources**:

````properties
# Datasource
spring.datasource.url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=sa
spring.datasource.driver-class-name=org.h2.Driver
# Only development
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
````

**NOTA**

> La base de datos **h2** es una base de datos en memoria, solo se necesita agregar la dependencia en el pom.xml e
> inmediatamente se autoconfigura, es decir no necesitamos agregar configuraciones en el application.properties para que
> funcione, sino más bien, **por defecto se autoconfigura**, pero también podemos realizar configuraciones
> personalizadas como habilitar la consola h2 usando esta configuración: **spring.h2.console.enabled=true**, entre
> otros.
>
> **¿y esas configuraciones del datasource que puse en el application.properties?** Son configuraciones que permiten
> crear un datasource para cualquier base de datos, en este caso usé dichas configuraciones para configurar la base de
> datos h2 tal como lo hubiera realizado configurando una base de datos real. Pero las puse con la finalidad de que más
> adelante usaré una base de datos real, entonces solo tendría que cambiar los datos de conexión.
>
> **Conclusión:** si solo usaré la base de datos **h2** para realizar las pruebas, tan solo agregando la dependencia de
> h2 ya estaría configurada el **datasource**, pero si luego usaré una base de datos real para las pruebas, podría usar
> la configuración que puse para tan solo cambiar los datos de conexión.

Ahora, toca modificar nuestras clases de modelo para **convertirlos en entities (clases de persistencia de hibernate)**:

````java

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String person;
    private BigDecimal balance;
    /* constructors, getters, setters, debit, credit, equals, hashCode and toString */
}
````

````java

@Entity
@Table(name = "banks")
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "total_transfers")
    private int totalTransfers;
    /*constructors, getters, setters and toString */
}
````

Creamos dentro del **test/resources/** un archivo llamado **import.sql** para poder poblar nuestras tablas con datos de
prueba:

````sql
INSERT INTO banks(name, total_transfers) VALUES('Banco de la Nación', 0);
INSERT INTO accounts(person, balance) VALUES('Martín', 2000);
INSERT INTO accounts(person, balance) VALUES('Alicia', 1000);
````

## Modificando nuestros repositorios con Spring Data JPA

Como ahora utilizaremos Hibernate/JPA con la dependencia de Spring Data JPA modificaremos nuestras interfaces de
repositorio para extender de las interfaces propias del JPA. Es importante notar que los métodos que teníamos
inicialmente, los eliminamos, ya que estos vienen dentro de la interfaz **CrudRepository** el cual es extendido por
**JpaRepository**:

````java
public interface IBankRepository extends JpaRepository<Bank, Long> {

}
````

````java
public interface IAccountRepository extends JpaRepository<Account, Long> {

}
````

Como eliminamos los métodos que usábamos inicialmente, nuestros test unitarios de la clase de servicio van a fallar, ya
que ellos están haciendo uso aún de los métodos que creamos inicialmente. Lo que haremos para solucionarlos será ahora
usar los métodos que jpa nos proporciona:

````java
class AccountServiceImplUnitTest {
    void canTransferBetweenAccounts() {
        /* omitted code */
        verify(this.accountRepository, times(2)).save(any(Account.class));  //<--se cambió el update por el save
        verify(this.bankRepository).save(any(Bank.class));                  //<--se cambió el update por el save
    }
}
````

Como observamos, en todos los test solo cambiaremos el **update()** que teníamos inicialmente por al **save()**. El
mismo cambio aplicamos en la clase de implementación del servicio **AccountServiceImpl**:

````java

@Service
public class AccountServiceImpl implements IAccountService {
    /* omitted code */

    @Override
    public void transfer(Long bankId, Long accountIdOrigen, Long accountIdDestination, BigDecimal amount) {
        /* omitted code */

        this.accountRepository.save(accountOrigen);         // Cambió el update por el save
        this.accountRepository.save(accountDestination);    // Cambió el update por el save
        this.bankRepository.save(bank);                     // Cambió el update por el save
    }
}
````

## Agregando consulta personalizada en el repositorio de cuenta con Spring Data JPA

Agregaremos dos consultas personalizadas en el repositorio de cuentas:

````java
public interface IAccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByPerson(String person);

    @Query(value = "SELECT a FROM Account AS a WHERE a.person = ?1")
    Optional<Account> findAccountByPerson(String person);
}
````

Si observamos ambas consultas definidas hacen lo mismo, la diferencia está en la forma cómo se construyen, mientras que
la primera consulta usa el **nombre del método** como palabras claves para realizar la consulta, la segunda forma usa
la anotación **@Query**, donde definimos manualmente una consulta JPQL.

## Pruebas de Integración con @DataJpaTest

Antes de continuar, mencionaré el **por qué** se llama **Prueba de Integración** al usar la anotación **@DataJpaTest**
y no **Prueba Unitaria**. Recordemos que en el tutorial de **Amigoscode** de test con Spring Boot se usa también esta
anotación para testear el repositorio, pero **solo para testear los métodos que nosotros agreguemos a la interfaz**,
a esa acción el tutor lo llama prueba unitaria del repositorio.

**[Fuente: stack overflow](https://stackoverflow.com/questions/23435937/how-to-test-spring-data-repositories)**

> Para abreviar, no hay forma de realizar pruebas unitarias de los repositorios Spring Data JPA razonablemente por una
> razón simple: es demasiado engorroso simular todas las partes de la API JPA que invocamos para arrancar los
> repositorios. De todos modos, las pruebas unitarias no tienen mucho sentido aquí, ya que normalmente no está
> escribiendo ningún código de implementación usted mismo, **por lo que las pruebas de integración son el enfoque más
razonable.**
>
> Si lo piensa, **no hay código que escriba para sus repositorios, por lo que no hay necesidad de escribir pruebas
> unitarias.** Simplemente, no hay necesidad de hacerlo, ya que puede confiar en nuestra base de prueba para detectar
> errores básicos. Sin embargo, definitivamente **se necesitan pruebas de integración** para probar dos aspectos de su
> capa de persistencia, porque son los aspectos relacionados con su dominio:
>
> * Entity mappings
> * Query semantics

Puedo añadir además, que cuando hablamos de **Pruebas Unitarias** nos referimos a la verificación o comprobación del
correcto funcionamiento de las **piezas de código de manera individual, en forma aislada.** Mientras que, una
**Prueba de integración** se realiza para verificar la **interacción entre distintos módulos,** y si recordamos nosotros
agregamos una dependencia para usar base de datos, es decir, las pruebas que haremos **interactuarán con un módulo de
base de datos**, por lo tanto, podemos decir que las pruebas a crear serán **Pruebas de integración**.

### [La anotación @DataJpaTest](https://docs.spring.io/spring-boot/docs/1.5.2.RELEASE/reference/html/boot-features-testing.html)

**@DataJpaTest** se puede usar si desea probar aplicaciones JPA. De forma predeterminada, configurará una base de datos
incrustada en memoria, buscará clases @Entity y configurará repositorios de Spring Data JPA, es decir que **solo probará
los componentes de la capa de repositorio/persistencia.**

La anotación **@DataJpaTest** no cargará los otros beans en el ApplicationContext: **@Component, @Controller, @Service y
beans anotados.**

La anotación **@DataJpaTest** habilita la configuración específica de JPA para la prueba y se **utilizan las inyecciones
de dependencia para acceder a los componentes de JPA (repositorios) que se desean probar.**

Los **datos de prueba de JPA son transaccionales** y **retroceden al final de cada prueba de forma predeterminada.** Es
decir, cada método de prueba anotado con @DataJpaTest se ejecutará dentro de una transacción, y al final de cada prueba,
la transacción se revertirá automáticamente, evitando así que los cambios de la prueba afecten la base de datos.

> Esto se diferencia de las pruebas unitarias, donde se aísla una unidad de código (como un método o clase) y se prueban
> sus funcionalidades de forma independiente, **sin depender de clases externas o bases de datos.**

**IMPORTANTE**

> **Únicamente deberíamos probar los métodos que nosotros creemos (los métodos personalizados para hacer consultas a la
> bd)**. Obviamente, como extendemos de alguna interfaz de Spring Data Jpa, tendremos muchos métodos, como el: save(),
> findById(), findAll(), etc... pero dichos métodos son métodos que ya vienen probados, puesto que nos lo proporciona
> Spring Data Jpa.
>
> Por tema de aprendizaje, en este curso probamos los métodos propios de las interfaces de Spring Data Jpa.

## Creando nuestra prueba de Integración con @DataJpaTest

Probaremos nuestro repositorio **IAccountRepository**, para ello nos posicionamos en el repositorio y presionamos
``Ctrl + Shift + T``, le llamaremos: **AccountRepositoryIntegrationTest**:

````java

@DataJpaTest                                        // (1)
class AccountRepositoryIntegrationTest {
    @Autowired
    private IAccountRepository accountRepository;   // (2)

    @Test
    void should_find_an_account_by_id() {
        Optional<Account> account = this.accountRepository.findById(1L);

        assertTrue(account.isPresent());
        assertEquals("Martín", account.get().getPerson());
    }

    @Test
    void should_find_an_account_by_person() {
        Optional<Account> account = this.accountRepository.findAccountByPerson("Martín");

        assertTrue(account.isPresent());
        assertEquals("Martín", account.get().getPerson());
        assertEquals(2000D, account.get().getBalance().doubleValue());
    }

    @Test
    void should_not_find_an_account_by_person_that_does_not_exist() {
        Optional<Account> account = this.accountRepository.findAccountByPerson("Pepito");

        assertTrue(account.isEmpty());
    }

    @Test
    void should_find_all_accounts() {
        List<Account> accounts = this.accountRepository.findAll();
        assertFalse(accounts.isEmpty());
        assertEquals(2, accounts.size());
    }
}
````

**DONDE**

- **(1)** la anotación @DataJpaTest que nos permite realizar pruebas a nuestros repositorios de JPA.
- **(2)** realizamos la inyección de dependencia de nuestro repositorio a probar. Esto es posible gracias a la anotación
  @DataJpaTest.
- Por defecto, gracias a la anotación @DataJpaTest, **cada método @Test es transaccional**, es decir, apenas termine la
  ejecución de un método test, automáticamente se hace un rollback de los datos para que se lleve a cabo la ejecución
  del siguiente test.

Como resultado observamos que los tests se ejecutan correctamente:

![prueba-de-integracion.png](./assets/prueba-de-integracion.png)

## Escribiendo pruebas para el save

````java

@DataJpaTest
class AccountRepositoryIntegrationTest {
    @Autowired
    private IAccountRepository accountRepository;

    @Test
    void should_save_an_account() {
        Account account = new Account(null, "Are", new BigDecimal("1500"));

        Account accountDB = this.accountRepository.save(account);

        assertNotNull(accountDB.getId());
        assertEquals("Are", accountDB.getPerson());
        assertEquals(1500D, accountDB.getBalance().doubleValue());
    }
}
````

## Escribiendo pruebas para el update y el delete

````java

@DataJpaTest
class AccountRepositoryIntegrationTest {
    @Autowired
    private IAccountRepository accountRepository;

    @Test
    void should_update_an_account() {
        Account account = new Account(null, "Are", new BigDecimal("1500"));

        Account accountDB = this.accountRepository.save(account);

        assertNotNull(accountDB.getId());
        assertEquals("Are", accountDB.getPerson());
        assertEquals(1500D, accountDB.getBalance().doubleValue());

        accountDB.setBalance(new BigDecimal("3800"));
        accountDB.setPerson("Karen Caldas");

        Account accountUpdated = this.accountRepository.save(accountDB);

        assertEquals(accountDB.getId(), accountUpdated.getId());
        assertEquals("Karen Caldas", accountUpdated.getPerson());
        assertEquals(3800D, accountUpdated.getBalance().doubleValue());
    }

    @Test
    void should_delete_an_account() {
        Optional<Account> accountDB = this.accountRepository.findById(1L);
        assertTrue(accountDB.isPresent());

        this.accountRepository.delete(accountDB.get());

        Optional<Account> accountDelete = this.accountRepository.findById(1L);
        assertTrue(accountDelete.isEmpty());
    }
}
````

**NOTA**

- Es importante volver a recalcar que cuando usamos la anotación **@DataJpaTest cada método test es transaccional**, eso
  significa que **hará rollback una vez finalice el método test**, de esa manera los datos vuelven a su estado original
  para dar paso al siguiente método test.
- Para comprobar el punto anterior, observemos la imagen inferior, vemos que el método test que se ejecuta primero es el
  **should_delete_an_account()** donde eliminamos el registro con id = 1L, pero luego en los otros métodos test estamos
  haciendo uso del registro con id = 1L y no estamos evidenciando problema alguno, eso es gracias a que los métodos son
  transaccionales y se está aplicando rollback de manera automática.

  ![rollback](./assets/rollback.png)

## Pruebas de Integración con @DataJpaTest usando MySQL

Primero debemos agregar el conector para mysql en el pom.xml:

````xml

<dependencies>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
````

En nuestro **application.properties** del directorio **/test/resources/** agregamos los datos de conexión a nuestra
base de datos de MySQL. Lo agregamos aquí, ya que estaremos simulando que MySQL será nuestra base de datos real para
pruebas.

````properties
# Datasource
spring.datasource.url=jdbc:mysql://localhost:3306/db_spring_boot_test?serverTimezone=America/Lima
spring.datasource.username=root
spring.datasource.password=magadiflo
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
# Only development
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
````

**NOTA**

> Como dijimos en un apartado superior, ahora solo reemplazamos los datos de nuestra base de datos real (MySQL).
> Anteriormente, usamos la misma configuración, pero con los datos de la base de datos en memoria **h2**.

Finalmente, a nuestra clase de prueba le agregamos una segunda anotación: **@AutoConfigureTestDatabase()**:

````java

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryIntegrationTest {
    @Autowired
    private IAccountRepository accountRepository;

    @Test
    void should_find_an_account_by_id() { /* omitted code */ }

    /* other tests */
}
````

La anotación **@AutoConfigureTestDatabase** la usamos cuando queremos ejecutar las pruebas en una base de datos real.
Al usar **replace = AutoConfigureTestDatabase.Replace.NONE**, le estamos indicando a Spring Boot que no reemplace la
configuración de la base de datos existente, lo que significa que utilizará la base de datos real configurada en tu
aplicación.

A continuación vemos la ejecución de nuestras pruebas, pero esta vez usando MySQL.
![prueba-integracion-mysql.png](./assets/prueba-integracion-mysql.png)

``Listo, ahora para continuar con el curso, dejaré configurado con la base de datos en memoria h2.``

# Sección 6: Spring Boot: Test de Controladores con MockMvc (WebMvcTest)

---

## Creando controller

Creamos en nuestro código fuente el controlador de nuestra aplicación. Por este capítulo solo definimos el método
**details()**:

````java

@RestController
@RequestMapping(path = "/api/v1/accounts")
public class AccountController {

    private final IAccountService accountService;

    public AccountController(IAccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Account> details(@PathVariable Long id) {
        return this.accountService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
````

## Creando controller parte 2

Continuamos con la construcción de nuestro controlador **Account**. Necesitamos implementar el método handler
transferir, pero necesitamos un objeto que recibirá los datos en el cuerpo del request, entonces crearemos un dto
llamado **TransactionDTO** que será un **Record**, ya que solo lo usaremos para recepcionar los datos enviados en la
solicitud:

````java
public record TransactionDTO(Long bankId, Long accountIdOrigin, Long accountIdDestination, BigDecimal amount) {
}
````

Ahora creamos el método **transfer()** en nuestro controlador:

````java

@RestController
@RequestMapping(path = "/api/v1/accounts")
public class AccountController {

    private final IAccountService accountService;

    /* construct and other handler method */

    @PostMapping(path = "/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransactionDTO dto) {
        this.accountService.transfer(dto.bankId(), dto.accountIdOrigin(), dto.accountIdDestination(), dto.amount());

        Map<String, Object> response = new HashMap<>();
        response.put("datetime", LocalDateTime.now());
        response.put("status", HttpStatus.OK);
        response.put("code", HttpStatus.OK.value());
        response.put("message", "transferencia exitosa");
        response.put("transaction", dto);

        return ResponseEntity.ok(response);
    }
}
````

Como más adelante usaremos clientes Http como Postman o Swagger, tenemos que configurar la transacción en la clase
service:

````java
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl implements IAccountService {
    /* omitted code */

    @Override
    @Transactional(readOnly = true) // (1)
    public Optional<Account> findById(Long id) { /* omitted code */ }

    @Override
    @Transactional(readOnly = true)
    public int reviewTotalTransfers(Long bancoId) { /* omitted code */ }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal reviewBalance(Long accountId) { /* omitted code */ }

    @Override
    @Transactional                  // (2)
    public void transfer(Long bankId, Long accountIdOrigen, Long accountIdDestination, BigDecimal amount) { /* omitted code */ }
}
````

**DONDE**

- **(1)** el método sobre el que está anotado, internamente solo hace búsquedas, es decir solo lee de la base de datos
  algún valor buscado, no hace ninguna modificación.
- **(2)** del método **transfer()**, sí se hacen modificaciones en la base de datos, por lo tanto, debe llevar tal cual
  está la anotación @Transactional.

**DATO**

> En Spring Boot, **la anotación @Transactional se utiliza para gestionar transacciones en métodos** que forman parte de
> un servicio o capa de servicio. **Una transacción es una secuencia de operaciones que se ejecutan como una unidad
> indivisible de trabajo.** Si alguna de las **operaciones falla, se deshacen todas las operaciones realizadas hasta ese
> momento**, evitando así estados inconsistentes en la base de datos.
>
> La anotación @Transactional se puede aplicar a nivel de clase o de método. Cuando se aplica a nivel de método,
> significa que ese método se ejecutará dentro de una transacción.
>
> Cuando se establece readOnly = true, se indica que el método no realizará operaciones de escritura (modificaciones en
> la base de datos). Esto permite que Spring optimice la transacción al no realizar un commit al final de la misma, ya
> que no hay necesidad de persistir cambios en la base de datos. Pero si estando el método anotado con readOnly = true,
> se intenta realizar operaciones de escritura, se producirá una excepción en tiempo de ejecución.

## Configurando Swagger

Usaré la dependencia que se usa en el tutorial:
[Spring Boot 3 + Swagger: Documentando una API REST desde cero,](https://www.youtube.com/watch?v=-SzKqwgPTyk)
ya que esta dependencia incluye varias características, tanto la especificación de **OpenAPI** y el **Swagger-UI** para
Spring Boot 3 entre otras
([Ver artículo del mismo tutorial para más información](https://sacavix.com/2023/03/spring-boot-3-spring-doc-swagger-un-ejemplo/)):

La documentación la podemos encontrar en: **[https://springdoc.org/](https://springdoc.org/)**:

````xml
<!-- https://springdoc.org/  -->
<dependencies>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.1.0</version>
    </dependency>
</dependencies>
````

**IMPORTANTE**

- Tan solo agregando la dependencia anterior **(sin configuración adicional)** nuestra aplicación de Spring Boot queda
  configurado con swagger-ui.
- La página de la interfaz de usuario de Swagger estará disponible en: ``http://localhost:8080/swagger-ui/index.html``
- La descripción de OpenAPI estará disponible en la siguiente URL para el formato
  json: ``http://localhost:8080/v3/api-docs``

**DATO**

- **OpenAPI** es una especificación. Es una especificación independiente del lenguaje que sirve para describir API REST.
  Es una serie de reglas, especificaciones y herramientas que nos ayudan a documentar nuestras APIs.
- **Swagger** es una herramienta que usa la especificación OpenAPI. Por ejemplo, OpenAPIGenerator y **SwaggerUI.**

## Configurando Base de datos MySQL

Como en nuestras dependencias ya tenemos agregado el conector a **MySQL**, agregaremos los datos de conexión a la base
de datos en el application.properties, ya que antes de hacer los tests, necesitamos verificar que la aplicación esté
funcionando. Recordar que también tenemos la dependencia de **h2**, pero esta dependencia la estamos usando para hacer
los test, mientras que la dependencia de **MySQL** lo usaremos para hacer funcionar nuestra aplicación, con nuestro
código fuente real.

``src/main/resources/application.properties``

````properties
# Datasource
spring.datasource.url=jdbc:mysql://localhost:3306/db_spring_boot_test?serverTimezone=America/Lima
spring.datasource.username=root
spring.datasource.password=magadiflo
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
# Only development
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
````

Creamos un archivo **import.sql** en la misma ruta del application.properties anterior:

``src/main/resources/import.sql``

````sql
INSERT INTO banks(name, total_transfers) VALUES('Banco de Crédito', 0);
INSERT INTO banks(name, total_transfers) VALUES('Banco Interbank', 0);
INSERT INTO banks(name, total_transfers) VALUES('Banco Scotiabank', 0);
INSERT INTO banks(name, total_transfers) VALUES('Banco BBVA', 0);

INSERT INTO accounts(person, balance) VALUES('Karen', 500);
INSERT INTO accounts(person, balance) VALUES('Miluska', 1500);
INSERT INTO accounts(person, balance) VALUES('Rosa', 750.50);
INSERT INTO accounts(person, balance) VALUES('Santiago', 260.30);
INSERT INTO accounts(person, balance) VALUES('Franz', 145.60);
````

## Pruebas Unitarias a controlador: con @WebMvcTest y MockMvc

### @WebMvcTest

- Anotación que se puede usar para una prueba de Spring MVC que se centra solo en los componentes de Spring MVC.
- El uso de esta anotación **deshabilitará la configuración automática completa** y, en su lugar, **aplicará solo la
  configuración relevante** para las pruebas de MVC, **es decir, habilitará los beans @Controller, @ControllerAdvice,
  @JsonComponent, Converter/GenericConverter, Filter, WebMvcConfigurer y HandlerMethodArgumentResolver**, pero no los
  beans @Component, @Service o @Repository beans.
- De forma predeterminada, **las pruebas anotadas con @WebMvcTest también configurarán automáticamente Spring Security y
  MockMvc.**
- Por lo general, **@WebMvcTest se usa en combinación con @MockBean o @Import** para crear cualquier colaborador
  requerido por sus beans @Controller.
- Spring nos brinda **la anotación @WebMvcTest** para facilitarnos los **test unitarios en de nuestros controladores.**
- Si colocamos el controlador dentro de la anotación **@WebMvcTest(AccountController.class)**, estamos indicándole que
  realizaremos las pruebas específicamente al controlador definido dentro de la anotación.

### MockMvc

- La anotación **@WebMvcTest permite especificar el controlador que se quiere probar** y tiene el efecto añadido que
  registra algunos beans de Spring, en particular una **instancia de la clase MockMvc**, que se puede **utilizar para
  invocar al controlador simulando la llamada HTTP sin tener que arrancar realmente ningún servidor web.**
- Es el contexto de MVC, pero falso, **el servidor HTTP es simulado**: request, response, etc. es decir, **no estamos
  trabajando sobre un servidor real HTTP**, lo que facilita la escritura de pruebas para controladores sin tener que
  iniciar un servidor web real.
- **MockMvc** ofrece una interfaz para **realizar solicitudes HTTP (GET, POST, PUT, DELETE, etc.)** a los endpoints de
  tus controladores y **obtener las respuestas simuladas.** Esto es especialmente útil para probar el comportamiento de
  tus controladores sin realizar solicitudes reales a una base de datos o a servicios externos.

### @MockBean

- La anotación **@MockBean** permite simular (mockear) el objeto sobre el cual esté anotado. Al utilizar @MockBean,
  Spring Boot reemplaza el bean original con el objeto simulado en el contexto de la aplicación durante la ejecución de
  la prueba.
- Como se mencionaba en el apartado **@WebMvcTest**, por lo general, **@WebMvcTest se usa en combinación con
  @MockBean o @Import** para crear cualquier colaborador requerido por sus beans @Controller.

### ResultActions

Permite aplicar acciones, como expectativas, sobre el resultado de una solicitud ejecutada, es decir, con esta clase
manejamos la respuesta del API REST.

### ObjectMapper

- Nos permitirá convertir cualquier objeto en un JSON y viceversa, un JSON en un objeto que por supuesto debe existir la
  clase a la que será convertido, donde los atributos de la clase coincidan con los nombres de los atributos del json y
  viceversa.
- ObjectMapper proporciona funcionalidad para leer y escribir JSON, ya sea hacia y desde POJO básicos (Plain Old Java
  Objects) o hacia y desde un modelo de árbol JSON de propósito general (JsonNode), así como la funcionalidad
  relacionada para realizar conversiones.

## Creando Prueba Unitaria al controlador AccountController

En el apartado anterior, coloqué algunas definiciones de los objetos que usaremos para crear nuestra prueba unitaria al
controlador, de esa manera tener más claro qué es lo que hace cada uno. Ahora, llegó el momento de crear la clase de
prueba:

````java

@WebMvcTest(AccountController.class)
class AccountControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private IAccountService accountService;

    @Test
    void should_find_an_account() throws Exception {
        // Given
        Long accountId = 1L;
        when(this.accountService.findById(accountId)).thenReturn(DataTest.account001());

        // When
        ResultActions response = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/accounts/{id}", accountId));

        // Then
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.person").value("Martín"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(2000));
        verify(this.accountService).findById(eq(accountId));
    }

    @Test
    void should_return_empty_when_account_does_not_exist() throws Exception {
        // Given
        Long accountId = 10L;
        when(this.accountService.findById(accountId)).thenReturn(Optional.empty());

        // When
        ResultActions response = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/accounts/{id}", accountId));

        // Then
        response.andExpect(MockMvcResultMatchers.status().isNotFound());
        verify(this.accountService).findById(eq(accountId));
    }
}
````

Como observamos en el código anterior, he creado dos pruebas unitarias donde se refleja un **escenario positivo** y un
**escenario negativo** al hacer una petición a la url ``/api/v1/accounts/{id}`` enviándole un id existente y un id que
no existe.
