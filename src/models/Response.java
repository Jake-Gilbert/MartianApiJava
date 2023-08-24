package models;

public class Response
{

    private final String message;
    private final int statusCode;

    public Response(String message, int statusCode)
    {
        this.message = message;
        this.statusCode = statusCode;
    }
}
