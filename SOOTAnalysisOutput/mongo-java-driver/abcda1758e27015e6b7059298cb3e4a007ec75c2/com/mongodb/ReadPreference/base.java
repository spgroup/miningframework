package com.mongodb;

import com.mongodb.ReplicaSetStatus.ReplicaSetNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ReadPreference {

    ReadPreference() {
    }

    public abstract boolean isSlaveOk();

    public abstract DBObject toDBObject();

    public abstract String getName();

    abstract ReplicaSetNode getNode(ReplicaSetStatus.ReplicaSet set);

    private static class PrimaryReadPreference extends ReadPreference {

        private PrimaryReadPreference() {
        }

        @Override
        public boolean isSlaveOk() {
            return false;
        }

        @Override
        public String toString() {
            return getName();
        }

        @Override
        public boolean equals(final Object o) {
            return o != null && getClass() == o.getClass();
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }

        @Override
        ReplicaSetNode getNode(ReplicaSetStatus.ReplicaSet set) {
            return set.getMaster();
        }

        @Override
        public DBObject toDBObject() {
            return new BasicDBObject("mode", getName());
        }

        @Override
        public String getName() {
            return "primary";
        }
    }

    @Deprecated
    public static class TaggedReadPreference extends ReadPreference {

        public TaggedReadPreference(Map<String, String> tags) {
            if (tags == null || tags.size() == 0) {
                throw new IllegalArgumentException("tags can not be null or empty");
            }
            _tags = new BasicDBObject(tags);
            List<DBObject> maps = splitMapIntoMultipleMaps(_tags);
            _pref = new TaggableReadPreference.SecondaryReadPreference(maps.get(0), getRemainingMaps(maps));
        }

        public TaggedReadPreference(DBObject tags) {
            if (tags == null || tags.keySet().size() == 0) {
                throw new IllegalArgumentException("tags can not be null or empty");
            }
            _tags = tags;
            List<DBObject> maps = splitMapIntoMultipleMaps(_tags);
            _pref = new TaggableReadPreference.SecondaryReadPreference(maps.get(0), getRemainingMaps(maps));
        }

        public DBObject getTags() {
            DBObject tags = new BasicDBObject();
            for (String key : _tags.keySet()) tags.put(key, _tags.get(key));
            return tags;
        }

        @Override
        public boolean isSlaveOk() {
            return _pref.isSlaveOk();
        }

        @Override
        ReplicaSetNode getNode(ReplicaSetStatus.ReplicaSet set) {
            return _pref.getNode(set);
        }

        @Override
        public DBObject toDBObject() {
            return _pref.toDBObject();
        }

        @Override
        public String getName() {
            return _pref.getName();
        }

        private static List<DBObject> splitMapIntoMultipleMaps(DBObject tags) {
            List<DBObject> tagList = new ArrayList<DBObject>(tags.keySet().size());
            for (String key : tags.keySet()) {
                tagList.add(new BasicDBObject(key, tags.get(key).toString()));
            }
            return tagList;
        }

        private DBObject[] getRemainingMaps(final List<DBObject> maps) {
            if (maps.size() <= 1) {
                return new DBObject[0];
            }
            return maps.subList(1, maps.size() - 1).toArray(new DBObject[maps.size() - 1]);
        }

        private final DBObject _tags;

        private final ReadPreference _pref;
    }

    public static ReadPreference primary() {
        return _PRIMARY;
    }

    public static ReadPreference primaryPreferred() {
        return _PRIMARY_PREFERRED;
    }

    public static TaggableReadPreference primaryPreferred(DBObject firstTagSet, DBObject... remainingTagSets) {
        return new TaggableReadPreference.PrimaryPreferredReadPreference(firstTagSet, remainingTagSets);
    }

    public static ReadPreference secondary() {
        return _SECONDARY;
    }

    public static TaggableReadPreference secondary(DBObject firstTagSet, DBObject... remainingTagSets) {
        return new TaggableReadPreference.SecondaryReadPreference(firstTagSet, remainingTagSets);
    }

    public static ReadPreference secondaryPreferred() {
        return _SECONDARY_PREFERRED;
    }

    public static TaggableReadPreference secondaryPreferred(DBObject firstTagSet, DBObject... remainingTagSets) {
        return new TaggableReadPreference.SecondaryPreferredReadPreference(firstTagSet, remainingTagSets);
    }

    public static ReadPreference nearest() {
        return _NEAREST;
    }

    public static ReadPreference valueOf(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        name = name.toLowerCase();
        if (name.equals(_PRIMARY.getName().toLowerCase())) {
            return _PRIMARY;
        }
        if (name.equals(_SECONDARY.getName().toLowerCase())) {
            return _SECONDARY;
        }
        if (name.equals(_SECONDARY_PREFERRED.getName().toLowerCase())) {
            return _SECONDARY_PREFERRED;
        }
        if (name.equals(_PRIMARY_PREFERRED.getName().toLowerCase())) {
            return _PRIMARY_PREFERRED;
        }
        if (name.equals(_NEAREST.getName().toLowerCase())) {
            return _NEAREST;
        }
        throw new IllegalArgumentException("No match for read preference of " + name);
    }

    public static TaggableReadPreference valueOf(String name, DBObject firstTagSet, final DBObject... remainingTagSets) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        name = name.toLowerCase();
        if (name.equals(_SECONDARY.getName().toLowerCase())) {
            return new TaggableReadPreference.SecondaryReadPreference(firstTagSet, remainingTagSets);
        }
        if (name.equals(_SECONDARY_PREFERRED.getName().toLowerCase())) {
            return new TaggableReadPreference.SecondaryPreferredReadPreference(firstTagSet, remainingTagSets);
        }
        if (name.equals(_PRIMARY_PREFERRED.getName().toLowerCase())) {
            return new TaggableReadPreference.PrimaryPreferredReadPreference(firstTagSet, remainingTagSets);
        }
        if (name.equals(_NEAREST.getName().toLowerCase())) {
            return new TaggableReadPreference.NearestReadPreference(firstTagSet, remainingTagSets);
        }
        throw new IllegalArgumentException("No match for read preference of " + name);
    }

    public static TaggableReadPreference nearest(DBObject firstTagSet, DBObject... remainingTagSets) {
        return new TaggableReadPreference.NearestReadPreference(firstTagSet, remainingTagSets);
    }

    @Deprecated
    public static final ReadPreference PRIMARY = new PrimaryReadPreference();

    @Deprecated
    public static final ReadPreference SECONDARY = new TaggableReadPreference.SecondaryPreferredReadPreference();

    @Deprecated
    public static ReadPreference withTags(Map<String, String> tags) {
        return new TaggedReadPreference(tags);
    }

    @Deprecated
    public static ReadPreference withTags(final DBObject tags) {
        return new TaggedReadPreference(tags);
    }

    private static final ReadPreference _PRIMARY = new PrimaryReadPreference();

    private static final ReadPreference _SECONDARY = new TaggableReadPreference.SecondaryReadPreference();

    private static final ReadPreference _SECONDARY_PREFERRED = new TaggableReadPreference.SecondaryPreferredReadPreference();

    private static final ReadPreference _PRIMARY_PREFERRED = new TaggableReadPreference.PrimaryPreferredReadPreference();

    private static final ReadPreference _NEAREST = new TaggableReadPreference.NearestReadPreference();
}
