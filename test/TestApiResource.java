import models.Clearance;
import models.MartianEntity;
import models.Response;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class TestApiResource
{
    private final MarsApiResource apiResource = new MarsApiResource();
    private static final Path martianEntity = Path.of("resource/martianEntity.json");
    private static final Path updatedMartianEntity = Path.of("resource/updatedMartianEntity.json");
    private static final String NON_EXISTENT_ID = "Non-existent ID";
    private static final String UPDATE_404_MESSAGE = "Either you don't have permission or the entity you have requested to update does not exist in the database";
    private static final String RETRIEVAL_404_MESSAGE = "Either no entity exists with id [%s] or user lacks the permissions to access the entity";
    private final static String FAILED_DESERIALISE_MESSAGE = "Failed to deserialise entity. Please submit a valid JSON body";

    @Test
    public void callingAddEndpointWhenJsonIsInvalidShouldReturn500Response()
    {
        assertThat(apiResource.countOfEntities()).isEqualTo(0);
        final var response = apiResource.createMartianEntity(null);

        assertThat(apiResource.countOfEntities()).isEqualTo(0);
        assertThat(response)
                .extracting(Response::getStatusCode, Response::getMessage)
                .contains(500, FAILED_DESERIALISE_MESSAGE);
    }

    @Test
    public void callingAddEndpointWhenJsonIsValidShouldAddEntityToDbAndReturn200Response() throws IOException
    {
        assertThat(apiResource.countOfEntities()).isEqualTo(0);
        final var response = apiResource.createMartianEntity(Files.readString(martianEntity));

        assertThat(apiResource.countOfEntities()).isEqualTo(1);
        assertThat(response)
                .extracting(Response::getStatusCode, Response::getMessage)
                .contains(200, String.format("Successfully created and uploaded entity to DB with id [%s]", response.getId()));
    }

    @Test
    public void callingRetrieveEndpointWhenIdNotPresentReturn404Response() throws IOException
    {
        final var response = apiResource.retrieveMartianEntity(NON_EXISTENT_ID, Clearance.TOP_LEVEL_CLEARANCE);
        assertThat(response)
                .extracting(Response::getStatusCode, Response::getMessage)
                .contains(404, String.format(RETRIEVAL_404_MESSAGE, NON_EXISTENT_ID));
    }

    @Test
    public void callingRetrieveEndpointWhenUserNotPermittedReturn404Response() throws IOException
    {
        final var responseContainingId = apiResource.createMartianEntity(Files.readString(martianEntity));
        final var response = apiResource.retrieveMartianEntity(responseContainingId.getId(), Clearance.ACCESS_RESTRICTED);
        assertThat(response)
                .extracting(Response::getStatusCode, Response::getMessage)
                .contains(404, String.format(RETRIEVAL_404_MESSAGE, responseContainingId.getId()));
    }

    @Test
    public void callingRetrieveEndpointWhenIdIsPresentShouldRetrieveEntityFromDbAndReturn200Response() throws IOException
    {
        final var originalEntity = Deserialiser.deserialiseMartianEntity(Files.readString(martianEntity));

        final var responseContainingId = apiResource.createMartianEntity(Files.readString(martianEntity));
        final var response = apiResource.retrieveMartianEntity(responseContainingId.getId(), Clearance.TOP_LEVEL_CLEARANCE);
        assertThat(deserialiseJson(response.getMessage()))
                .extracting(MartianEntity::getSpecies, MartianEntity::getClearanceRequired)
                .containsExactly(originalEntity.getSpecies(), originalEntity.getClearanceRequired());
    }

    @Test
    public void callingUpdateEndpointWhenIdNonExistentShouldReturn404Response() throws IOException
    {
        final var response = apiResource.updateMartianEntity(Files.readString(updatedMartianEntity), NON_EXISTENT_ID, Clearance.TOP_LEVEL_CLEARANCE);

        assertThat(response)
                .extracting(Response::getStatusCode, Response::getMessage)
                .contains(404, UPDATE_404_MESSAGE);
    }

    @Test
    public void callingUpdateEndpointWhenUserNotClearedShouldReturn404Response() throws IOException
    {
        final var responseContainingId = apiResource.createMartianEntity(Files.readString(martianEntity));
        final var response = apiResource.updateMartianEntity(Files.readString(updatedMartianEntity), responseContainingId.getId(), Clearance.ACCESS_RESTRICTED);

        assertThat(response)
                .extracting(Response::getStatusCode, Response::getMessage)
                .contains(404, UPDATE_404_MESSAGE);
    }

    @Test
    public void callingUpdateEndpointWhenJsonIsInvalidShouldReturn500Response() throws IOException
    {
        final var responseContainingId = apiResource.createMartianEntity(Files.readString(martianEntity));
        final var response = apiResource.updateMartianEntity("Fake Json", responseContainingId.getId(), Clearance.TOP_LEVEL_CLEARANCE);

        assertThat(response)
                .extracting(Response::getStatusCode, Response::getMessage)
                .contains(500, FAILED_DESERIALISE_MESSAGE);
    }

    @Test
    public void callingUpdateEndpointWhenJsonIsValidShouldAddEntityToDbAndReturn200Response() throws IOException
    {
        final var responseContainingId = apiResource.createMartianEntity(Files.readString(martianEntity));
        final var originalRetrievedEntity = deserialiseJson(apiResource.retrieveMartianEntity(responseContainingId.getId(), Clearance.TOP_LEVEL_CLEARANCE).getMessage());

        final var response = apiResource.updateMartianEntity(Files.readString(updatedMartianEntity), responseContainingId.getId(), Clearance.TOP_LEVEL_CLEARANCE);
        final var updatedRetrievedEntry = deserialiseJson(apiResource.retrieveMartianEntity(responseContainingId.getId(), Clearance.TOP_LEVEL_CLEARANCE).getMessage());

        assertThat(response)
                .extracting(Response::getStatusCode, Response::getMessage)
                .contains(200, String.format("Successfully updated entity in DB with id [%s]", responseContainingId.getId()));
        assertThat(originalRetrievedEntity)
                .extracting(MartianEntity::getSpecies, MartianEntity::getClearanceRequired)
                .doesNotContain(updatedRetrievedEntry.getSpecies(), updatedRetrievedEntry.getClearanceRequired());
    }

    @Test
    public void callingDeleteEndpointWhenIdIsInvalidShouldReturn404Response() throws IOException
    {
        final var responseContainingId = apiResource.createMartianEntity(Files.readString(martianEntity));
        final var response = apiResource.deleteMartianEntry(NON_EXISTENT_ID, Clearance.TOP_LEVEL_CLEARANCE);

        assertThat(response)
                .extracting(Response::getStatusCode, Response::getMessage)
                .contains(404, "The entity you are trying to delete is either not present in the database or you lack permissions to execute the deletion");
        assertThat(apiResource.retrieveMartianEntity(responseContainingId.getId(), Clearance.TOP_LEVEL_CLEARANCE))
                .extracting(Response::getStatusCode, Response::getMessage)
                .contains(200);
    }

    @Test
    public void callingDeleteEndpointWhenClearanceIsInsufficientShouldReturn404Response() throws IOException
    {
        final var responseContainingId = apiResource.createMartianEntity(Files.readString(martianEntity));
        final var response = apiResource.deleteMartianEntry(responseContainingId.getId(), Clearance.ACCESS_RESTRICTED);

        assertThat(response)
                .extracting(Response::getStatusCode, Response::getMessage)
                .contains(404, "The entity you are trying to delete is either not present in the database or you lack permissions to execute the deletion");
        assertThat(apiResource.retrieveMartianEntity(responseContainingId.getId(), Clearance.TOP_LEVEL_CLEARANCE))
                .extracting(Response::getStatusCode, Response::getMessage)
                .contains(200);
    }

    @Test
    public void callingDeleteEndpointWheIdIsValidAndClearanceSufficientShouldAddEntityToDbAndReturn200Response() throws IOException
    {
        final var responseContainingId = apiResource.createMartianEntity(Files.readString(martianEntity));
        final var response = apiResource.deleteMartianEntry(responseContainingId.getId(), Clearance.TOP_LEVEL_CLEARANCE);

        assertThat(response)
                .extracting(Response::getStatusCode, Response::getMessage)
                .contains(200, String.format("Deleted martian from db with id [%s]", responseContainingId.getId()));
        assertThat(apiResource.retrieveMartianEntity(responseContainingId.getId(), Clearance.TOP_LEVEL_CLEARANCE))
                .extracting(Response::getStatusCode, Response::getMessage)
                .contains(404, String.format(RETRIEVAL_404_MESSAGE, responseContainingId.getId()));
    }

    private MartianEntity deserialiseJson(String json)
    {
        return Deserialiser.deserialiseMartianEntity(json);
    }
}
