package com.codingchili.core.storage;

import com.codingchili.core.files.Configurations;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Validator;

/**
 * @author Robin Duda
 *         <p>
 *         Basic filter types for use before indexes with query support.
 */
abstract class BaseFilter<Value> {
    private Validator validator = new Validator();

    boolean queryExact(Value value, String attribute, Comparable compare) {
        return compare.equals(Serializer.json(value).getValue(attribute));
    }

    boolean querySimilar(Value value, String attribute, Comparable compare) {
        int feedBackLength = Configurations.storage().getMinFeedbackChars();

        // FIXME: 2016-12-03 awaiting support for complex queries
        if (compare.equals("*")) {
            return true;
        }

        if (validator.plainText(compare) && compare.toString().length() >= feedBackLength) {
            Object source = Serializer.json(value).getValue(attribute);

            if (source != null) {
                return source.toString().startsWith(compare.toString());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    boolean queryRange(Value value, String attribute, int from, int to) {
        Integer ordinal = Serializer.json(value).getInteger(attribute);
        return (ordinal != null && ordinal >= from && ordinal <= to);
    }
}
