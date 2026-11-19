package com.zifang.z.meta.core.collector;

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.core.io.Resource;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * 方法级注解过滤器：判断一个类里有没有方法挂着指定注解。
 *
 * <p>Spring 自带的 {@code AnnotationTypeFilter} 只看类级注解，而 {@code @BizMethod} /
 * {@code @BizParam} / {@code @BizApi} 是方法级的 —— 靠它过滤会一个都扫不到。
 * 反射扫全包又太贵（每个类都要 load）。这里用 ASM 直接读字节码判断，
 * 不命中的类连 ClassLoader 都不用走。</p>
 *
 * @author zifang
 * @since 1.1.0
 */
public class MethodAnnotationTypeFilter implements TypeFilter {

    private final String annotationDescriptor;

    /**
     * @param annotationClass 方法级注解类
     */
    public MethodAnnotationTypeFilter(Class<?> annotationClass) {
        this.annotationDescriptor = "L" + annotationClass.getName().replace('.', '/') + ";";
    }

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
        Resource resource = metadataReader.getResource();
        try (InputStream in = resource.getInputStream()) {
            ClassReader reader = new ClassReader(in);
            final boolean[] found = new boolean[1];
            reader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc,
                                                String signature, String[] exceptions) {
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                            if (annotationDescriptor.equals(descriptor)) found[0] = true;
                            return null;
                        }
                    };
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return found[0];
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 工厂方法：只保留读元数据，不做别的事。
     *
     * @param annotationClass 方法级注解类
     * @return 过滤器
     */
    public static TypeFilter of(Class<?> annotationClass) {
        return new MethodAnnotationTypeFilter(annotationClass);
    }
}
