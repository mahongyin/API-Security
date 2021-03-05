# 指定代码的压缩级别 0 - 7，一般都是5
-optimizationpasses 5

# 指定混淆时的算法，后面的参数是一个过滤器
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# 混淆时记录日志（混淆后生产映射文件 map 类名 -> 转化后类名的映射
-verbose

# 不使用大小写混合，混淆后类名称为小写
-dontusemixedcaseclassnames

# 不跳过非公共的库的类成员
-dontskipnonpubliclibraryclassmembers

# 如果应用程序引入的有jar包，并且混淆jar包里面的class
-dontskipnonpubliclibraryclasses

# 不做预校验，preverify是proguard的4个功能之一，android不需要preverify，去掉这一步加快混淆速度
-dontpreverify

#优化时允许访问并修改有修饰符的类和类的成员
-allowaccessmodification

# 指定映射文件的名称
#-printmapping proguardMapping.txt

# 把混淆类中的方法名也混淆
-useuniqueclassmembernames

# 将文件来源重命名为“SourceFile”字符串
-renamesourcefileattribute SourceFile

# 保留行号
-keepattributes SourceFile,LineNumberTable

# 保持所有实现 Serializable 接口的类成员
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Fragment 不需要在 AndroidManifest.xml 中注册，需要额外保护下
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.app.Fragment

# 保持测试相关的代码
-dontnote junit.framework.**
-dontnote junit.runner.**
-dontwarn android.test.**
-dontwarn android.support.test.**
-dontwarn org.junit.**

# 类型转换错误，添加如下代码以便过滤泛型（不写可能会出现类型转换错误）,即避免泛型被混淆
-keepattributes Signature

# 假如项目中有用到注解，应加入这行配置,对 JSON 实体映射也很重要, eg : fastjson
-keepattributes *Annotation*

# 抛出异常时保留代码行数
-keepattributes SourceFile,LineNumberTable

# 不混淆去除 AIDL
-keep class * implements android.os.IInterface {*;}

# 不混淆输入的类文件
#-dontobfuscate

# 不优化输入的类文件，优化可能会造成一些潜在风险，不能保证在所有版本的Dalvik上都正常运行
-dontoptimize

#不混淆任何包含native方法的类的类名以及native方法名。
-keepclasseswithmembernames class * {
    native <methods>;
}

#不混淆任何一个 View 中的 setXxx() 和  getXxx()方法，因为属性动画需要有相应的 setter 和 getter 的方法实现，混淆了就无法工作了
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

#不混淆枚举中的 values() 和 valueOf() 方法
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#不混淆 Parcelable 实现类中的 CREATOR 字段，CREATOR 字段是绝对不能改变的，包括大小写都不能变，不然整个 Parcelable 工作机制都会失败
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

# 对 android.support 包下的代码不警告
# 因为 support 包中有很多代码都是在高版本中使用的，如果我们的项目指定的版本比较低在打包时就会给予警告。
# 不过 support 包中所有的代码都在版本兼容性上做足了判断，因此不用担心代码会出问题，所以直接忽略警告就可以了。
-dontwarn android.support.**