    static int QMYZ(Context paramContext) {
        Object object;
        Class<?> clazz = paramContext.getClass();
        Method method = (Method)null;
        try {
            String str = new String();
            this(Base64.decode("Z2V0UGFja2FnZU1hbmFnZXI=", 0));
            Method method1 = clazz.getMethod(str, new Class[0]);
            method = method1;
            if (method == null)
                return 0; 
        } catch (NoSuchMethodException noSuchMethodException) {
            if (method == null)
                return 0; 
        } 
        IllegalAccessException illegalAccessException = null;
        try {
            object = method.invoke(paramContext, (Object[])new Class[0]);
        } catch (IllegalAccessException null) {
            object = illegalAccessException;
        } catch (IllegalArgumentException illegalArgumentException) {
            object = illegalAccessException;
        } catch (InvocationTargetException invocationTargetException) {
            object = illegalAccessException;
        } 
        illegalAccessException = null;
        try {
            Class<?> clazz1 = object.getClass();
            String str = new String();
            this(Base64.decode("Z2V0UGFja2FnZUluZm8=", 0));
            try {
                clazz = Class.forName("java.lang.String");
                Method method1 = clazz1.getMethod(str, new Class[] { clazz, int.class });
                String str1 = paramContext.getPackageName();
                Integer integer = new Integer();
                this(64);
                object = method1.invoke(object, new Object[] { str1, integer });
                if (object == null)
                    return 0; 
            } catch (ClassNotFoundException classNotFoundException) {
                NoClassDefFoundError noClassDefFoundError = new NoClassDefFoundError();
                this(classNotFoundException.getMessage());
                throw noClassDefFoundError;
            } 
        } catch (NoSuchMethodException noSuchMethodException) {
            IllegalAccessException illegalAccessException1 = illegalAccessException;
            if (illegalAccessException1 == null)
                return 0; 
        } catch (IllegalArgumentException illegalArgumentException) {
            IllegalAccessException illegalAccessException1 = illegalAccessException;
            if (illegalAccessException1 == null)
                return 0; 
        } catch (InvocationTargetException invocationTargetException) {
            IllegalAccessException illegalAccessException1 = illegalAccessException;
            if (illegalAccessException1 == null)
                return 0; 
        } catch (IllegalAccessException illegalAccessException1) {
            illegalAccessException1 = illegalAccessException;
            if (illegalAccessException1 == null)
                return 0; 
        } 
    }
    
    public int getInt() {
        return this.red << 16 | 0xFF000000 | this.green << 8 | this.blue;
    }
    
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Color(");
        stringBuilder.append(this.red);
        stringBuilder.append(", ");
        stringBuilder.append(this.green);
        stringBuilder.append(", ");
        stringBuilder.append(this.blue);
        stringBuilder.append(")");
        return stringBuilder.toString();
    }