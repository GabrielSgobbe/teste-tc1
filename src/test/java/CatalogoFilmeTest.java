import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CatalogoFilmeTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();

        // Espera explícita configurada globalmente para todos os testes
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // --- Teste da página inicial ---
    @Test
    public void testPaginaInicialTitulo() {
        driver.get("https://catalogo-filme-rosy.vercel.app/");
        String titulo = driver.getTitle();
        assertTrue(titulo.toLowerCase().contains("catalogo"), "Título da página deve conter 'catalogo'");
    }

    @Test
    public void testListaFilmesVisivel() {
        driver.get("https://catalogo-filme-rosy.vercel.app/");
        List<WebElement> filmes = driver.findElements(By.cssSelector(".card"));
        assertFalse(filmes.isEmpty(), "Deve haver pelo menos um filme listado na página inicial");

        WebElement primeiroFilme = filmes.get(0);
        WebElement tituloFilme = primeiroFilme.findElement(By.cssSelector(".card-title"));
        assertTrue(tituloFilme.isDisplayed(), "Título do filme deve estar visível");
        assertFalse(tituloFilme.getText().isEmpty(), "Título do filme não deve estar vazio");
    }

    // --- Teste de criação de filme válido ---
    @Test
    public void testCriarFilme() {
        driver.get("https://catalogo-filme-rosy.vercel.app/Criar");

        WebElement campoNome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("nome")));
        WebElement campoGenero = driver.findElement(By.name("genero"));
        WebElement campoAno = driver.findElement(By.name("ano"));

        campoNome.sendKeys("Filme de Teste");
        campoGenero.sendKeys("Aventura");
        campoAno.sendKeys("2025");

        WebElement botaoCriar = driver.findElement(By.cssSelector("button.btn-success[type='submit']"));
        botaoCriar.click();

        wait.until(ExpectedConditions.urlContains("/"));
        assertTrue(driver.getCurrentUrl().contains("/"), "Deveria redirecionar para a página inicial após criar o filme.");
    }

    // --- Teste de criação com ano inválido ---
    @Test
    public void testCriarFilmeComAnoInvalido() {
        driver.get("https://catalogo-filme-rosy.vercel.app/Criar");

        WebElement campoNome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("nome")));
        WebElement campoGenero = driver.findElement(By.name("genero"));
        WebElement campoAno = driver.findElement(By.name("ano"));

        campoNome.sendKeys("Bug: Filme do Futuro");
        campoGenero.sendKeys("Ficção");
        campoAno.sendKeys("999999"); // ano inválido

        WebElement botaoCriar = driver.findElement(By.cssSelector("button.btn-success[type='submit']"));
        botaoCriar.click();

        // Ainda deve estar na mesma tela (sem redirecionamento)
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("nome")));
        String url = driver.getCurrentUrl();
        assertTrue(url.contains("/Criar"), "Filme com ano inválido não deveria ser criado.");
    }


    @Test
    public void testEditarFilme() {
        driver.get("https://catalogo-filme-rosy.vercel.app/Alterar");

        WebElement campoId = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("idFilme")));
        campoId.clear();
        campoId.sendKeys("2");

        WebElement botaoProcurar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Procurar')]")));
        botaoProcurar.click();

        WebElement campoNome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("nome")));
        WebElement campoGenero = driver.findElement(By.name("genero"));
        WebElement campoAno = driver.findElement(By.name("ano"));

        campoNome.clear();
        campoNome.sendKeys("Filme Editado");
        campoGenero.clear();
        campoGenero.sendKeys("Drama");
        campoAno.clear();
        campoAno.sendKeys("1999");

        WebElement botaoAlterar = driver.findElement(By.xpath("//button[contains(text(), 'Alterar')]"));
        botaoAlterar.click();

        WebElement corpo = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        assertTrue(corpo.getText().toLowerCase().contains("sucesso") || corpo.getText().toLowerCase().contains("editado"));
    }


    @Test
    public void testAlterarFilmeComIdInexistente() {
        driver.get("https://catalogo-filme-rosy.vercel.app/Alterar/99999"); // ID inexistente

        WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        assertTrue(
                body.getText().toLowerCase().contains("não encontrado")
                        || body.getText().toLowerCase().contains("erro")
                        || body.getText().trim().isEmpty(),
                "A página não deveria carregar com ID inexistente."
        );
    }


    @Test
    public void testExcluirFilme() {
        driver.get("https://catalogo-filme-rosy.vercel.app/Apagar");

        WebElement campoId = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("idFilme")));
        campoId.clear();
        campoId.sendKeys("3");

        WebElement botaoProcurar = driver.findElement(By.xpath("//button[text()='Procurar']"));
        botaoProcurar.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Apagar Filme']")));
        WebElement btnApagar = driver.findElement(By.xpath("//button[text()='Apagar']"));
        btnApagar.click();

        WebElement confirmacao = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'sucesso') or contains(text(), 'apagado')]")));
        assertTrue(confirmacao.getText().toLowerCase().contains("sucesso"));
    }
}
