package models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class MartianEntity implements Serializable
{
    @JsonProperty("species")
    private final String species;
    @JsonProperty("clearanceRequired")
    private final Clearance clearanceRequired;

    public MartianEntity(String species, Clearance clearanceRequired)
    {
        this.species = species;
        this.clearanceRequired = clearanceRequired;
    }

    public MartianEntity()
    {
        this.species = null;
        this.clearanceRequired = null;
    }

    public String getSpecies()
    {
        return species;
    }

    public Clearance getClearanceRequired()
    {
        return clearanceRequired;
    }
}
