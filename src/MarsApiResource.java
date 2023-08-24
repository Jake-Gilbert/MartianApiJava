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
    @Path("/retrieve/{id}/{clearance}")
    public Response retrieve(@PathParam("id") String id, @PathParam("clearance") Clearance clearance) throws JsonProcessingException
    {
        final var entity = database.retrieveMartian(id, clearance);
        return new Response(jsonHandler.writeValueAsString(entity), 200);
    }

    @Path("/uploadEntity")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(String payload)
    {
        final var deserialisedEntity = Deserialiser.deserialiseMartianEntity(payload);
        if (deserialisedEntity == null)
        {
            return new Response("Failed to deserialise entity", 500);
        }

        final var id = database.addMartian(deserialisedEntity.getSpecies(), deserialisedEntity.getClearanceRequired());
        return new Response(String.format("Successfully created and uploaded entity to DB with id [%s]", id), 200);
    }

    @Path("/deleteEntity/{id}/{clearance}")
    public Response deleteMartianEntry(@PathParam("id") String id, @PathParam("clearance") Clearance clearance)
    {
        final var entityDeleted = database.deleteMartian(id, clearance);
        if (!entityDeleted)
        {
            return new Response("Martian species with this id not found in database", 404);
        }
        return new Response(String.format("Deleted martian from db with id [%s]", id), 200);
    }

    @Path("/modifyEntity/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateMartian(String payload, @PathParam("id") String id, @PathParam("clearance") Clearance clearance)
    {
        final var deserialisedEntity = Deserialiser.deserialiseMartianEntity(payload);
        if (deserialisedEntity == null)
        {
            return new Response("Failed to deserialise entity", 500);
        }

        final var retrievedEntity = database.retrieveMartian(id, clearance);

        if (retrievedEntity == null)
        {
            return new Response("You don't have permission or the entity you have requested to update does not exist in the database", 404);
        }

        if (!database.updateMartian(id, deserialisedEntity))
        {
            return new Response(String.format("The entity you're trying to update with id [%s] no longer exists in the database", id), 404);
        }
        return new Response(String.format("Successfully created and uploaded entity to DB with id [%s]", id), 200);
    }
}
