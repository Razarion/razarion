package com.btxtech.server.persistence.item;

import com.btxtech.server.persistence.MongoDbService;
import com.btxtech.server.user.SecurityCheck;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.conversions.Bson;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 08.01.2018.
 */
@Singleton
public class ItemTrackerAccess {
    private static final int MAX_RESULT = 1000;
    @Inject
    private MongoDbService mongoDbService;

    @SecurityCheck
    public List<ItemTracking> read(ItemTrackingSearch itemTrackingSearch) {
        MongoCollection<ItemTracking> dbCollection = mongoDbService.getCollection(MongoDbService.CollectionName.SERVER_ITEM_TRACKING, ItemTracking.class);

        Bson filter = null;
        if (itemTrackingSearch.getFrom() != null) {
            filter = Filters.gte("timeStamp", itemTrackingSearch.getFrom());
        }
        if (itemTrackingSearch.getTo() != null) {
            Bson lte = Filters.lte("timeStamp", itemTrackingSearch.getTo());
            if (filter == null) {
                filter = lte;
            } else {
                filter = Filters.and(filter, lte);
            }
        }
        if(itemTrackingSearch.getHumanPlayerId() != null) {
            Bson lte = Filters.or(Filters.eq("actorHumanPlayerId", itemTrackingSearch.getHumanPlayerId()), Filters.eq("targetHumanPlayerId", itemTrackingSearch.getHumanPlayerId()));
            if (filter == null) {
                filter = lte;
            } else {
                filter = Filters.and(filter, lte);
            }
        }
        FindIterable<ItemTracking> findIterable;
        if (filter != null) {
            findIterable = dbCollection.find(filter);
        } else {
            findIterable = dbCollection.find();
        }
        List<ItemTracking> itemTrackings = new ArrayList<>();
        if (itemTrackingSearch.getCount() != null) {
            findIterable = findIterable.limit(itemTrackingSearch.getCount());
        } else {
            findIterable = findIterable.limit(MAX_RESULT);
        }
        findIterable.sort(Sorts.descending("timeStamp")).forEach((Consumer<ItemTracking>) itemTrackings::add);
        return itemTrackings;
    }

}
