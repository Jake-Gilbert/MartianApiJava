package models;

import java.util.Optional;

public class Response
{

    private final String message;
    private final int statusCode;
    private final Optional<String> id;
    public Response(String message, int statusCode)
    {
        this(message, statusCode, null);
    }

    public Response(String message, int statusCode, String id)
    {
        this.message = message;
        this.statusCode = statusCode;
        this.id = Optional.ofNullable(id);
    }

    public String getMessage()
    {
        return message;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public String getId()
    {
        return id.orElse("No id detected");
    }

}
