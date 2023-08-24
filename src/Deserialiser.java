import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.MartianEntity;

public class Deserialiser
{
    public static MartianEntity deserialiseMartianEntity(String payload)
    {
        try
        {
            return validateAndDeserialise(payload);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static MartianEntity validateAndDeserialise(String payload) throws JsonProcessingException
    {
        return new ObjectMapper().readValue(payload, MartianEntity.class);
    }
}
