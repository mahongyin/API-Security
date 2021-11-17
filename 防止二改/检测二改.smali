.class public Lutils/tool/CheckSignature;
.super Ljava/lang/Object;
.source "CheckSignature.java"


# static fields
.field static final synthetic $assertionsDisabled:Z


# direct methods
.method static constructor <clinit>()V
    .registers 0

    return-void
.end method

.method public constructor <init>()V
    .registers 1

    .line 18
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method private static checkPMProxy(Landroid/content/pm/PackageManager;)Z
    .registers 2
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "packageManager"
        }
    .end annotation

    .line 131
    invoke-static {p0}, Lutils/tool/CheckSignature;->nowPMName_str(Landroid/content/pm/PackageManager;)Ljava/lang/String;

    move-result-object p0

    const-string v0, "android.content.pm.IPackageManager$Stub$Proxy"

    invoke-virtual {v0, p0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p0

    return p0
.end method

.method public static check_start(Landroid/app/Activity;ZZZ)V
    .registers 14
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0,
            0x0,
            0x0,
            0x0
        }
        names = {
            "thisActivity",
            "isCheckPMProxy",
            "isCheckClass",
            "isFullToast"
        }
    .end annotation

    .line 21
    invoke-virtual {p0}, Landroid/app/Activity;->getApplicationContext()Landroid/content/Context;

    move-result-object v0

    invoke-virtual {p0}, Landroid/app/Activity;->getPackageName()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lutils/tool/CheckSignature;->getSingInfoMD5(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    const/4 v1, 0x1

    if-eqz p1, :cond_18

    .line 24
    invoke-virtual {p0}, Landroid/app/Activity;->getPackageManager()Landroid/content/pm/PackageManager;

    move-result-object p1

    invoke-static {p1}, Lutils/tool/CheckSignature;->checkPMProxy(Landroid/content/pm/PackageManager;)Z

    move-result p1

    goto :goto_19

    :cond_18
    const/4 p1, 0x1

    :goto_19
    const/4 v2, 0x0

    if-eqz p2, :cond_41

    const-string p2, "bin.mt.apksignaturekillerplus.HookApplication"

    .line 33
    invoke-static {p2}, Lutils/tool/CheckSignature;->isHasClass(Ljava/lang/String;)Z

    move-result p2

    const-string v3, "me.weishu.exposed.ExposedApplication"

    .line 34
    invoke-static {v3}, Lutils/tool/CheckSignature;->isHasClass(Ljava/lang/String;)Z

    move-result v3

    const-string v4, "com.bug.load.ProxyApplication"

    .line 35
    invoke-static {v4}, Lutils/tool/CheckSignature;->isHasClass(Ljava/lang/String;)Z

    move-result v4

    const-string v5, "com.minusoneapp.HookApplication"

    .line 36
    invoke-static {v5}, Lutils/tool/CheckSignature;->isHasClass(Ljava/lang/String;)Z

    move-result v5

    const-string v6, "arm.StubApp"

    .line 37
    invoke-static {v6}, Lutils/tool/CheckSignature;->isHasClass(Ljava/lang/String;)Z

    move-result v6

    const-string v7, "入口"

    .line 38
    invoke-static {v7}, Lutils/tool/CheckSignature;->isHasClass(Ljava/lang/String;)Z

    move-result v7

    goto :goto_47

    :cond_41
    const/4 p2, 0x0

    const/4 v3, 0x0

    const/4 v4, 0x0

    const/4 v5, 0x0

    const/4 v6, 0x0

    const/4 v7, 0x1

    :goto_47
    const-string v8, "已通过"

    const-string v9, "不通过"

    if-nez p2, :cond_5a

    if-nez v3, :cond_5a

    if-nez v4, :cond_5a

    if-nez v5, :cond_5a

    if-nez v6, :cond_5a

    if-eqz v7, :cond_5a

    move-object v3, v8

    const/4 p2, 0x1

    goto :goto_5c

    :cond_5a
    move-object v3, v9

    const/4 p2, 0x0

    .line 54
    :goto_5c
    invoke-static {v0}, Ljava/util/Objects;->requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;

    move-object v4, v0

    check-cast v4, Ljava/lang/String;

    const-string v5, "xiaobeiit"

    invoke-virtual {v4, v5}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v4

    if-eqz v4, :cond_70

    if-eqz p1, :cond_70

    if-eqz p2, :cond_70

    const/4 p2, 0x1

    goto :goto_71

    :cond_70
    const/4 p2, 0x0

    :goto_71
    if-eqz p1, :cond_74

    goto :goto_75

    :cond_74
    move-object v8, v9

    :goto_75
    if-nez p2, :cond_7a

    const-string p1, "签名检验失败"

    goto :goto_7c

    :cond_7a
    const-string p1, "签名检验通过"

    :goto_7c
    if-eqz p3, :cond_a7

    const/4 p3, 0x7

    new-array p3, p3, [Ljava/lang/Object;

    aput-object p1, p3, v2

    const-string p1, "\n\n计算签名:\n\n"

    aput-object p1, p3, v1

    const/4 p1, 0x2

    aput-object v0, p3, p1

    const/4 p1, 0x3

    const-string v0, "\n\n代理检测:"

    aput-object v0, p3, p1

    const/4 p1, 0x4

    aput-object v8, p3, p1

    const/4 p1, 0x5

    const-string v0, "\n\n类名检测:"

    aput-object v0, p3, p1

    const/4 p1, 0x6

    aput-object v3, p3, p1

    const-string p1, "%s%s%s%s%s%s%s"

    .line 69
    invoke-static {p1, p3}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object p1

    .line 73
    invoke-virtual {p0}, Landroid/app/Activity;->getApplicationContext()Landroid/content/Context;

    move-result-object p3

    invoke-static {p3, p1}, Lutils/tool/CheckSignature;->copyString(Landroid/content/Context;Ljava/lang/String;)V

    :cond_a7
    if-nez p2, :cond_b7

    .line 78
    invoke-virtual {p0}, Landroid/app/Activity;->getApplicationContext()Landroid/content/Context;

    move-result-object p2

    invoke-static {p2, p1, v2}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p1

    invoke-virtual {p1}, Landroid/widget/Toast;->show()V

    .line 79
    invoke-virtual {p0, v1}, Landroid/app/Activity;->moveTaskToBack(Z)Z

    :cond_b7
    return-void
.end method

.method public static copyString(Landroid/content/Context;Ljava/lang/String;)V
    .registers 3
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0,
            0x0
        }
        names = {
            "context",
            "copyStr"
        }
    .end annotation

    const-string v0, "clipboard"

    .line 85
    invoke-virtual {p0, v0}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object p0

    check-cast p0, Landroid/content/ClipboardManager;

    const-string v0, "Label"

    .line 86
    invoke-static {v0, p1}, Landroid/content/ClipData;->newPlainText(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Landroid/content/ClipData;

    move-result-object p1

    if-eqz p0, :cond_13

    .line 88
    invoke-virtual {p0, p1}, Landroid/content/ClipboardManager;->setPrimaryClip(Landroid/content/ClipData;)V

    :cond_13
    return-void
.end method

.method private static getMD5(Ljava/lang/String;)Ljava/lang/String;
    .registers 6
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "plainText"
        }
    .end annotation

    .line 107
    :try_start_0
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "MD5"

    .line 108
    invoke-static {v1}, Ljava/security/MessageDigest;->getInstance(Ljava/lang/String;)Ljava/security/MessageDigest;

    move-result-object v1

    .line 109
    invoke-virtual {p0}, Ljava/lang/String;->getBytes()[B

    move-result-object p0

    invoke-virtual {v1, p0}, Ljava/security/MessageDigest;->update([B)V

    .line 110
    invoke-virtual {v1}, Ljava/security/MessageDigest;->digest()[B

    move-result-object p0

    .line 112
    array-length v1, p0

    const/4 v2, 0x0

    :goto_18
    if-ge v2, v1, :cond_33

    aget-byte v3, p0, v2

    if-gez v3, :cond_20

    add-int/lit16 v3, v3, 0x100

    :cond_20
    const/16 v4, 0x10

    if-ge v3, v4, :cond_29

    const-string v4, "0"

    .line 118
    invoke-virtual {v0, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 120
    :cond_29
    invoke-static {v3}, Ljava/lang/Integer;->toHexString(I)Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    add-int/lit8 v2, v2, 0x1

    goto :goto_18

    .line 123
    :cond_33
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p0
    :try_end_37
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_37} :catch_38

    goto :goto_39

    :catch_38
    const/4 p0, 0x0

    :goto_39
    return-object p0
.end method

.method private static getSignatureString(Landroid/content/pm/Signature;)Ljava/lang/String;
    .registers 7
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "sig"
        }
    .end annotation

    .line 178
    invoke-virtual {p0}, Landroid/content/pm/Signature;->toByteArray()[B

    move-result-object p0

    :try_start_4
    const-string v0, "SHA1"

    .line 181
    invoke-static {v0}, Ljava/security/MessageDigest;->getInstance(Ljava/lang/String;)Ljava/security/MessageDigest;

    move-result-object v0

    .line 182
    invoke-virtual {v0, p0}, Ljava/security/MessageDigest;->digest([B)[B

    move-result-object p0

    .line 183
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    .line 184
    array-length v1, p0

    const/4 v2, 0x0

    :goto_15
    if-ge v2, v1, :cond_2d

    aget-byte v3, p0, v2

    and-int/lit16 v3, v3, 0xff

    or-int/lit16 v3, v3, 0x100

    .line 185
    invoke-static {v3}, Ljava/lang/Integer;->toHexString(I)Ljava/lang/String;

    move-result-object v3

    const/4 v4, 0x3

    const/4 v5, 0x1

    invoke-virtual {v3, v5, v4}, Ljava/lang/String;->substring(II)Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    add-int/lit8 v2, v2, 0x1

    goto :goto_15

    .line 187
    :cond_2d
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p0
    :try_end_31
    .catch Ljava/security/NoSuchAlgorithmException; {:try_start_4 .. :try_end_31} :catch_32

    goto :goto_38

    :catch_32
    move-exception p0

    .line 189
    invoke-virtual {p0}, Ljava/security/NoSuchAlgorithmException;->printStackTrace()V

    const-string p0, "error!"

    :goto_38
    return-object p0
.end method

.method private static getSignatures(Landroid/content/Context;Ljava/lang/String;)[Landroid/content/pm/Signature;
    .registers 3
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0,
            0x0
        }
        names = {
            "context",
            "packageName"
        }
    .end annotation

    .line 168
    :try_start_0
    invoke-virtual {p0}, Landroid/content/Context;->getPackageManager()Landroid/content/pm/PackageManager;

    move-result-object p0

    const/16 v0, 0x40

    invoke-virtual {p0, p1, v0}, Landroid/content/pm/PackageManager;->getPackageInfo(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;

    move-result-object p0

    .line 169
    iget-object p0, p0, Landroid/content/pm/PackageInfo;->signatures:[Landroid/content/pm/Signature;
    :try_end_c
    .catch Landroid/content/pm/PackageManager$NameNotFoundException; {:try_start_0 .. :try_end_c} :catch_d

    return-object p0

    :catch_d
    move-exception p0

    .line 171
    invoke-virtual {p0}, Landroid/content/pm/PackageManager$NameNotFoundException;->printStackTrace()V

    const/4 p0, 0x0

    return-object p0
.end method

.method private static getSingInfoMD5(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;
    .registers 2
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0,
            0x0
        }
        names = {
            "context",
            "packageName"
        }
    .end annotation

    .line 152
    invoke-static {p0, p1}, Lutils/tool/CheckSignature;->getSignatures(Landroid/content/Context;Ljava/lang/String;)[Landroid/content/pm/Signature;

    move-result-object p0

    .line 153
    invoke-static {p0}, Ljava/util/Objects;->requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;

    check-cast p0, [Landroid/content/pm/Signature;

    array-length p1, p0

    if-lez p1, :cond_14

    const/4 p1, 0x0

    aget-object p0, p0, p1

    .line 154
    invoke-static {p0}, Lutils/tool/CheckSignature;->getSignatureString(Landroid/content/pm/Signature;)Ljava/lang/String;

    move-result-object p0

    goto :goto_15

    :cond_14
    const/4 p0, 0x0

    :goto_15
    if-eqz p0, :cond_1b

    .line 158
    invoke-static {p0}, Lutils/tool/CheckSignature;->getMD5(Ljava/lang/String;)Ljava/lang/String;

    move-result-object p0

    :cond_1b
    return-object p0
.end method

.method private static isHasClass(Ljava/lang/String;)Z
    .registers 1
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "className"
        }
    .end annotation

    .line 96
    :try_start_0
    invoke-static {p0}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;
    :try_end_3
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_3} :catch_5

    const/4 p0, 0x1

    goto :goto_6

    :catch_5
    const/4 p0, 0x0

    :goto_6
    return p0
.end method

.method private static nowPMName_str(Landroid/content/pm/PackageManager;)Ljava/lang/String;
    .registers 3
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "packageManager"
        }
    .end annotation

    .line 137
    :try_start_0
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v0

    const-string v1, "mPM"

    invoke-virtual {v0, v1}, Ljava/lang/Class;->getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;

    move-result-object v0

    const/4 v1, 0x1

    .line 138
    invoke-virtual {v0, v1}, Ljava/lang/reflect/Field;->setAccessible(Z)V

    .line 139
    invoke-virtual {v0, p0}, Ljava/lang/reflect/Field;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object p0

    .line 142
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object p0

    invoke-virtual {p0}, Ljava/lang/Class;->getName()Ljava/lang/String;

    move-result-object p0
    :try_end_1a
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_1a} :catch_1b

    goto :goto_1d

    :catch_1b
    const-string p0, ""

    :goto_1d
    return-object p0
.end method
