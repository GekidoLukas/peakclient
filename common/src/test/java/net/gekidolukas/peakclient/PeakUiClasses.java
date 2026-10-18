package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakElement;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class PeakUiClasses {
    private static final String PACKAGE_NAME = "net.gekidolukas.peakclient.ui";
    private static final String PACKAGE_PATH = PACKAGE_NAME.replace('.', '/') + "/";
    private static final String CLASS_SUFFIX = ".class";

    private PeakUiClasses() {}

    public static List<Class<?>> all() throws Exception {
        List<Class<?>> result = new ArrayList<>();
        for (String name : classNames()) {
            result.add(Class.forName(PACKAGE_NAME + "." + name, false, PeakElement.class.getClassLoader()));
        }
        return result;
    }

    private static List<String> classNames() throws Exception {
        Path location = Paths.get(PeakElement.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        List<String> names = new ArrayList<>();
        if (Files.isDirectory(location)) {
            try (Stream<Path> files = Files.list(location.resolve(PACKAGE_PATH))) {
                files.map(path -> path.getFileName().toString())
                        .filter(name -> name.endsWith(CLASS_SUFFIX) && !name.contains("$"))
                        .forEach(name -> names.add(name.substring(0, name.length() - CLASS_SUFFIX.length())));
            }
        } else {
            try (JarFile jar = new JarFile(location.toFile())) {
                jar.stream().map(entry -> entry.getName())
                        .filter(name -> name.startsWith(PACKAGE_PATH) && name.endsWith(CLASS_SUFFIX)
                                && !name.contains("$") && name.indexOf('/', PACKAGE_PATH.length()) < 0)
                        .forEach(name -> names.add(name.substring(PACKAGE_PATH.length(), name.length() - CLASS_SUFFIX.length())));
            }
        }
        return names;
    }
}
