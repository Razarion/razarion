package com.btxtech.server.persistence;

import com.btxtech.server.util.DateUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.text.SimpleDateFormat;

/**
 * Created by Beat
 * on 01.09.2017.
 */
@Singleton
public class MongoDbService {
    public static final String RAZARION_DB = "razarion";
    private MongoClient mongoClient;

    @PostConstruct
    public void postConstruct() {
        mongoClient = new MongoClient();
    }

    public MongoDatabase getRazarionDatabase() {
        return mongoClient.getDatabase(RAZARION_DB);
    }

    public MongoCollection<Document> getCollection(CollectionName collectionNameName) {
        return getRazarionDatabase().getCollection(collectionNameName.getName());
    }

    public ObjectMapper setupObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat(DateUtil.JSON_FORMAT_STRING));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    public void storeObject(Object entity, CollectionName collectionNameName) throws JsonProcessingException {
        MongoCollection<Document> dbCollection = getCollection(collectionNameName);
        ObjectMapper objectMapper = setupObjectMapper();
        Document document = Document.parse(objectMapper.writeValueAsString(entity));
        dbCollection.insertOne(document);
    }

    public enum CollectionName {
        IN_GAME_TRACKING("in_game_tracking"),
        PLANET_BACKUP("planet_backup");

        private String name;

        CollectionName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
