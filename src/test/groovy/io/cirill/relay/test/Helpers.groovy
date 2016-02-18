package io.cirill.relay.test

/**
 * Created by mcirillo on 2/17/16.
 */
class Helpers {

    public static boolean mapsAreEqual(Map a, Map b) {
        for (entry in a.entrySet()) {
            if (b.keySet().contains(entry.key)) {
                if (List.isAssignableFrom(entry.value.getClass()) && List.isAssignableFrom(b[entry.key].getClass())) {
                    if (!listsAreEqual(entry.value as List, b[entry.key] as List)) {
                        return false
                    }
                } else if (Map.isAssignableFrom(entry.value.getClass()) && Map.isAssignableFrom(b[entry.key].getClass())) {
                    if (!mapsAreEqual(entry.value as Map, b[entry.key] as Map)) {
                        return false
                    }
                } else if (!entry.value.equals(b[entry.key])) {
                    return false
                }
            } else {
                return false
            }
        }

        true
    }

    public static boolean listsAreEqual(List a, List b) {
        if (a.size() != b.size()) {
            return false
        }

        for (elem in a) {
            if (List.isAssignableFrom( elem.getClass() )) {
                def lists = b.findAll({ Map.isAssignableFrom(it.getClass()) })
                if (lists.count({ listsAreEqual(elem as List, it as List) }) != 1) {
                    return false
                }
            } else if (Map.isAssignableFrom(elem.getClass())) {
                def maps = b.findAll({ Map.isAssignableFrom(it.getClass()) })
                if (maps.count({ mapsAreEqual(elem as Map, it as Map) }) != 1) {
                    return false
                }
            } else if (b.count({ it.equals(elem) }) != 1) {
                return false
            }
        }

        true
    }
}
