package cn.org.obaby.adsskiper.yolo;

import android.content.Context;
import android.util.Log;

import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import cn.org.obaby.adsskiper.LuckyMoneyTinkerApplication;

public class TorchModule {
    private static TorchModule instance = null;
    public Module mModule = null;
    private List<String> mClasses = null;

    private TorchModule() {
        if (mModule == null) {
            try {
                mModule = LiteModuleLoader.load(assetFilePath(LuckyMoneyTinkerApplication.getContext(),"zapping.torchscript.ptl"));//"file:///android_asset/zapping.torchscript.ptl");
                BufferedReader br = new BufferedReader(new InputStreamReader(LuckyMoneyTinkerApplication.getContext().getAssets().open("classes.txt")));
                String line;
                List<String> classes = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    classes.add(line);
                }
                mClasses = classes;
                PrePostProcessor.mClasses = new String[classes.size()];
                classes.toArray(PrePostProcessor.mClasses);
            } catch (IOException e) {
                Log.e("Object Detection", "Error reading assets", e);
            }
        }
    }

    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    public static TorchModule getInstance() {
        if (instance == null) {
            instance = new TorchModule();
        }
        return instance;
    }

    public String printTorchModule(){
        return mModule.toString();
    }

    public List<String> getmClasses() {
        return mClasses;
    }
}
