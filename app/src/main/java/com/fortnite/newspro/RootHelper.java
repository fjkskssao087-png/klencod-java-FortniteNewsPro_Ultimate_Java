package com.fortnite.newspro;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class RootHelper {

    public interface RootCallback {
        void onResult(boolean granted, String details);
    }

    public static void requestRoot(final RootCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process process = null;
                DataOutputStream os = null;
                BufferedReader reader = null;
                boolean isRooted = false;
                StringBuilder info = new StringBuilder();

                try {
                    process = Runtime.getRuntime().exec("su");
                    os = new DataOutputStream(process.getOutputStream());
                    os.writeBytes("id\n");
                    os.writeBytes("exit\n");
                    os.flush();

                    reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        info.append(line);
                    }

                    int code = process.waitFor();
                    if (code == 0 && info.toString().contains("uid=0")) {
                        isRooted = true;
                    }
                } catch (Exception e) {
                    info.append(e.getMessage());
                } finally {
                    try {
                        if (os != null) os.close();
                        if (reader != null) reader.close();
                        if (process != null) process.destroy();
                    } catch (Exception ignored) {}
                }

                if (callback != null) {
                    callback.onResult(isRooted, info.toString());
                }
            }
        }).start();
    }

    public static void executeShellCommand(final String cmd, final RootCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process process = null;
                DataOutputStream os = null;
                BufferedReader reader = null;
                StringBuilder result = new StringBuilder();
                boolean success = false;
                try {
                    process = Runtime.getRuntime().exec("su");
                    os = new DataOutputStream(process.getOutputStream());
                    os.writeBytes(cmd + "\n");
                    os.writeBytes("exit\n");
                    os.flush();

                    reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line).append("\n");
                    }
                    success = (process.waitFor() == 0);
                } catch (Exception e) {
                    result.append(e.getMessage());
                } finally {
                    try {
                        if (os != null) os.close();
                        if (reader != null) reader.close();
                        if (process != null) process.destroy();
                    } catch (Exception ignored) {}
                }
                if (callback != null) {
                    callback.onResult(success, result.toString().trim());
                }
            }
        }).start();
    }
}
