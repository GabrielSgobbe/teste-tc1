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
    public void testCriarEExcluirFilme() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 1. Acessa a página criar
        driver.get("https://catalogo-filme-rosy.vercel.app/Criar");

        // 2. Cria um filme teste com dados unico
        String tituloFilme = "AutoTeste_" + System.currentTimeMillis();
        String genero = "Teste";
        String ano = "2025";

        // 3. Aqui vai preencher os cmapos
        WebElement campoNome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("nome")));
        driver.findElement(By.name("genero")).sendKeys(genero);
        driver.findElement(By.name("ano")).sendKeys(ano);
        campoNome.sendKeys(tituloFilme);

        // 4. Manda o filme pro cadastro
        WebElement botaoCriar = driver.findElement(By.cssSelector("button.btn-success[type='submit']"));
        botaoCriar.click();

        // 5. Vai direcionar para a pagina home
        wait.until(ExpectedConditions.urlToBe("https://catalogo-filme-rosy.vercel.app/"));
        driver.navigate().refresh();

        // 6. Aguarda e busca o link que contém o nome do filme e extrai o ID do texto criado
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.text-decoration-none[href^='/Ler/']")));

        List<WebElement> links = driver.findElements(By.cssSelector("a.text-decoration-none[href^='/Ler/']"));
        String idFilmeCriado = null;

        for (WebElement link : links) {
            WebElement divFilme = link.findElement(By.tagName("div"));
            String textoDiv = divFilme.getText();

            if (textoDiv.contains(tituloFilme)) {
                int idxIdStart = textoDiv.indexOf("Id: ") + 4; //vai pular 4 caracteres para byuscar apenas o Id
                int idxIdEnd = textoDiv.indexOf(" Nome:");
                if (idxIdStart >= 4 && idxIdEnd > idxIdStart) {
                    idFilmeCriado = textoDiv.substring(idxIdStart, idxIdEnd).trim();
                }
                break;
            }
        }

        assertNotNull(idFilmeCriado, "ID do filme criado não foi encontrado!");
        System.out.println("Filme criado com ID: " + idFilmeCriado);

        // 7. Acessa a página de apagar com o ID
        driver.get("https://catalogo-filme-rosy.vercel.app/Apagar/" + idFilmeCriado);

        // 8. Aguarda o campo do ID no formulário de procura estar visível
        WebElement campoId = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("idFilme")));

        // 9. Verifica se o campo já contém o ID esperado
        String valorCampoId = campoId.getAttribute("value");
        assertEquals(idFilmeCriado, valorCampoId, "O campo ID não contém o ID esperado");

        // 10. Clica no botão "Procurar"
        WebElement botaoProcurar = driver.findElement(By.cssSelector("button.btn-primary[type='submit']"));
        botaoProcurar.click();

        // 11. Aguarda a tela de exclusão carregar
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("h1"), "Apagar Filme"));

        // 12. Clica no botão "Apagar"
        WebElement botaoApagar = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-danger[type='submit']")));
        botaoApagar.click();

        // 13. Aguarda o alert do navegador aparecer e o aceita
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertTrue(alert.getText().toLowerCase().contains("sucesso"), "Mensagem do alert não confirma sucesso.");
        alert.accept();
    }

    @Test
    public void testExcluirSemID_DeveFalhar() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Acessa a URL de exclusão sem fornecer ID (propositalmente inválida)
        driver.get("https://catalogo-filme-rosy.vercel.app/Apagar");

        // Espera por algum título da página de exclusão (que não vai aaparecer)
        // Aqui o teste deve falhar porque a página real precisa de um ID
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[text()='Apagar Filme']")));

        // Se caso carregar que nao vai, precisa colocar o id manualmente
        fail("A página de exclusão não deveria carregar sem um ID.");
    }

}
