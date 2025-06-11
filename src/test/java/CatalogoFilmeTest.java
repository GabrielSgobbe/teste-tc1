import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Catálogo de Filmes")
public class CatalogoFilmeTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Nested
    @DisplayName("Página Inicial")
    class PaginaInicialTests {

        @Test
        @Tag("pagina")
        @DisplayName("Deve conter 'catalogo' no título da página")
        public void testPaginaInicialTitulo() {
            driver.get("https://catalogo-filme-rosy.vercel.app/");
            String titulo = driver.getTitle();
            assertTrue(titulo.toLowerCase().contains("catalogo"), "Título da página deve conter 'catalogo'");
        }

        @Test
        @Tag("pagina")
        @DisplayName("Deve listar pelo menos um filme com título visível")
        public void testListaFilmesVisivel() {
            driver.get("https://catalogo-filme-rosy.vercel.app/");
            List<WebElement> filmes = driver.findElements(By.cssSelector(".card"));
            assertFalse(filmes.isEmpty(), "Deve haver pelo menos um filme listado na página inicial");

            WebElement primeiroFilme = filmes.get(0);
            WebElement tituloFilme = primeiroFilme.findElement(By.cssSelector(".card-title"));
            assertTrue(tituloFilme.isDisplayed(), "Título do filme deve estar visível");
            assertFalse(tituloFilme.getText().isEmpty(), "Título do filme não deve estar vazio");
        }
    }

    @Nested
    @DisplayName("Criação de Filmes")
    class CriacaoFilmeTests {

        @Test
        @Tag("criacao")
        @DisplayName("Deve criar filme com dados válidos")
        public void testCriarFilme() {
            driver.get("https://catalogo-filme-rosy.vercel.app/Criar");

            WebElement campoNome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("nome")));
            WebElement campoGenero = driver.findElement(By.name("genero"));
            WebElement campoAno = driver.findElement(By.name("ano"));

            campoNome.sendKeys("Filme de Teste");
            campoGenero.sendKeys("Aventura");
            campoAno.sendKeys("2025");

            driver.findElement(By.cssSelector("button.btn-success[type='submit']")).click();

            wait.until(ExpectedConditions.urlContains("/"));
            assertTrue(driver.getCurrentUrl().contains("/"), "Deveria redirecionar para a página inicial após criar o filme.");
        }

        @Test
        @Tag("validacao")
        @DisplayName("Não deve criar filme com ano inválido")
        public void testCriarFilmeComAnoInvalido() {
            driver.get("https://catalogo-filme-rosy.vercel.app/Criar");

            WebElement campoNome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("nome")));
            WebElement campoGenero = driver.findElement(By.name("genero"));
            WebElement campoAno = driver.findElement(By.name("ano"));

            campoNome.sendKeys("Bug: Filme do Futuro");
            campoGenero.sendKeys("Ficção");
            campoAno.sendKeys("999999");

            driver.findElement(By.cssSelector("button.btn-success[type='submit']")).click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("nome")));
            assertTrue(driver.getCurrentUrl().contains("/Criar"), "Filme com ano inválido não deveria ser criado.");
        }
    }

    @Nested
    @DisplayName("Edição de Filmes")
    class EdicaoFilmeTests {

        @Test
        @Tag("edicao")
        @DisplayName("Deve editar um filme existente com sucesso")
        public void testEditarFilme() {
            driver.get("https://catalogo-filme-rosy.vercel.app/Alterar");

            WebElement campoId = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("idFilme")));
            campoId.clear();
            campoId.sendKeys("2");

            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Procurar')]"))).click();

            WebElement campoNome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("nome")));
            WebElement campoGenero = driver.findElement(By.name("genero"));
            WebElement campoAno = driver.findElement(By.name("ano"));

            campoNome.clear();
            campoNome.sendKeys("Filme Editado");
            campoGenero.clear();
            campoGenero.sendKeys("Drama");
            campoAno.clear();
            campoAno.sendKeys("1999");

            driver.findElement(By.xpath("//button[contains(text(), 'Alterar')]")).click();

            WebElement corpo = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            assertTrue(corpo.getText().toLowerCase().contains("sucesso") || corpo.getText().toLowerCase().contains("editado"));
        }

        @Test
        @Tag("edicao")
        @DisplayName("Não deve editar filme com ID inexistente")
        public void testAlterarFilmeComIdInexistente() {
            driver.get("https://catalogo-filme-rosy.vercel.app/Alterar/99999");

            WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            assertTrue(
                    body.getText().toLowerCase().contains("não encontrado")
                            || body.getText().toLowerCase().contains("erro")
                            || body.getText().trim().isEmpty(),
                    "A página não deveria carregar com ID inexistente."
            );
        }
    }

    @Nested
    @DisplayName("Exclusão de Filmes")
    class ExclusaoFilmeTests {

        @Test
        @Tag("exclusao")
        @DisplayName("Deve criar e excluir um filme com sucesso")
        public void testCriarEExcluirFilme() {
            driver.get("https://catalogo-filme-rosy.vercel.app/Criar");

            String tituloFilme = "AutoTeste_" + System.currentTimeMillis();
            driver.findElement(By.name("nome")).sendKeys(tituloFilme);
            driver.findElement(By.name("genero")).sendKeys("Teste");
            driver.findElement(By.name("ano")).sendKeys("2025");

            driver.findElement(By.cssSelector("button.btn-success[type='submit']")).click();
            wait.until(ExpectedConditions.urlToBe("https://catalogo-filme-rosy.vercel.app/"));
            driver.navigate().refresh();

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.text-decoration-none[href^='/Ler/']")));
            List<WebElement> links = driver.findElements(By.cssSelector("a.text-decoration-none[href^='/Ler/']"));

            String idFilmeCriado = null;
            for (WebElement link : links) {
                WebElement divFilme = link.findElement(By.tagName("div"));
                String textoDiv = divFilme.getText();

                if (textoDiv.contains(tituloFilme)) {
                    int idxIdStart = textoDiv.indexOf("Id: ") + 4;
                    int idxIdEnd = textoDiv.indexOf(" Nome:");
                    if (idxIdStart >= 4 && idxIdEnd > idxIdStart) {
                        idFilmeCriado = textoDiv.substring(idxIdStart, idxIdEnd).trim();
                    }
                    break;
                }
            }

            assertNotNull(idFilmeCriado, "ID do filme criado não foi encontrado!");

            driver.get("https://catalogo-filme-rosy.vercel.app/Apagar/" + idFilmeCriado);

            WebElement campoId = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("idFilme")));
            assertEquals(idFilmeCriado, campoId.getAttribute("value"), "O campo ID não contém o ID esperado");

            driver.findElement(By.cssSelector("button.btn-primary[type='submit']")).click();
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("h1"), "Apagar Filme"));

            WebElement botaoApagar = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-danger[type='submit']")));
            botaoApagar.click();

            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            assertTrue(alert.getText().toLowerCase().contains("sucesso"), "Mensagem do alert não confirma sucesso.");
            alert.accept();
        }

        @Test
        @Tag("validacao")
        @DisplayName("Não deve permitir exclusão sem fornecer ID")
        public void testExcluirSemID_DeveFalhar() {
            driver.get("https://catalogo-filme-rosy.vercel.app/Apagar");

            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h1[text()='Apagar Filme']")));

            fail("A página de exclusão não deveria carregar sem um ID.");
        }
    }
}
