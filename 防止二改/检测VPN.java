  Android vpn 网络检测 
   
   public static boolean a() {
        try {
            Enumeration networkInterfaces = NetworkInterface.getNetworkInterfaces();
            if (networkInterfaces != null) {
                for (NetworkInterface networkInterface : Collections.list(networkInterfaces)) {
                    if (networkInterface.isUp() && networkInterface.getInterfaceAddresses().size() != 0) {
                        if ("tun0".equals(networkInterface.getName()) || "ppp0".equals(networkInterface.getName())) {
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return false;
    }

判断网络接口名字包含 ppp0 或 tun0
    public void isDeviceInVPN() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (name.equals("tun0") || name.equals("ppp0")) {
                    Log.i("TAG", "isDeviceInVPN  current device is in VPN.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
获取当前网络 Transpoart 字样

    public void networkCheck() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network = connectivityManager.getActiveNetwork();

            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            Log.i("TAG", "networkCapabilities -> " + networkCapabilities.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
