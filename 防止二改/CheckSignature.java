package utils.tool;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.widget.Toast;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class CheckSignature {
    static final /* synthetic */ boolean $assertionsDisabled = false;

    public static void check_start(Activity activity, boolean z, boolean z2, boolean z3) {
        Object obj;
        Object obj2;
        String singInfoMD5 = getSingInfoMD5(activity.getApplicationContext(), activity.getPackageName());
        z = z ? checkPMProxy(activity.getPackageManager()) : true;
        boolean isHasClass;
        boolean isHasClass2;
        boolean isHasClass3;
        boolean isHasClass4;
        boolean isHasClass5;
        if (z2) {
            z2 = isHasClass("bin.mt.apksignaturekillerplus.HookApplication");
            isHasClass = isHasClass("me.weishu.exposed.ExposedApplication");
            isHasClass2 = isHasClass("com.bug.load.ProxyApplication");
            isHasClass3 = isHasClass("com.minusoneapp.HookApplication");
            isHasClass4 = isHasClass("arm.StubApp");
            isHasClass5 = isHasClass("入口");
        } else {
            z2 = false;
            isHasClass = false;
            isHasClass2 = false;
            isHasClass3 = false;
            isHasClass4 = false;
            isHasClass5 = true;
        }
        String str = "已通过";
        String str2 = "不通过";
        if (z2 || isHasClass || isHasClass2 || isHasClass3 || isHasClass4 || !isHasClass5) {
            obj = str2;
            obj2 = null;
        } else {
            obj = str;
            obj2 = 1;
        }
        Objects.requireNonNull(singInfoMD5);
        obj2 = (((String) singInfoMD5).equals("xiaobeiit") && z && obj2 != null) ? 1 : null;
        if (!z) {
            str = str2;
        }
        CharSequence charSequence = obj2 == null ? "签名检验失败" : "签名检验通过";
        if (z3) {
            charSequence = String.format("%s%s%s%s%s%s%s", new Object[]{charSequence, "\n\n计算签名:\n\n", singInfoMD5, "\n\n代理检测:", str, "\n\n类名检测:", obj});
            copyString(activity.getApplicationContext(), charSequence);
        }
        if (obj2 == null) {
            Toast.makeText(activity.getApplicationContext(), charSequence, 0).show();
            activity.moveTaskToBack(true);
        }
    }

    public static void copyString(Context context, String str) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService("clipboard");
        ClipData newPlainText = ClipData.newPlainText("Label", str);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(newPlainText);
        }
    }

    private static boolean isHasClass(String str) {
        try {
            Class.forName(str);
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    private static String getMD5(String str) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(str.getBytes());
            for (int i : instance.digest()) {
                int i2;
                if (i2 < 0) {
                    i2 += 256;
                }
                if (i2 < 16) {
                    stringBuilder.append("0");
                }
                stringBuilder.append(Integer.toHexString(i2));
            }
            return stringBuilder.toString();
        } catch (Exception unused) {
            return null;
        }
    }

    private static boolean checkPMProxy(PackageManager packageManager) {
        return "android.content.pm.IPackageManager$Stub$Proxy".equals(nowPMName_str(packageManager));
    }

    private static String nowPMName_str(PackageManager packageManager) {
        try {
            Field declaredField = packageManager.getClass().getDeclaredField("mPM");
            declaredField.setAccessible(true);
            return declaredField.get(packageManager).getClass().getName();
        } catch (Exception unused) {
            return "";
        }
    }

    private static String getSingInfoMD5(Context context, String str) {
        Signature[] signatures = getSignatures(context, str);
        Objects.requireNonNull(signatures);
        signatures = signatures;
        String signatureString = signatures.length > 0 ? getSignatureString(signatures[0]) : null;
        return signatureString != null ? getMD5(signatureString) : signatureString;
    }

    private static Signature[] getSignatures(Context context, String str) {
        try {
            return context.getPackageManager().getPackageInfo(str, 64).signatures;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getSignatureString(Signature signature) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA1").digest(signature.toByteArray());
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : digest) {
                stringBuilder.append(Integer.toHexString((b & 255) | 256).substring(1, 3));
            }
            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "error!";
        }
    }
}