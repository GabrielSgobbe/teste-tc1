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
        @Tag("interface")
        @DisplayName("Botões Editar e Excluir só aparecem quando há filmes cadastrados")
        public void testBotoesEditarExcluirAparecemComFilmes() {
            driver.get("https://catalogo-filme-rosy.vercel.app/");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            List<WebElement> filmes = driver.findElements(By.cssSelector("a.text-decoration-none[href^='/Ler/']"));

            if (filmes.isEmpty()) {
                // Se não há filmes, os botões Editar e Excluir não devem estar presentes
                List<WebElement> botoesEditar = driver.findElements(By.cssSelector("a[href^='/Alterar/']"));
                List<WebElement> botoesExcluir = driver.findElements(By.cssSelector("a[href^='/Apagar/']"));

                assertTrue(botoesEditar.isEmpty(), "Não deveria haver botão Editar sem filmes.");
                assertTrue(botoesExcluir.isEmpty(), "Não deveria haver botão Excluir sem filmes.");
            } else {
                // Se há filmes, os botões Editar e Excluir devem aparecer ao menos uma vez
                List<WebElement> botoesEditar = driver.findElements(By.cssSelector("a[href^='/Alterar/']"));
                List<WebElement> botoesExcluir = driver.findElements(By.cssSelector("a[href^='/Apagar/']"));

                assertFalse(botoesEditar.isEmpty(), "Botões Editar deveriam aparecer com filmes cadastrados.");
                assertFalse(botoesExcluir.isEmpty(), "Botões Excluir deveriam aparecer com filmes cadastrados.");
            }
        }

        @Test
        @Tag("rota")
        @DisplayName("Não deve permitir acessar rota de alteração sem ID")
        public void testAcessarAlterarSemId() {
            driver.get("https://catalogo-filme-rosy.vercel.app/Alterar/");

            WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            String textoPagina = body.getText().toLowerCase();

            assertTrue(
                    textoPagina.contains("erro")
                            || textoPagina.contains("not found")
                            || textoPagina.contains("página não encontrada")
                            || textoPagina.isEmpty(),
                    "A aplicação deveria exibir uma mensagem de erro ou redirecionar ao acessar rota dinâmica sem ID."
            );
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

        @Test
        @Tag("criacao")
        @DisplayName("Não deve criar filme com Letra no Ano ")
        public void testCriarFilmeComLetraAno() {
            driver.get("https://catalogo-filme-rosy.vercel.app/Criar");

            WebElement campoNome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("nome")));
            WebElement campoGenero = driver.findElement(By.name("genero"));
            WebElement campoAno = driver.findElement(By.name("ano"));

            campoNome.sendKeys("Filme com Erro no Ano");
            campoGenero.sendKeys("Drama");

            // Só é permitido digitar 1 letra, o navegador corta
            campoAno.sendKeys("a");

            // Clica no botão de envio
            driver.findElement(By.cssSelector("button.btn-success[type='submit']")).click();

            // Aguarda um pouco e confirma que não redirecionou
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
                shortWait.until(ExpectedConditions.urlToBe("https://catalogo-filme-rosy.vercel.app/"));
                fail("Deveria impedir o envio do formulário com letra no campo de ano.");
            } catch (TimeoutException e) {
                // OK: o formulário não foi enviado, continuou na página de criação
                assertTrue(driver.getCurrentUrl().contains("/Criar"), "Deveria permanecer na página de criação com erro no campo.");
            }
        }
    }

    @Nested
    @DisplayName("Edição de Filmes")
    class EdicaoFilmeTests {

        @Test
        @Tag("edicao")
        @DisplayName("Deve editar um filme existente com sucesso")
        public void testEditarFilme() {
            driver.get("https://catalogo-filme-rosy.vercel.app/Alterar/2"); //so funciona se colocar o id manual

            WebElement campoId = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("idFilme")));
            campoId.clear();
            campoId.sendKeys("2"); ;// aqui tbm o id do item

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


            // Espera até que o alert esteja presente
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());

            // Pega o texto do alert
            String textoAlert = alert.getText().toLowerCase();
            System.out.println("Texto do alert: " + textoAlert);

            // Verifica se o texto contém as palavras esperadas
            assertTrue(textoAlert.contains("sucesso") || textoAlert.contains("editado"));

            // Fecha o alert clicando no OK
            alert.accept();

        }

        @Test
        @Tag("edicao")
        @DisplayName("Não deve editar filme com ID inexistente") // erro no teste pq nao tem mensagem de id nao encontrado só um 404
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
            driver.get("https://catalogo-filme-rosy.vercel.app/Criar"); //forma de criar e editar um filme

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
            driver.get("https://catalogo-filme-rosy.vercel.app/Apagar/");  //nao vai permitir pois nao tem essa rota, somente se tiver item cadastrado

            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h1[text()='Apagar Filme']")));

            fail("A página de exclusão não deveria carregar sem um ID.");
        }



    }
}
