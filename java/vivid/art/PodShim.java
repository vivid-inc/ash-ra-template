// Copyright 2019 Vivid Inc.

package vivid.art;

import org.projectodd.shimdandy.ClojureRuntimeShim;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.WeakHashMap;

public class PodShim {

    private static final WeakHashMap<ClojureRuntimeShim, Object> pods = new WeakHashMap<>();

    private static File
    bootdir() throws Exception {
        File h = new File(System.getProperty("user.home"));
        String a = System.getProperty("BOOT_HOME");
        String b = System.getenv("BOOT_HOME");
        String c = new File(h, ".boot").getCanonicalPath();
        return new File((a != null) ? a : ((b != null) ? b : c));
    }

    public static ClojureRuntimeShim
    newShim(String name, Object data, File[] jarFiles) throws Exception {
        URL[] urls = new URL[jarFiles.length];

        for (int i = 0; i < jarFiles.length; i++) urls[i] = jarFiles[i].toURI().toURL();

        ClassLoader cl = new AddableClassLoader(urls, PodShim.class.getClassLoader());
        ClojureRuntimeShim rt = ClojureRuntimeShim.newRuntime(cl);

        rt.setName(name != null ? name : "anonymous");

        File[] hooks = {new File(bootdir(), "boot-shim.clj"), new File("boot-shim.clj")};

        for (File hook : hooks)
            if (hook.exists())
                rt.invoke("clojure.core/load-file", hook.getPath());

        rt.require("vivid.art.pod");
        rt.invoke("vivid.art.pod/seal-app-classloader");
        rt.invoke("vivid.art.pod/extend-addable-classloader");
        rt.invoke("vivid.art.pod/set-data!", data);
        rt.invoke("vivid.art.pod/set-pods!", pods);
        rt.invoke("vivid.art.pod/set-this-pod!", new WeakReference<>(rt));

        pods.put(rt, new Object());
        return rt;
    }

}
