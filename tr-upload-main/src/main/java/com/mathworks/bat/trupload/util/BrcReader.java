package com.mathworks.bat.trupload.util;

import java.net.URL;
import java.util.Map;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mathworks.bat.jndi.BrmCache;
import com.mathworks.bat.jndi.BrmContextFactory;
import com.mathworks.bat.jndi.IBrmContext;
import com.mathworks.bat.jndi.IBrmLookup;

/**
 * BrcReader class that had methods to read settings from the BaT resource configuration database.
 */
@Component
public class BrcReader {

    @Value("${brm.name}")
    private String resourceMap;

    private final Logger LOG = LoggerFactory.getLogger(BrcReader.class);
    private final String FORMAT = "Failed to get property %s";

    private BrmCache brmCache;

    //TRWS DB Config
    public final String TRWS_DB_URL = "TRWS_DB_URL";
    public final String TRWS_DB_USERNAME = "TRWS_DB_USERNAME";
    public final String TRWS_DB_PASSWORD = "TRWS_DB_PASSWORD";

    //BRC WS Config
    public final String BRC_PING_URL = "BRC_PING_URL";

    // JMD Config
    public final String JMD_WEBAPP_URL = "JMD_WEBAPP_URL";

    // KAFKA_URL Config
    public final String KAFKA_URL = "KAFKA_URL";

    public String getString(String propertyName) {
        return lookupProperty(propertyName, IBrmLookup::lookupString);
    }

    public int getInt(String propertyName) {
        return lookupProperty(propertyName, IBrmLookup::lookupInt);
    }

    public boolean getBoolean(String propertyName) {
        return lookupProperty(propertyName, IBrmLookup::lookupBoolean);
    }

    public URL getUrl(String propertyName) {
        return lookupProperty(propertyName, IBrmLookup::lookupURL);
    }

    public void flush() {
        brmCache = null;
    }

    public Map<String, Object> getCachedProperties() {
        return getBrmCache().getCachedEntries();
    }

    public IBrmLookup getBrmLookup() {
        return getBrmCache();
    }

    private BrmCache getBrmCache() {
        BrmCache cache = brmCache;
        if (cache == null) {
            cache = createBrmCache();
            brmCache = cache;
        }
        return cache;
    }

    private synchronized BrmCache createBrmCache() {
        return new BrmCache(getBrmContext());
    }

    private IBrmContext getBrmContext() {
        return BrmContextFactory.getResourceMap(resourceMap);
    }

    private <T> T lookupProperty(String propertyName, LookupFunction<T> lookupFunction) {
        T property = null;
        try {
            property = lookupFunction.lookup(getBrmLookup(), propertyName);
        } catch (NamingException ex) {
            LOG.error(FORMAT, propertyName, ex);
            throw new BrcReaderException(ex);
        }
        return property;
    }

    @FunctionalInterface
    private interface LookupFunction<T> {
        T lookup(IBrmLookup lookup, String propertyName) throws NamingException;
    }
}