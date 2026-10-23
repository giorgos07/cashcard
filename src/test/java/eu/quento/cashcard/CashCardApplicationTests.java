package eu.quento.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import eu.quento.cashcard.data.models.DbCashCard;
import eu.quento.cashcard.models.requests.CreateCashCardRequest;
import eu.quento.cashcard.models.requests.UpdateCashCardRequest;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public class CashCardApplicationTests {
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    public void shouldReturnACashCardWhenDataIsSaved() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("gmanoltzas@quento.eu", "123abc!")
                .getForEntity("/cash-cards/99", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(99);

        Double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo(123.45);
    }

    @Test
    public void shouldNotReturnACashCardWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("gmanoltzas@quento.eu", "123abc!")
                .getForEntity("/cash-cards/1000", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }

    @Test
    @DirtiesContext
    public void shouldCreateANewCashCard() {
        var newCashCard = new CreateCashCardRequest(250.00);

        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("gmanoltzas@quento.eu", "123abc!")
                .postForEntity("/cash-cards", newCashCard, Void.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
        assert locationOfNewCashCard != null;

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("gmanoltzas@quento.eu", "123abc!")
                .getForEntity(locationOfNewCashCard, String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        Number id = documentContext.read("$.id");
        assertThat(id).isNotNull();

        Double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo(250.00);
    }

    @Test
    public void shouldReturnAllCashCardsWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("gmanoltzas@quento.eu", "123abc!")
                .getForEntity("/cash-cards", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int cashCardCount = documentContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(2);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).asInstanceOf(LIST).containsExactlyInAnyOrder(99, 100);

        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).asInstanceOf(LIST).containsExactlyInAnyOrder(123.45, 100.0);
    }

    @Test
    public void shouldReturnAPageOfCashCards() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("gmanoltzas@quento.eu", "123abc!")
                .getForEntity("/cash-cards?page=0&size=1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(1);
    }

    @Test
    public void shouldReturnASortedPageOfCashCards() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("gmanoltzas@quento.eu", "123abc!")
                .getForEntity("/cash-cards?page=0&size=1&sort=amount,desc", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        JSONArray read = documentContext.read("$[*]");
        assertThat(read.size()).isEqualTo(1);

        double amount = documentContext.read("$[0].amount");
        assertThat(amount).isEqualTo(123.45);
    }

    @Test
    public void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("gmanoltzas@quento.eu", "123abc!")
                .getForEntity("/cash-cards", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(2);

        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).asInstanceOf(LIST).containsExactly(100.00, 123.45);
    }

    @Test
    public void shouldRejectUsersWhoAreNotCardOwners() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("owner@example.com", "hello123")
                .getForEntity("/cash-cards/99", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    public void shouldUpdateAnExistingCashCard() {
        var cashCardUpdate = new DbCashCard(null, 19.99, null);
        HttpEntity<DbCashCard> request = new HttpEntity<>(cashCardUpdate);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("gmanoltzas@quento.eu", "123abc!")
                .exchange("/cash-cards/99", HttpMethod.PUT, request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("gmanoltzas@quento.eu", "123abc!")
                .getForEntity("/cash-cards/99", String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(99);

        Double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo(19.99);
    }

    @Test
    public void shouldNotUpdateACashCardThatDoesNotExist() {
        var unknownCard = new UpdateCashCardRequest(19.99);
        HttpEntity<UpdateCashCardRequest> request = new HttpEntity<>(unknownCard);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("gmanoltzas@quento.eu", "123abc!")
                .exchange("/cash-cards/99999", HttpMethod.PUT, request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldNotUpdateACashCardThatIsOwnedBySomeoneElse() {
        var notOwnedCard = new UpdateCashCardRequest(333.33);
        HttpEntity<UpdateCashCardRequest> request = new HttpEntity<>(notOwnedCard);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("owner@example.com", "hello123")
                .exchange("/cash-cards/101", HttpMethod.PUT, request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    public void shouldDeleteAnExistingCashCard() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("gmanoltzas@quento.eu", "123abc!")
                .exchange("/cash-cards/99", HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("gmanoltzas@quento.eu", "123abc!")
                .getForEntity("/cash-cards/99", String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldNotDeleteACashCardThatDoesNotExist() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("gmanoltzas@quento.eu", "123abc!")
                .exchange("/cash-cards/99999", HttpMethod.DELETE, null, Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldNotAllowDeletionOfCashCardsTheyDoNotOwn() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("owner@example.com", "hello123")
                .exchange("/cash-cards/99", HttpMethod.DELETE, null, Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("gmanoltzas@quento.eu", "123abc!")
                .getForEntity("/cash-cards/99", String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
