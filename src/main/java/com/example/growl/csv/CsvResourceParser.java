package com.example.growl.csv;

import com.example.growl.csv.model.AdOpportunity;
import com.example.growl.csv.model.ChatDataRow;
import com.example.growl.csv.model.Offer;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * Parses the three CSV resources into POJOs. Uses OpenCSV so quoted fields may span lines
 * (needed for {@link CsvResourcePaths#CHAT_DATA}).
 */
public final class CsvResourceParser {

    private CsvResourceParser() {
    }

    public static List<Offer> parseOffers(ClassLoader classLoader) throws IOException {
        return parse(classLoader, CsvResourcePaths.OFFERS, Offer.class);
    }

    public static List<ChatDataRow> parseChatData(ClassLoader classLoader) throws IOException {
        return parse(classLoader, CsvResourcePaths.CHAT_DATA, ChatDataRow.class);
    }

    public static List<AdOpportunity> parseAdOpportunities(ClassLoader classLoader) throws IOException {
        return parse(classLoader, CsvResourcePaths.AD_OPPORTUNITIES, AdOpportunity.class);
    }

    public static List<Offer> parseOffers(InputStream inputStream) throws IOException {
        return parse(inputStream, Offer.class);
    }

    public static List<ChatDataRow> parseChatData(InputStream inputStream) throws IOException {
        return parse(inputStream, ChatDataRow.class);
    }

    public static List<AdOpportunity> parseAdOpportunities(InputStream inputStream) throws IOException {
        return parse(inputStream, AdOpportunity.class);
    }

    private static <T> List<T> parse(ClassLoader classLoader, String classpathLocation, Class<T> type)
            throws IOException {
        InputStream in = classLoader.getResourceAsStream(classpathLocation);
        if (in == null) {
            throw new IllegalArgumentException("Classpath resource not found: " + classpathLocation);
        }
        try (InputStream stream = in) {
            return parse(stream, type);
        }
    }

    private static <T> List<T> parse(InputStream inputStream, Class<T> type) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            HeaderColumnNameMappingStrategy<T> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(type);
            return new CsvToBeanBuilder<T>(reader)
                    .withMappingStrategy(strategy)
                    .build()
                    .parse();
        }
    }
}
