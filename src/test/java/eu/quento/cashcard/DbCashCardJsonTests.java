package eu.quento.cashcard;

import eu.quento.cashcard.data.models.DbCashCard;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class DbCashCardJsonTests {
    @Autowired
    private JacksonTester<DbCashCard> json;

    @Autowired
    private JacksonTester<DbCashCard[]> jsonList;

    private DbCashCard[] cashCards;

    @BeforeEach()
    public void setUp() {
        cashCards = Arrays.array(
                new DbCashCard(99L, 123.45, "gmanoltzas@quento.eu"),
                new DbCashCard(100L, 100.00, "gmanoltzas@quento.eu"),
                new DbCashCard(101L, 150.00, "random-user@example.com")
        );
    }

    @Test
    public void cashCardSerializationTest() throws IOException {
        var cashCard = cashCards[0];
        assertThat(json.write(cashCard)).isStrictlyEqualToJson("single.json");
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id").isEqualTo(99);
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount").isEqualTo(123.45);
    }

    @Test
    public void cashCardDeserializationTest() throws IOException {
        String expected = """
           {
               "id":99,
               "amount":123.45,
               "owner":"owner"
           }
           """;

        assertThat(json.parse(expected)).isEqualTo(new DbCashCard(99L, 123.45, "owner"));
        assertThat(json.parseObject(expected).id()).isEqualTo(99);
        assertThat(json.parseObject(expected).amount()).isEqualTo(123.45);
    }

    @Test
    public void cashCardListSerializationTest() throws IOException {
        assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson("list.json");
    }

    @Test
    public void cashCardListDeserializationTest() throws IOException {
        String expected = """
         [
            { "id": 99, "amount": 123.45, "owner": "gmanoltzas@quento.eu" },
            { "id": 100, "amount": 100.00, "owner": "gmanoltzas@quento.eu" },
            { "id": 101, "amount": 150.00, "owner": "random-user@example.com" }
         ]
         """;

        assertThat(jsonList.parse(expected)).isEqualTo(cashCards);
    }
}
