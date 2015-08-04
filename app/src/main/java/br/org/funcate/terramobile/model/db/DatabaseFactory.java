package br.org.funcate.terramobile.model.db;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bogo on 03/08/15.
 */
public class DatabaseFactory {

    private DatabaseFactory()
    {
    }

    public static DatabaseHelper getDatabase(Context context, String databaseName)
    {
        if(ApplicationDatabase.DATABASE_NAME.equalsIgnoreCase(databaseName))
        {
            return new ApplicationDatabase(context);
        }
        else
        {
            return new ProjectDatabase(context, databaseName);
        }

    }
}
