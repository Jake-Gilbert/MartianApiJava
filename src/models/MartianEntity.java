package models;

public class MartianEntity
{
    private final String species;
    private final Clearance clearanceRequired;

    public MartianEntity(String species, Clearance clearanceRequired)
    {
        this.species = species;
        this.clearanceRequired = clearanceRequired;
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
