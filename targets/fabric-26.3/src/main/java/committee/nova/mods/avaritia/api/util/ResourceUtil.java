package committee.nova.mods.avaritia.api.util;

import committee.nova.mods.avaritia.Const;
import net.minecraft.resources.Identifier;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

public class ResourceUtil {
    private static final IdentifierFactory FACTORY;

    static {
        IdentifierFactory factory1;
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle fromNamespaceAndPath;
        MethodHandle parse;
        MethodHandle tryWithResources;
        MethodHandle valueOf;
        MethodHandle of;
        Constructor<Identifier> constructor;
        try {
            fromNamespaceAndPath = lookup.findStatic(Identifier.class, "fromNamespaceAndPath",
                    MethodType.methodType(Identifier.class, String.class, String.class));
            parse = lookup.findStatic(Identifier.class, "parse",
                    MethodType.methodType(Identifier.class, String.class));
            factory1 = new HighVersionFactory(fromNamespaceAndPath, parse);
        } catch (Exception e) {
            try {
                tryWithResources = lookup.findStatic(Identifier.class, "tryWithResources",
                        MethodType.methodType(Identifier.class, String.class, String.class));
                factory1 = new LowVersionFactory(tryWithResources, LowVersionFactory.MethodType.TRY_WITH_RESOURCES);
            } catch (Exception e1) {
                try {
                    valueOf = lookup.findStatic(Identifier.class, "valueOf",
                            MethodType.methodType(Identifier.class, String.class, String.class));
                    factory1 = new LowVersionFactory(valueOf, LowVersionFactory.MethodType.VALUE_OF);
                } catch (Exception e2) {
                    try {
                        of = tryFindStaticMethod(lookup, Identifier.class, MethodType.methodType(Identifier.class,
                                String.class, String.class), Arrays.asList("of", "m_135822_"));
                        factory1 = new LowVersionFactory(of, LowVersionFactory.MethodType.OF);
                    } catch (Exception e3) {
                        try {
                            constructor = Identifier.class.getDeclaredConstructor(String.class, String.class);
                            constructor.setAccessible(true);
                            factory1 = new LowVersionFactory(constructor, LowVersionFactory.MethodType.CONSTRUCTOR);
                        } catch (Exception e4) {
                            throw new RuntimeException("无法找到可用的 Identifier 创建方法", e4);
                        }
                    }
                }
            }
        }
        FACTORY = factory1;
    }

    private interface IdentifierFactory {
        Identifier create(String namespace, String path);

        Identifier parse(String path);
    }

    private record HighVersionFactory(MethodHandle fromNamespaceAndPath,
                                      MethodHandle parse) implements IdentifierFactory {

        @Override
        public Identifier create(String namespace, String path) {
            try {
                return (Identifier) fromNamespaceAndPath.invokeExact(namespace, path);
            } catch (Throwable e) {
                throw new RuntimeException("高版本创建 Identifier 失败", e);
            }
        }

        @Override
        public Identifier parse(String path) {
            try {
                return (Identifier) parse.invokeExact(path);
            } catch (Throwable e) {
                throw new RuntimeException("高版本解析 Identifier 失败", e);
            }
        }
    }

    private static class LowVersionFactory implements IdentifierFactory {
        private final Object method;
        private final MethodType methodType;

        enum MethodType {
            TRY_WITH_RESOURCES, VALUE_OF, OF, CONSTRUCTOR
        }

        public LowVersionFactory(MethodHandle methodHandle, MethodType methodType) {
            this.method = methodHandle;
            this.methodType = methodType;
        }

        public LowVersionFactory(Constructor<Identifier> constructor, MethodType methodType) {
            this.method = constructor;
            this.methodType = methodType;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Identifier create(String namespace, String path) {
            try {
                return switch (methodType) {
                    case TRY_WITH_RESOURCES, VALUE_OF, OF ->
                            (Identifier) ((MethodHandle) method).invokeExact(namespace, path);
                    case CONSTRUCTOR -> ((Constructor<Identifier>) method).newInstance(namespace, path);
                };
            } catch (Throwable e) {
                throw new RuntimeException("低版本创建 Identifier 失败", e);
            }
        }

        @Override
        public Identifier parse(String path) {
            int colonIndex = path.indexOf(':');
            if (colonIndex == -1) {
                return create(Const.MOD_ID, path);
            } else {
                String namespace = path.substring(0, colonIndex);
                String localPath = path.substring(colonIndex + 1);
                return create(namespace, localPath);
            }
        }
    }

    private static MethodHandle tryFindStaticMethod(MethodHandles.Lookup lookup,
                                                    Class<Identifier> clazz,
                                                    MethodType methodType,
                                                    List<String> methodNames) throws NoSuchMethodException {
        for (String methodName : methodNames) {
            try {
                return lookup.findStatic(clazz, methodName, methodType);
            } catch (NoSuchMethodException | IllegalAccessException ignored) {
            }
        }
        throw new NoSuchMethodException("未找到任何匹配的方法: " + methodNames);
    }

    /**
     * 创建Identifier的通用方法
     */
    private static Identifier createIdentifier(String namespace, String path) {
        return FACTORY.create(namespace, path);
    }

    /**
     * 解析Identifier的通用方法
     */
    private static Identifier parseIdentifier(String path) {
        return FACTORY.parse(path);
    }

    public static Identifier createInstance(String path) {
        return createIdentifier(Const.MOD_ID, path);
    }

    public static String createStringInstance(String path) {
        return Const.MOD_ID + ":" + path;
    }

    public static Identifier createInstance(String name, String path) {
        return createIdentifier(name, path);
    }

    public static Identifier createInstanceNoNamespace(String path) {
        return parseIdentifier(path);
    }

    public static Identifier createMinecraftInstance(String path) {
        return createIdentifier("minecraft", path);
    }

    public static Identifier createTextureInstance(String path) {
        return createIdentifier(Const.MOD_ID, "textures/" + path);
    }

    public static Identifier createInstanceWithColon(String all) {
        String namespace;
        String path;
        if (all.contains(":")) {
            int colonIndex = all.indexOf(":");
            namespace = all.substring(0, colonIndex);
            path = all.substring(colonIndex + 1);
        } else {
            namespace = Const.MOD_ID;
            path = all;
        }
        return createInstance(namespace, path);
    }

    /**
     * 自动解析 namespace:path
     * 若无冒号，自动补 modid
     */
    public static Identifier create(String id) {
        if (id.contains(":")) {
            return createInstanceWithColon(id);
        }
        return createInstance(Const.MOD_ID, id);
    }
}