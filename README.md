# SM2: Spaced Repetition Scheduling Library for Java
![badge](https://github.com/mattcarpenter/sm2/workflows/Java%20CI/badge.svg?branch=master)

SM2 is a lightweight SRS scheduling library for Java based on the SuperMemo SM-2 algorithm developed by Piotr Wozniak.

The primary function of this library is to calculate SRS review due-dates when given a set of items and a list of review outcomes. Querying and SRS review session management are not handled by this library.

## Adding SM2 to your build

SM2's Maven group ID is `net.mattcarpenter.srs` and its artifact ID is `sm2`. To add a dependency on SM2 using Maven, use the following:

```$xslt
<dependency>
  <groupId>net.mattcarpenter.srs</groupId>
  <artifactId>sm2</artifactId>
  <version>1.0</version>
</dependency>
```

## Usage

The following example illustrates how item reviews are applied to compute the item's review schedule.

```java
Scheduler scheduler = Scheduler.builder().build();

// Create an Item with default starting easiness factor

Item item = Item.builder().build();

// A session represents a contiguous block of time where a user reviews one or more items until a satisfactory score is
// given to each item being reviewed.

Session session1 = new Session();

// One or more reviews for any given item may be applied to a session. As per the SM-2 algorithm, if an item lapsed
// during a session but was answered correctly later in the same session, the item's EF will not be adjusted.

Review review1 = new Review(item, 5);
session1.applyReview(review1);

// Update due dates and intervals for each item applied to the session.

scheduler.applySession(session1);

System.out.println(item.getDueDate()); // 2019-01-02

Session session2 = new Session();
Review review2 = new Review(item, 5);
session2.applyReview(review2);
scheduler.applySession(session2);

System.out.println(item.getDueDate()); // 2019-01-07
```

The scheduler may be built with a custom consecutive-correct-count-to-interval mapping. See the following example.

```java
Map<Integer, Float> intervalMapping = ImmutableMap.of(
        2, 4f
);

Scheduler scheduler = Scheduler.builder()
        .consecutiveCorrectIntervalMappings(intervalMapping)
        .build();
```

In this example, the scheduler will use an interval of 4 days instead of the SM-2 default 6 days when an item has been answered correctly in the second consecutive session.
