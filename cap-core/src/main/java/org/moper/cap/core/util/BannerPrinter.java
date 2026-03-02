package org.moper.cap.core.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * Banner 打印工具类，用于在应用启动时输出自定义的 ASCII 艺术或欢迎信息。
 *
 * <p>支持从多个源读取 Banner 内容：
 * <ul>
 *   <li>应用 classpath 下的资源文件（如 {@code banner.txt}）</li>
 *   <li>文件系统中的任意文件</li>
 *   <li>直接提供的字符串内容</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * // 从 classpath 资源读取
 * BannerPrinter.printBannerFromClasspath("banner.txt");
 *
 * // 从文件系统读取
 * BannerPrinter.printBannerFromFile("/path/to/banner.txt");
 *
 * // 直接打印字符串
 * BannerPrinter.printBanner("Welcome to Cap Framework!");
 * </pre>
 */
public final class BannerPrinter {

    private static final String DEFAULT_BANNER_CHARSET = "UTF-8";

    /**
     * 从 classpath 资源加载并打印 Banner。
     *
     * @param resourcePath 相对于 classpath 的资源路径（如 {@code banner.txt}）
     * @throws BannerLoadException 当资源不存在或读取失败时抛出
     */
    public static void printBannerFromClasspath(String resourcePath) {
        try {
            String content = loadBannerFromClasspath(resourcePath);
            System.out.println(content);
        } catch (IOException e) {
            throw new BannerLoadException("Failed to load banner from classpath: " + resourcePath, e);
        }
    }

    /**
     * 从文件系统加载并打印 Banner。
     *
     * @param filePath 文件系统中的绝对或相对路径
     * @throws BannerLoadException 当文件不存在或读取失败时抛出
     */
    public static void printBannerFromFile(String filePath) {
        try {
            String content = loadBannerFromFile(filePath);
            System.out.println(content);
        } catch (IOException e) {
            throw new BannerLoadException("Failed to load banner from file: " + filePath, e);
        }
    }

    /**
     * 直接打印 Banner 内容。
     *
     * @param content Banner 文本内容
     */
    public static void printBanner(String content) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        System.out.println(content);
    }

    /**
     * 从 classpath 资源加载 Banner 内容（不打印，仅返回）。
     *
     * @param resourcePath 相对于 classpath 的资源路径
     * @return Banner 文本内容
     * @throws IOException 当资源不存在或读取失败时抛出
     */
    public static String loadBannerFromClasspath(String resourcePath) throws IOException {
        try (InputStream inputStream = BannerPrinter.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Banner resource not found: " + resourcePath);
            }
            return readFromInputStream(inputStream);
        }
    }

    /**
     * 从文件系统加载 Banner 内容（不打印，仅返回）。
     *
     * @param filePath 文件系统中的路径
     * @return Banner 文本内容
     * @throws IOException 当文件不存在或读取失败时抛出
     */
    public static String loadBannerFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Banner file not found: " + filePath);
        }
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    /**
     * 从输入流读取 Banner 内容。
     *
     * @param inputStream 输入流
     * @return Banner 文本内容
     * @throws IOException 当读取失败时抛出
     */
    private static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    /**
     * Banner 加载异常。
     */
    public static class BannerLoadException extends RuntimeException {
        public BannerLoadException(String message) {
            super(message);
        }

        public BannerLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}