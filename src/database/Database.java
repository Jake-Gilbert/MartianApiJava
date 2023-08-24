package database;

import models.Clearance;
import models.MartianEntity;

import java.util.*;

public class Database
{
    private final List<String> presentIds = new ArrayList<>();
    private final Map<String, MartianEntity> presentMartians = new HashMap<>();
    public String addMartian(String species, Clearance clearance)
    {
        final var martian = new MartianEntity(species, clearance);
        final var id = UUID.randomUUID().toString();
        presentMartians.put(id, martian);
        presentIds.add(id);
        return id;
    }

    public boolean deleteMartian(String id, Clearance clearance)
    {
        return removeEntityFromIdAndMartianCollection(id, clearance);
    }

    public MartianEntity retrieveMartian(String id, Clearance clearance)
    {
        if (!presentIds.contains(id))
        {
            return null;
        }
        final var martian = presentMartians.get(id);

        if (!martian.getClearanceRequired().authorisesClearanceLevel(clearance))
        {
            return null;
        }
        return martian;
    }

    //Permission has already been granted when this method is called
    public boolean updateMartian(String id, MartianEntity entity)
    {
        if (!presentIds.contains(id))
        {
            return false;
        }
        presentMartians.put(id, entity);
        return true;
    }


    private boolean removeEntityFromIdAndMartianCollection(String id, Clearance clearance)
    {
        if (presentIds.contains(id) && presentMartians.containsKey(id))
        {
            if (!presentMartians.get(id).getClearanceRequired().authorisesClearanceLevel(clearance))
            {
                return false;
            }
            presentIds.remove(id);
            presentMartians.remove(id);
            return true;
        }
        return false;
    }
}
