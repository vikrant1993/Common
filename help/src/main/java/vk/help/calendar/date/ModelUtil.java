package vk.help.calendar.date;

import java.lang.reflect.Array;

final class ModelUtil {

    static String toStringFor(Object aObject) {
        return ToStringUtil.getText(aObject);
    }

    static String toStringAvoidCyclicRefs(Object aObject, Class aSpecialClass, String aMethodName) {
        return ToStringUtil.getTextAvoidCyclicRefs(aObject, aSpecialClass, aMethodName);
    }

    static final int hashCodeFor(Object... aFields) {
        int result = HASH_SEED;
        for (Object field : aFields) {
            result = hash(result, field);
        }
        return result;
    }

    static final int HASH_SEED = 23;

    static int hash(int aSeed, boolean aBoolean) {
        return firstTerm(aSeed) + (aBoolean ? 1 : 0);
    }

    static int hash(int aSeed, char aChar) {
        return firstTerm(aSeed) + aChar;
    }

    static int hash(int aSeed, int aInt) {
        return firstTerm(aSeed) + aInt;
    }

    static int hash(int aSeed, long aLong) {
        return firstTerm(aSeed) + (int) (aLong ^ (aLong >>> 32));
    }

    static int hash(int aSeed, float aFloat) {
        return hash(aSeed, Float.floatToIntBits(aFloat));
    }

    static int hash(int aSeed, double aDouble) {
        return hash(aSeed, Double.doubleToLongBits(aDouble));
    }

    static int hash(int aSeed, Object aObject) {
        int result = aSeed;
        if (aObject == null) {
            result = hash(result, 0);
        } else if (!isArray(aObject)) {
            result = hash(result, aObject.hashCode());
        } else {
            int length = Array.getLength(aObject);
            for (int idx = 0; idx < length; ++idx) {
                Object item = Array.get(aObject, idx);
                result = hash(result, item);
            }
        }
        return result;
    }

    static Boolean quickEquals(Object aThis, Object aThat) {
        Boolean result = null;
        if (aThis == aThat) {
            result = Boolean.TRUE;
        } else {
            Class<?> thisClass = aThis.getClass();
            if (!thisClass.isInstance(aThat)) {
                result = Boolean.FALSE;
            }
        }
        return result;
    }

    static boolean equalsFor(Object[] aThisSignificantFields, Object[] aThatSignificantFields) {
        if (aThisSignificantFields.length != aThatSignificantFields.length) {
            throw new IllegalArgumentException("Array lengths do not match. 'This' length is " + aThisSignificantFields.length + ", while 'That' length is " + aThatSignificantFields.length + ".");
        }

        boolean result = true;
        for (int idx = 0; idx < aThisSignificantFields.length; ++idx) {
            if (!areEqual(aThisSignificantFields[idx], aThatSignificantFields[idx])) {
                result = false;
                break;
            }
        }
        return result;
    }

    static boolean areEqual(boolean aThis, boolean aThat) {
        return aThis == aThat;
    }

    static boolean areEqual(char aThis, char aThat) {
        return aThis == aThat;
    }

    static boolean areEqual(long aThis, long aThat) {
        return aThis == aThat;
    }

    static boolean areEqual(float aThis, float aThat) {
        return Float.floatToIntBits(aThis) == Float.floatToIntBits(aThat);
    }

    static boolean areEqual(double aThis, double aThat) {
        return Double.doubleToLongBits(aThis) == Double.doubleToLongBits(aThat);
    }

    static boolean areEqual(Object aThis, Object aThat) {
        if (isArray(aThis) || isArray(aThat)) {
            throw new IllegalArgumentException("This method does not currently support arrays.");
        }
        return aThis == null ? aThat == null : aThis.equals(aThat);
    }

    enum NullsGo {FIRST, LAST}

    static <T extends Comparable<T>> int comparePossiblyNull(T aThis, T aThat, NullsGo aNullsGo) {
        int EQUAL = 0;
        int BEFORE = -1;
        int AFTER = 1;
        int result = EQUAL;

        if (aThis != null && aThat != null) {
            result = aThis.compareTo(aThat);
        } else {
            if (aThis == null && aThat == null) {
            } else if (aThis == null && aThat != null) {
                result = BEFORE;
            } else if (aThis != null && aThat == null) {
                result = AFTER;
            }
            if (NullsGo.LAST == aNullsGo) {
                result = (-1) * result;
            }
        }
        return result;
    }

    private ModelUtil() {
    }

    private static final int fODD_PRIME_NUMBER = 37;

    private static int firstTerm(int aSeed) {
        return fODD_PRIME_NUMBER * aSeed;
    }

    private static boolean isArray(Object aObject) {
        return aObject != null && aObject.getClass().isArray();
    }
}