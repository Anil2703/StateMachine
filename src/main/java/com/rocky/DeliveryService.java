package com.rocky;



import java.util.*;

public class DeliveryService {

    public static void main(String[] args) {


        List<DeliveryEvent> deliveryEvents = new ArrayList<>();

        Map<String, DeliveryResult> resultMap = processEventsUsingFSM(deliveryEvents);

        for (Map.Entry<String, DeliveryResult> entry : resultMap.entrySet()) {
            String trackingId = entry.getKey();
            DeliveryResult result = entry.getValue();

            System.out.println("Tracking ID: " + trackingId);
            System.out.println("Final State: " + result.finalState);

            if (!result.invalidEvents.isEmpty()) {
                System.out.println("Invalid Events:");
                for (DeliveryEvent e : result.invalidEvents) {
                    System.out.println("  → " + e.type + " at timestamp " + e.timestamp);
                }
            } else {
                System.out.println("Invalid Events: None");
            }

            System.out.println("-------------------------");
        }
    }


    /*
    1. result must show final state (last valid event type) and list of invalid events for every delivery.
        (change the signature to reflect that)
    2. `trackingId` is uniquely identify a single delivery.
    3. deliveries are processed by `timestamp` in ascending order.
    4. `DELIVERED`, `LOST` and `RETURNED` — final statuses.
    5. the following sequence represents valid transitions between statuses for a delivery:
    `CREATED` -> `PICKED_UP` -> `IN_TRANSIT` -> any final status.
    6. events that does not follow the sequence above are considered invalid.
    7. any events after valid final status should be ignored (i.e. automatically invalid).

    (for example in sequence `CREATED`->`LOST`->`PICKED_UP`, the final state would be `PICKED_UP`
        and `LOST` is invalid event)
    */
    public static Object processEvents(List<DeliveryEvent> events) {
        return null;
    }

    //trackingId-1
    //trackingId-2
    //trackingId-3

    public static class DeliveryEvent {
        public String trackingId;
        public long timestamp;
        public EventType type;

        public enum EventType {
            CREATED, PICKED_UP, IN_TRANSIT,
            DELIVERED, LOST, RETURNED

        }

        public String getTrackingId() {
            return trackingId;
        }

        public void setTrackingId(String trackingId) {
            this.trackingId = trackingId;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public EventType getType() {
            return type;
        }

        public void setType(EventType type) {
            this.type = type;
        }

        public DeliveryEvent(String trackingId, long timestamp, EventType type) {
            this.trackingId = trackingId;
            this.timestamp = timestamp;
            this.type = type;
        }

        @Override
        public String toString() {
            return "DeliveryEvent{" +
                    "trackingId='" + trackingId + '\'' +
                    ", timestamp=" + timestamp +
                    ", type=" + type +
                    '}';
        }
    }


    //Using Conditional Statements

    public static class DeliveryResult {
        public DeliveryEvent.EventType finalState;
        public List<DeliveryEvent> invalidEvents = new ArrayList<>();

        public DeliveryResult(DeliveryEvent.EventType finalState) {
            this.finalState = finalState;
        }
    }

    public static Map<String, DeliveryResult> processEventsUsingConditionalStatements(List<DeliveryEvent> events) {

        List<DeliveryEvent> events2 = List.of(
                new DeliveryEvent("D1", 1, DeliveryEvent.EventType.CREATED),
                new DeliveryEvent("D1", 2, DeliveryEvent.EventType.LOST),
                new DeliveryEvent("D1", 3, DeliveryEvent.EventType.PICKED_UP),
                new DeliveryEvent("D2", 1, DeliveryEvent.EventType.CREATED),
                new DeliveryEvent("D2", 2, DeliveryEvent.EventType.PICKED_UP),
                new DeliveryEvent("D2", 3, DeliveryEvent.EventType.IN_TRANSIT),
                new DeliveryEvent("D2", 4, DeliveryEvent.EventType.DELIVERED),
                new DeliveryEvent("D2", 5, DeliveryEvent.EventType.RETURNED)
        );
        events.addAll(events2);


        Map<String, List<DeliveryEvent>> grouped = new HashMap<>();

        for (DeliveryEvent e : events) {
            grouped.computeIfAbsent(e.trackingId, k -> new ArrayList<>()).add(e);
        }

        Map<String, DeliveryResult> result = new HashMap<>();

        for (Map.Entry<String, List<DeliveryEvent>> entry : grouped.entrySet()) {

            //Preparing Data
            String trackingId = entry.getKey();
            List<DeliveryEvent> deliveryEvents = entry.getValue();
            deliveryEvents.sort(Comparator.comparingLong(e -> e.timestamp));

            DeliveryEvent.EventType currentState = null;
            boolean isFinal = false;
            List<DeliveryEvent> invalids = new ArrayList<>();


            //Processing Data
            for (DeliveryEvent event : deliveryEvents) {
                if (isFinal) {
                    invalids.add(event);
                    continue;
                }

                if (currentState == null) {
                    if (event.type == DeliveryEvent.EventType.CREATED) {
                        currentState = event.type;
                    } else {
                        invalids.add(event);
                    }
                } else if (currentState == DeliveryEvent.EventType.CREATED) {
                    if (event.type == DeliveryEvent.EventType.PICKED_UP) {
                        currentState = event.type;
                    } else {
                        invalids.add(event);
                    }
                } else if (currentState == DeliveryEvent.EventType.PICKED_UP) {
                    if (event.type == DeliveryEvent.EventType.IN_TRANSIT) {
                        currentState = event.type;
                    } else {
                        invalids.add(event);
                    }
                } else if (currentState == DeliveryEvent.EventType.IN_TRANSIT) {
                    if (event.type == DeliveryEvent.EventType.DELIVERED ||
                            event.type == DeliveryEvent.EventType.LOST ||
                            event.type == DeliveryEvent.EventType.RETURNED) {
                        currentState = event.type;
                        isFinal = true;
                    } else {
                        invalids.add(event);
                    }
                } else {
                    // already in a final state (should never reach here due to isFinal flag)
                    invalids.add(event);
                }
            }

            DeliveryResult deliveryResult = new DeliveryResult(currentState);
            deliveryResult.invalidEvents.addAll(invalids);
            result.put(trackingId, deliveryResult);
        }

        return result;
    }

