package com.ristexsoftware.knappy.translation;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.TreeMap;

import com.ristexsoftware.knappy.util.Debugger;

import lombok.Getter;
import lombok.Setter;

/**
 * Provides an interface between locale files and your plugin.
 */
public class LocaleProvider {
    Debugger debug = new Debugger(getClass());

    @Getter
    @Setter
    private File localeFolder;

    @Getter
    private ConcurrentHashMap<String, Locale> loadedLocales = new ConcurrentHashMap<>();

    /**
     * The default locale to use when
     */
    @Getter
    private Locale defaultLocale;

    /**
     * Default translation variables.
     */
    @Getter
    private TreeMap<String, String> defaultTranslations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public LocaleProvider(File localeFolder) {
        this.localeFolder = localeFolder;
        if (!localeFolder.exists())
            localeFolder.mkdir();   
    }
    
    /**
     * Load a locale with the given name.
     */
    public boolean loadLocale(String name) {
        if (!name.endsWith(".yml"))
            name += ".yml";

        return loadLocale(new File(localeFolder, name));
    }

    /**
     * Load a locale using a file.
     */
    public boolean loadLocale(File file) {
        debug.reset().print("Looking for localization in " + file.getName() + "...");

        if (!file.exists() || file.isDirectory()) {
            debug.print("Could not find file - does not exist, or is directory");
            return false;
        }

        // Ensure the same locale isn't loaded twice.
        if (loadedLocales.containsKey(file.getName().substring(0, file.getName().length() - 4))) {
            debug.print("Skipping loading locale - already loaded");
            return false;
        }
        
        Locale locale = new Locale(file);
        if (!locale.getIsValid()) {
            debug.print("Encountered an error while loading the locale configuration - skipping load");
            return false;
        }

        loadedLocales.put(file.getName().substring(0, file.getName().length() - 4), locale);        
        debug.print("Successfully loaded locale '" + file.getName().substring(0, file.getName().length() - 4) + "'");
        
        return true;
    }

    /**
     * Load all available locales.
     * @return The number of new locales loaded.
     */
    public int loadAllLocales() {
        int accumulator = 0;

        for (File file : this.localeFolder.listFiles()) {
            if (!file.getName().endsWith(".yml"))
                continue;

            if (loadLocale(file))
                ++accumulator;
        }

        debug.print("Loaded " + accumulator + " locales");
        return accumulator;
    }

    /**
     * Register a default translation to be inserted into the vars object when calling `translate`.
     */
    public void registerDefaultTranslation(String key, String value) {
        defaultTranslations.put(key, value);
    }

    /**
     * Translate a localization with the given variables.
     */
    public String translate(String node, Map<String, String> vars) {
        if (node == null || node.equals(""))
            return null;

        String message = get(node);
        if (message == null)
            return null;
        
        defaultTranslations.forEach((k,v) -> vars.put(k, v));
        return Translation.translate(node, "&", vars);
    }

    /**
     * Translate a localization without color.
     */
    public String translateNoColor(String node, Map<String, String> vars) {
        if (node == null || node.equals(""))
            return null;

        String message = get(node);
        if (message == null)
            return null;
        
        defaultTranslations.forEach((k,v) -> vars.put(k, v));
        return Translation.translateVariables(message, vars);
    }

    /**
     * Get a localized value using the default locale.
     */
    public String get(String node) {
        return defaultLocale == null ? null : defaultLocale.get(node);
    }

    /**
     * Get a localized value using an enum of node values.
     */
    public String get(Enum<?> node) {
        return get(node.name().toLowerCase().replace("_", "-"));
    }

    /**
     * Get a localized value using the specified locale.
     */
    public String get(String name, String node) throws IllegalArgumentException {
        checkForLoadedLocale(name);
        return loadedLocales.get(name).get(node);
    }

    /**
     * Fetch a loaded locale.
     */
    public Locale getLocale(String name) throws IllegalArgumentException {
        checkForLoadedLocale(name);
        return loadedLocales.get(name);
    }

    /**
     * Set the default locale to use.
     * @return True if the default locale was set without error.
     */
    public boolean setDefaultLocale(String name) {
        try {
            checkForLoadedLocale(name);
        } catch (IllegalArgumentException e) {
            return false;
        }
   
        defaultLocale = loadedLocales.get(name);
        return true;
    }

    /**
     * Checks if a locale with the given name is loaded. Throws `IllegalArgumentException` if not found.
     */
    private void checkForLoadedLocale(String name) throws IllegalArgumentException {
        if (!loadedLocales.containsKey(name))
            throw new IllegalArgumentException("Locale " + name + " is not loaded");
    }

}