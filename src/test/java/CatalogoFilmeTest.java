import io.github.bonigarcia.wdm.WebDriverManager;
import net.datafaker.Faker;
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
    private Faker faker;

    @BeforeEach
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        faker = new Faker();
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

            assertTrue(textoPagina.contains("erro") || textoPagina.contains("not found") || textoPagina.contains("página não encontrada") || textoPagina.isEmpty(),
                    "A aplicação deveria exibir uma mensagem de erro ou redirecionar ao acessar rota dinâmica sem ID.");
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


            String nomeFilme = faker.lordOfTheRings().character() + " em " + faker.lordOfTheRings().location();
            String generoFilme = faker.lordOfTheRings().location();

            campoNome.sendKeys(nomeFilme);
            campoGenero.sendKeys(generoFilme);
            campoAno.sendKeys(String.valueOf(faker.number().numberBetween(1950, 2025)));

            driver.findElement(By.cssSelector("button.btn-success[type='submit']")).click();

            wait.until(ExpectedConditions.urlContains("/"));
            assertTrue(driver.getCurrentUrl().contains("/"), "Deveria redirecionar para a página inicial após criar o filme.");
        }

        @Test
        @Tag("criacao")
        @DisplayName("Não deve criar filme com letra no campo Ano")
        public void testCriarFilmeComLetraAno() {
            driver.get("https://catalogo-filme-rosy.vercel.app/Criar");

            WebElement campoNome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("nome")));
            WebElement campoGenero = driver.findElement(By.name("genero"));
            WebElement campoAno = driver.findElement(By.name("ano"));

            String nomeFilme = faker.lordOfTheRings().character() + " em " + faker.lordOfTheRings().location();
            String generoFilme = faker.lordOfTheRings().location();

            campoNome.sendKeys(nomeFilme);
            campoGenero.sendKeys(generoFilme);
            campoAno.sendKeys("a"); // navegador pode cortar ou impedir envio

            driver.findElement(By.cssSelector("button.btn-success[type='submit']")).click();

            // Espera 3 segundos e verifica que  nao redirecionou para a ppagina incial
            boolean redirecionou = new WebDriverWait(driver, Duration.ofSeconds(3)).until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/Criar"),
                    ExpectedConditions.not(ExpectedConditions.urlToBe("https://catalogo-filme-rosy.vercel.app/"))
            ));

            assertTrue(driver.getCurrentUrl().contains("/Criar"), "Deveria permanecer na página de criação com erro no campo ano.");
        }

        @Test
        @Tag("validacao")
        @DisplayName("Não deve permitir o cadastro de filme com ano no futuro muito distante")
        public void testNaoCriarFilmeComAnoFuturoInvalido() {
            driver.get("https://catalogo-filme-rosy.vercel.app/Criar");

            WebElement campoNome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("nome")));
            WebElement campoGenero = driver.findElement(By.name("genero"));
            WebElement campoAno = driver.findElement(By.name("ano"));

            String nomeFilme = faker.lordOfTheRings().character() + " em " + faker.lordOfTheRings().location();
            String generoFilme = faker.lordOfTheRings().location();

            campoNome.sendKeys(nomeFilme);
            campoGenero.sendKeys(generoFilme);
            campoAno.sendKeys("999999");

            driver.findElement(By.cssSelector("button.btn-success[type='submit']")).click();

            // Verifica que a URL continua em /Criar, ou seja, não foi aceito
            assertTrue(wait.until(ExpectedConditions.urlContains("/Criar")), "A aplicação deveria permanecer na página de criação com ano inválido.");

            // verifica se tem alguma mensamge de erro explicida
            List<WebElement> mensagensErro = driver.findElements(By.cssSelector("p.text-danger"));
            assertFalse(mensagensErro.isEmpty(), "Deveria exibir uma mensagem de erro para ano inválido.");

            WebElement erro = mensagensErro.get(0);
            String texto = erro.getText().toLowerCase();

            assertTrue(erro.isDisplayed(), "Mensagem de erro deve estar visível.");
            assertTrue(texto.contains("ano") || texto.contains("inválido"), "Mensagem de erro não menciona claramente problema no ano.");
        }
    }

    @Nested
    @DisplayName("Edição de Filmes")
    class EdicaoFilmeTests {

        @Test
        @Tag("edicao")
        @DisplayName("Deve editar um filme existente com sucesso")
        public void testEditarFilme() {
            driver.get("https://catalogo-filme-rosy.vercel.app/Alterar/2");

            WebElement campoId = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("idFilme"))); //so funciona se colocar o id manual
            campoId.clear();
            campoId.sendKeys("2"); // aqui tbm o id do item

            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Procurar')]"))).click();

            WebElement campoNome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("nome")));
            WebElement campoGenero = driver.findElement(By.name("genero"));
            WebElement campoAno = driver.findElement(By.name("ano"));

            String nomeFilme = faker.lordOfTheRings().character() + " em " + faker.lordOfTheRings().location();
            String generoFilme = faker.lordOfTheRings().location();

            campoNome.clear();
            campoNome.sendKeys(nomeFilme);
            campoGenero.clear();
            campoGenero.sendKeys(generoFilme);
            campoAno.clear();
            campoAno.sendKeys(String.valueOf(faker.number().numberBetween(1950, 2025)));

            driver.findElement(By.xpath("//button[contains(text(), 'Alterar')]")).click();

            //Espera até que o alert esteja presente
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());

            //Pega o texto do alert
            String textoAlert = alert.getText().toLowerCase();
            System.out.println("Texto do alert: " + textoAlert);

            //Verifica se o texto contém algumas das plavras
            assertTrue(textoAlert.contains("sucesso") || textoAlert.contains("editado"));
            alert.accept();
        }

        @Test
        @Tag("edicao")
        @DisplayName("Não deve editar filme com ID inexistente")
        public void testAlterarFilmeComIdInexistente() {
            driver.get("https://catalogo-filme-rosy.vercel.app/Alterar/99999");

            WebElement campoId = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("idFilme")));
            assertEquals("99999", campoId.getAttribute("value"), "Campo ID deveria conter 99999.");

            WebElement botaoProcurar = wait.until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Procurar')]"))
            );
            botaoProcurar.click();

            WebElement erroMsg = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.text-danger"))
            );

            String textoErro = erroMsg.getText().toLowerCase();
            assertTrue(
                    textoErro.contains("404") || textoErro.contains("request failed") || textoErro.contains("não encontrado"),
                    "A página deveria exibir mensagem de erro ao acessar um ID inexistente."
            );
        }

        @Nested
        @DisplayName("Exclusão de Filmes")
        class ExclusaoFilmeTests {

            @Test
            @Tag("exclusao")
            @DisplayName("Deve criar e excluir um filme com sucesso")
            public void testCriarEExcluirFilme() {
                driver.get("https://catalogo-filme-rosy.vercel.app/Criar"); //forma de criar e excluir um filme

                String tituloFilme = faker.lordOfTheRings().character() + "_" + System.currentTimeMillis();
                String generoFilme = faker.lordOfTheRings().location();

                driver.findElement(By.name("nome")).sendKeys(tituloFilme);
                driver.findElement(By.name("genero")).sendKeys(generoFilme);
                driver.findElement(By.name("ano")).sendKeys(String.valueOf(faker.number().numberBetween(1980, 2025)));

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
                driver.get("https://catalogo-filme-rosy.vercel.app/Apagar/"); //nao vai permitir pois nao tem essa rota, somente se tiver item cadastrado

                WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                String textoPagina = body.getText().toLowerCase();

                assertTrue(textoPagina.contains("erro") || textoPagina.contains("não encontrado") || textoPagina.contains("404") || textoPagina.isEmpty(),
                        "A aplicação deveria exibir mensagem de erro ou redirecionar ao acessar /Apagar/ sem ID.");
            }
        }
    }
}