    public static Map<String, DeliveryResult> processEventsUsingFSM(List<DeliveryEvent> events) {

        // Sample events to test
        List<DeliveryEvent> events2 = List.of(
                new DeliveryEvent("D1", 1, DeliveryEvent.EventType.CREATED),
                new DeliveryEvent("D1", 2, DeliveryEvent.EventType.LOST),
                new DeliveryEvent("D1", 3, DeliveryEvent.EventType.PICKED_UP),
                new DeliveryEvent("D2", 1, DeliveryEvent.EventType.CREATED),
                new DeliveryEvent("D2", 2, DeliveryEvent.EventType.PICKED_UP),
                new DeliveryEvent("D2", 3, DeliveryEvent.EventType.IN_TRANSIT),
                new DeliveryEvent("D2", 4, DeliveryEvent.EventType.DELIVERED),
                new DeliveryEvent("D2", 5, DeliveryEvent.EventType.RETURNED)
        );
        events.addAll(events2);

        // 1. Define valid transitions
        Map<DeliveryEvent.EventType, Set<DeliveryEvent.EventType>> transitions = new HashMap<>();
        transitions.put(null, Set.of(DeliveryEvent.EventType.CREATED)); // initial
        transitions.put(DeliveryEvent.EventType.CREATED, Set.of(DeliveryEvent.EventType.PICKED_UP));
        transitions.put(DeliveryEvent.EventType.PICKED_UP, Set.of(DeliveryEvent.EventType.IN_TRANSIT));
        transitions.put(DeliveryEvent.EventType.IN_TRANSIT,
                Set.of(DeliveryEvent.EventType.DELIVERED, DeliveryEvent.EventType.LOST, DeliveryEvent.EventType.RETURNED));

        Set<DeliveryEvent.EventType> finalStates = Set.of(
                DeliveryEvent.EventType.DELIVERED,
                DeliveryEvent.EventType.LOST,
                DeliveryEvent.EventType.RETURNED
        );

        // 2. Group by trackingId
        Map<String, List<DeliveryEvent>> grouped = new HashMap<>();
        for (DeliveryEvent e : events) {
            grouped.computeIfAbsent(e.trackingId, k -> new ArrayList<>()).add(e);
        }

        // 3. Process each delivery
        Map<String, DeliveryResult> result = new HashMap<>();

        for (Map.Entry<String, List<DeliveryEvent>> entry : grouped.entrySet()) {
            String trackingId = entry.getKey();
            List<DeliveryEvent> deliveryEvents = entry.getValue();
            deliveryEvents.sort(Comparator.comparingLong(e -> e.timestamp));

            DeliveryEvent.EventType currentState = null;
            boolean isFinal = false;
            List<DeliveryEvent> invalids = new ArrayList<>();

            for (DeliveryEvent event : deliveryEvents) {
                if (isFinal) {
                    invalids.add(event);
                    continue;
                }

                Set<DeliveryEvent.EventType> allowedNext = transitions.getOrDefault(currentState, Collections.emptySet());
                if (allowedNext.contains(event.type)) {
                    currentState = event.type;
                    if (finalStates.contains(currentState)) {
                        isFinal = true;
                    }
                } else {
                    invalids.add(event);
                }
            }

            DeliveryResult deliveryResult = new DeliveryResult(currentState);
            deliveryResult.invalidEvents.addAll(invalids);
            result.put(trackingId, deliveryResult);
        }

        return result;
    }
}
