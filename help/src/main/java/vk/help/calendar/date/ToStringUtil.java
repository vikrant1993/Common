package vk.help.calendar.date;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ToStringUtil {

    static String getText(Object aObject) {
        return getTextAvoidCyclicRefs(aObject, null, null);
    }

    static String getTextAvoidCyclicRefs(Object aObject, Class aSpecialClass, String aMethodName) {
        StringBuilder result = new StringBuilder();
        addStartLine(aObject, result);

        Method[] methods = aObject.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (isContributingMethod(method, aObject.getClass())) {
                addLineForGetXXXMethod(aObject, method, result, aSpecialClass, aMethodName);
            }
        }

        addEndLine(result);
        return result.toString();
    }

    private static final String fGET_CLASS = "getClass";
    private static final String fCLONE = "clone";
    private static final String fHASH_CODE = "hashCode";
    private static final String fTO_STRING = "toString";

    private static final String fGET = "get";
    private static final Object[] fNO_ARGS = new Object[0];
    private static final Class[] fNO_PARAMS = new Class[0];

    private static final String fINDENT = "";
    private static final String fAVOID_CIRCULAR_REFERENCES = "[circular reference]";
    private static final Logger fLogger = Util.getLogger(ToStringUtil.class);
    private static final String NEW_LINE = System.getProperty("line.separator");

    private static Pattern PASSWORD_PATTERN = Pattern.compile("password", Pattern.CASE_INSENSITIVE);

    private ToStringUtil() {
    }

    private static void addStartLine(Object aObject, StringBuilder aResult) {
        aResult.append(aObject.getClass().getName());
        aResult.append(" {");
        aResult.append(NEW_LINE);
    }

    private static void addEndLine(StringBuilder aResult) {
        aResult.append("}");
        aResult.append(NEW_LINE);
    }

    private static boolean isContributingMethod(Method aMethod, Class aNativeClass) {
        boolean isPublic = Modifier.isPublic(aMethod.getModifiers());
        boolean hasNoArguments = aMethod.getParameterTypes().length == 0;
        boolean hasReturnValue = aMethod.getReturnType() != Void.TYPE;
        boolean returnsNativeObject = aMethod.getReturnType() == aNativeClass;
        boolean isMethodOfObjectClass = aMethod.getName().equals(fCLONE) || aMethod.getName().equals(fGET_CLASS) || aMethod.getName().equals(fHASH_CODE) || aMethod.getName().equals(fTO_STRING);
        return isPublic && hasNoArguments && hasReturnValue && !isMethodOfObjectClass && !returnsNativeObject;
    }

    private static void addLineForGetXXXMethod(Object aObject, Method aMethod, StringBuilder aResult, Class aCircularRefClass, String aCircularRefMethodName) {
        aResult.append(fINDENT);
        aResult.append(getMethodNameMinusGet(aMethod));
        aResult.append(": ");
        Object returnValue = getMethodReturnValue(aObject, aMethod);
        if (returnValue != null && returnValue.getClass().isArray()) {
            aResult.append(Util.getArrayAsString(returnValue));
        } else {
            if (aCircularRefClass == null) {
                aResult.append(returnValue);
            } else {
                if (aCircularRefClass == returnValue.getClass()) {
                    Method method = getMethodFromName(aCircularRefClass, aCircularRefMethodName);
                    if (isContributingMethod(method, aCircularRefClass)) {
                        returnValue = getMethodReturnValue(returnValue, method);
                        aResult.append(returnValue);
                    } else {
                        aResult.append(fAVOID_CIRCULAR_REFERENCES);
                    }
                }
            }
        }
        aResult.append(NEW_LINE);
    }

    private static String getMethodNameMinusGet(Method aMethod) {
        String result = aMethod.getName();
        if (result.startsWith(fGET)) {
            result = result.substring(fGET.length());
        }
        return result;
    }

    private static Object getMethodReturnValue(Object aObject, Method aMethod) {
        Object result = null;
        try {
            result = aMethod.invoke(aObject, fNO_ARGS);
        } catch (IllegalAccessException ex) {
            vomit(aObject, aMethod);
        } catch (InvocationTargetException ex) {
            vomit(aObject, aMethod);
        }
        result = dontShowPasswords(result, aMethod);
        return result;
    }

    private static Method getMethodFromName(Class aSpecialClass, String aMethodName) {
        Method result = null;
        try {
            result = aSpecialClass.getMethod(aMethodName, fNO_PARAMS);
        } catch (NoSuchMethodException ex) {
            vomit(aSpecialClass, aMethodName);
        }
        return result;
    }


    private static void vomit(Object aObject, Method aMethod) {
        fLogger.severe("Cannot get return value using reflection. Class: " + aObject.getClass().getName() + " Method: " + aMethod.getName());
    }

    private static void vomit(Class aSpecialClass, String aMethodName) {
        fLogger.severe("Reflection fails to get no-arg method named: " + Util.quote(aMethodName) + " for class: " + aSpecialClass.getName());
    }

    private static Object dontShowPasswords(Object aReturnValue, Method aMethod) {
        Object result = aReturnValue;
        Matcher matcher = PASSWORD_PATTERN.matcher(aMethod.getName());
        if (matcher.find()) {
            result = "****";
        }
        return result;
    }

    private static final class Ping {
        public void setPong(Pong aPong) {
            fPong = aPong;
        }

        public Pong getPong() {
            return fPong;
        }

        public Integer getId() {
            return 123;
        }

        public String getUserPassword() {
            return "blah";
        }

        public String toString() {
            return getText(this);
        }

        private Pong fPong;
    }

    private static final class Pong {
        public void setPing(Ping aPing) {
            fPing = aPing;
        }

        public Ping getPing() {
            return fPing;
        }

        public String toString() {
            return getTextAvoidCyclicRefs(this, Ping.class, "getId");
        }

        private Ping fPing;
    }

    public static void main(String... args) {
        List<String> list = new ArrayList<>();
        list.add("blah");
        list.add("blah");
        list.add("blah");
        System.out.println(ToStringUtil.getText(list));
        StringTokenizer parser = new StringTokenizer("This is the end.");
        System.out.println(ToStringUtil.getText(parser));
        Ping ping = new Ping();
        Pong pong = new Pong();
        ping.setPong(pong);
        pong.setPing(ping);
        System.out.println(ping);
        System.out.println(pong);
    }
}