package Realm.Model;

/**
 * @author Robin Duda
 *         Defines how a attribute/value modifier is interpreted.
 *
 *         max - the max value
 *         current - the current value
 *
 *         example:
 *
 *         modify: health, as [current], value -0.01
 *
 */
public enum As {
    max, current
}
