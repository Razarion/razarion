package com.btxtech.server.service.tracking;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The pieces the history's aggregation pipelines are built from.
 * <p>
 * These exist because the pipelines were verified the wrong way round: their <em>semantics</em>
 * were checked against production data by running the same stages from a script, which found two
 * real bugs - but the Java that assembles them was never executed. It threw a
 * NullPointerException on every call, because {@code List.of} rejects null and the null in the
 * {@code $cond} is the whole point of it. Building the document is cheap; not building it in a test
 * cost a production endpoint.
 */
class TrackingAttributionTest {

    /**
     * The regression. A null in a $cond branch is how "push nothing" is expressed - $$REMOVE is
     * honoured by $addToSet but not by $push - so the list that carries it has to tolerate one.
     */
    @Test
    void theClickIdAccumulatorCanBeBuiltAtAll() {
        Document clickIds = assertDoesNotThrow(TrackingAttribution::clickIds);

        Document cond = clickIds.get("$push", Document.class);
        List<?> branches = cond.getList("$cond", Object.class);
        assertEquals(3, branches.size());
        assertNotNull(branches.get(0), "the condition");
        assertNotNull(branches.get(1), "the triple to push");
        assertNull(branches.get(2), "the branch that pushes nothing");
    }

    /**
     * A missing field is not null in an aggregation expression - {@code {$ne: [missing, null]}} is
     * true, unlike the same shape in a $match. $ifNull is what makes the two the same absence, and
     * leaving it out silently claimed every row had a click id.
     */
    @Test
    void theEmptinessTestNormalisesMissingToNull() {
        Document notNull = TrackingAttribution.notNull("$fbclid");

        List<?> operands = notNull.getList("$ne", Object.class);
        assertEquals(2, operands.size());
        Document ifNull = (Document) operands.get(0);
        assertEquals(List.of("$fbclid").get(0), ifNull.getList("$ifNull", Object.class).get(0));
        assertNull(ifNull.getList("$ifNull", Object.class).get(1));
        assertNull(operands.get(1));
    }

    /** The stage that drops what the accumulator above pushed as null. */
    @Test
    void theFilterStageRemovesThoseNulls() {
        Document stage = assertDoesNotThrow(() -> TrackingAttribution.filterNulls("clickIds"));

        Document fields = stage.get("$addFields", Document.class);
        Document filter = fields.get("clickIds", Document.class).get("$filter", Document.class);
        assertEquals("$clickIds", filter.getString("input"));
        assertTrue(filter.get("cond", Document.class).getList("$ne", Object.class).contains(null));
    }

    /**
     * The pipeline the history actually runs, built end to end. This is the one that threw: not a
     * helper in isolation but the assembled list, where a single null in the wrong kind of List is
     * enough to take an endpoint down.
     */
    @Test
    void theSessionAttributionPipelineCanBeBuiltAtAll() {
        PageRequestService service = new PageRequestService(null);

        List<Document> pipeline = assertDoesNotThrow(
                () -> service.sessionAttributionPipeline(new java.util.Date(0), new java.util.Date()));

        assertEquals(4, pipeline.size(), "match, sort, group, filter");
        assertTrue(pipeline.get(0).containsKey("$match"));
        assertTrue(pipeline.get(1).containsKey("$sort"));
        assertTrue(pipeline.get(2).containsKey("$group"));
        assertTrue(pipeline.get(3).containsKey("$addFields"));
    }

    /** And with no window at all, which takes the other branch of matchWindow. */
    @Test
    void theSameHoldsWithoutADateRange() {
        PageRequestService service = new PageRequestService(null);

        assertDoesNotThrow(() -> service.sessionAttributionPipeline(null, null));
    }

    /** Absent values stay out of the set rather than becoming a member of it. */
    @Test
    void theSetAccumulatorSkipsAbsentValues() {
        Document addToSet = assertDoesNotThrow(() -> TrackingAttribution.addToSetNonNull("$userAgent"));

        Document ifNull = addToSet.get("$addToSet", Document.class);
        assertEquals("$$REMOVE", ifNull.getList("$ifNull", Object.class).get(1));
    }
}
