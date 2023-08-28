import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.Database;
import models.Clearance;
import models.Response;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

@Path("MarsApi")
public class MarsApiResource
{
    private final Database database = new Database();
    private final ObjectMapper jsonHandler = new ObjectMapper();
    private final static String FAILED_DESERIALISE_MESSAGE = "Failed to deserialise entity. Please submit a valid JSON body";
    @Path("/retrieve/{id}/{clearance}")
    public Response retrieveMartianEntity(@PathParam("id") String id, @PathParam("clearance") Clearance clearance) throws JsonProcessingException
    {
        final var entity = database.retrieveMartian(id, clearance);
        if (entity == null)
        {
            //For security reasons only return 404. Returning 403 implies a resource exists.
            return new Response(String.format("Either no entity exists with id [%s] or user lacks the permissions to access the entity", id), 404);
        }
        return new Response(jsonHandler.writeValueAsString(entity), 200);
    }

    @Path("/uploadEntity")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createMartianEntity(String payload)
    {
        final var deserialisedEntity = Deserialiser.deserialiseMartianEntity(payload);
        if (deserialisedEntity == null)
        {
            return new Response(FAILED_DESERIALISE_MESSAGE, 500);
        }

        final var id = database.addMartian(deserialisedEntity.getSpecies(), deserialisedEntity.getClearanceRequired());
        return new Response(String.format("Successfully created and uploaded entity to DB with id [%s]", id), 200, id);
    }

    @Path("/deleteEntity/{id}/{clearance}")
    public Response deleteMartianEntry(@PathParam("id") String id, @PathParam("clearance") Clearance clearance)
    {
        final var entityDeleted = database.deleteMartian(id, clearance);
        if (!entityDeleted)
        {
            return new Response("Entity with this id not found in database", 404);
        }
        return new Response(String.format("Deleted martian from db with id [%s]", id), 200);
    }

    @Path("/modifyEntity/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateMartianEntity(String payload, @PathParam("id") String id, @PathParam("clearance") Clearance clearance)
    {
        final var deserialisedEntity = Deserialiser.deserialiseMartianEntity(payload);
        if (deserialisedEntity == null)
        {
            return new Response(FAILED_DESERIALISE_MESSAGE, 500);
        }

        final var retrievedEntity = database.retrieveMartian(id, clearance);

        if (retrievedEntity == null)
        {
            return new Response("Either you don't have permission or the entity you have requested to update does not exist in the database", 404);
        }

        if (!database.updateMartian(id, deserialisedEntity))
        {
            return new Response(String.format("The entity you're trying to update with id [%s] no longer exists in the database", id), 404);
        }
        return new Response(String.format("Successfully updated entity in DB with id [%s]", id), 200);
    }

    @Path("/count")
    public int countOfEntities()
    {
        return database.count();
    }
}
