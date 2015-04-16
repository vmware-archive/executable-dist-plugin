package io.pivotal.labs.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ManifestUtils {

    public static Manifest create(Map<String, String> attributes) {
        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.forEach(mainAttributes::putValue);
        return manifest;
    }

    public static byte[] toByteArray(Manifest manifest) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        manifest.write(buf);
        return buf.toByteArray();
    }

}
