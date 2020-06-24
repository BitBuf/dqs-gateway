package dev.dewy.dqs.gateway.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class SessionHandler
{
    public String fileName;

    public SessionHandler(String fileName)
    {
        this.fileName = fileName;
    }

    public void cacheUser(String server, String uuid)
    {
        YMLParser parser = new YMLParser(this.getFile());

        if (parser.exists(server))
        {
            List<String> str = parser.getList(server);

            str.add(uuid);

            parser.set(server, str);
            parser.save();

            return;
        }

        List<String> str = new ArrayList<>();

        str.add(uuid);

        parser.set(server, str);
        parser.save();
    }

    public void purgeUserCache(String server)
    {
        YMLParser parser = new YMLParser(this.getFile());

        parser.remove(server);
        parser.save();
    }

    public boolean isUserCached(String server, String uuid)
    {
        YMLParser parser = new YMLParser(this.getFile());

        if (!parser.exists(server))
        {
            return false;
        }

        List<String> arr = parser.getList(server);

        return arr.contains(uuid);
    }

    private File getFile()
    {
        File file = new File(fileName);

        if (!file.exists())
        {
            try
            {
                file.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return file;
    }
}
