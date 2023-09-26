package com.btxtech.server.persistence;

import com.btxtech.server.system.FilePropertiesService;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * Created by Beat
 * on 01.09.2017.
 */
@Singleton
public class MongoDbService {
    public static final String RAZARION_DB = "razarion";
    @Inject
    private FilePropertiesService filePropertiesService;
    private MongoClient mongoClient;

    @PostConstruct
    public void postConstruct() {
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        mongoClient = new MongoClient(filePropertiesService.getMongoDbHost(), MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build());
    }

    public MongoDatabase getRazarionDatabase() {
        return mongoClient.getDatabase(RAZARION_DB);
    }

    public <T> MongoCollection<T> getCollection(CollectionName collectionNameName, Class<T> theClass) {
        return getRazarionDatabase().getCollection(collectionNameName.getName(), theClass);
    }

    public <T> void storeObject(T entity, Class<T> theClass, CollectionName collectionNameName) {
        getCollection(collectionNameName, theClass).insertOne(entity);
    }

    public enum CollectionName {
        IN_GAME_TRACKING("in_game_tracking"),
        PLANET_BACKUP("planet_backup"),
        SERVER_ITEM_TRACKING("server_item_tracking"),
        STARTUP_TRACKING("startup_tracking");

        private String name;

        CollectionName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
