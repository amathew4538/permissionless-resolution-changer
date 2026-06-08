package amathew4538.permissionlessresolutionchanger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.Collections;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ResolutionChanger {
    private static String screenWidth;
    private static String screenHeight;
    private static String screenScale;
    private static String dpi;
    private static String port = "-1";

    /**
     * Get the resolution settings of the mac
     */
    public static void InitializeScreenSettings() {
        try {
            ProcessBuilder resolutionPB = new ProcessBuilder("osascript", "-e", "tell application \"Finder\" to get bounds of window of desktop");
            Process resolutionProcess = resolutionPB.start();

            String resolutionOutput = "";
            try (BufferedReader resolutionReader = new BufferedReader(new InputStreamReader(resolutionProcess.getInputStream()))) {
                resolutionOutput = resolutionReader.lines().collect(Collectors.joining("")).trim();
            } catch (Exception e) {
                e.printStackTrace();
            }

            int exitCode = resolutionProcess.waitFor();
            if (exitCode == 0 && !resolutionOutput.isEmpty()) {
                String[] bounds = resolutionOutput.split(",");
                if (bounds.length >= 4) {
                    screenWidth = bounds[2].trim();
                    screenHeight = bounds[3].trim();
                    
                    System.out.println("Resolution: " + screenWidth + "x" + screenHeight);
                }
            } else {
                System.err.println("AppleScript execution failed with exit code: " + exitCode);
                screenWidth = "1470";
                screenHeight = "956";
                System.out.println("Resolution: " + screenWidth + "x" + screenHeight);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ProcessBuilder screenScalePB = new ProcessBuilder("osascript", "-l", "JavaScript", "-e", "ObjC.import('AppKit'); $.NSScreen.mainScreen.backingScaleFactor");
            Process screenScaleProcess = screenScalePB.start();

            String screenScaleOutput = "";
            try (BufferedReader screenScaleReader = new BufferedReader(new InputStreamReader(screenScaleProcess.getInputStream()))) {
                screenScaleOutput = screenScaleReader.lines().collect(Collectors.joining("")).trim();
            } catch (Exception e) {
                e.printStackTrace();
            }

            int exitCode = screenScaleProcess.waitFor();
            if (exitCode == 0 && !screenScaleOutput.isEmpty()) {
                screenScale = screenScaleOutput.trim();
                System.out.println("Screen Scale: " + screenScale);
            } else {
                System.err.println("AppleScript execution failed with exit code: " + exitCode);
                screenScale = "2";
                System.out.println("Screen Scale: " + screenScale);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if ("2".equals(screenScale)) {
            dpi = "8192";
        } else {
            dpi = "16384";
        }
    }

    public static void getBWPort() {
        Path instancesPath = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "PrismLauncher", "instances");
        try {
            List<Path> boundlessPortFiles = findPortFiles(instancesPath);

            if (boundlessPortFiles.isEmpty()) {
                System.out.println("No boundless_port.txt files found");
                port = "-1";
            } else {
                System.out.println("\nFound the following port files:");
                for (Path file : boundlessPortFiles) {
                    System.out.println("- " + file.toAbsolutePath());

                    try {
                        port = new String(Files.readAllBytes(file), StandardCharsets.UTF_8).trim();
                        System.out.println("Active Port: " + port);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid port number format in file.");
                        port = "-1";
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading directories or files: " + e.getMessage());
            port = "-1";
        }
    }

    /**
     * Finds port files
     */
    public static List<Path> findPortFiles(Path instancesPath) throws IOException {
        if (!Files.exists(instancesPath)) {
            return Collections.emptyList();
        }

        List<Path> foundFiles = new ArrayList<>();

        try (Stream<Path> instanceDirs = Files.list(instancesPath)) {
            instanceDirs.filter(Files::isDirectory).forEach(instanceDir -> {
                Path originalBashPath = instanceDir
                    .resolve("natives")
                    .resolve("..")
                    .resolve("minecraft")
                    .resolve("boundless_port.txt");

                Path normalizedPath = originalBashPath.normalize();

                if (Files.exists(instanceDir.resolve("natives")) && Files.exists(normalizedPath)) {
                    foundFiles.add(normalizedPath);
                }
            });
        }

        System.out.println("Port files found");
        return foundFiles;
    }

    // Resolution Setters
    private static void sendResolutionCommandAsync(String targetResolution) {
        getBWPort();
        
        if (port == null || port.equals("-1")) {
            System.out.println("No port!");
            return;
        }

        new Thread(() -> {
            try {
                String bashCommand = String.format("echo %s | nc localhost %s", targetResolution, port);
                ProcessBuilder pb = new ProcessBuilder("bash", "-c", bashCommand);

                pb.redirectErrorStream(true);
                pb.inheritIO();

                Process process = pb.start();

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    System.err.println("Error changing resolution. Bash exited with code: " + exitCode);
                }
            } catch (Exception e) {
                System.err.println("Connection failed: " + e.getMessage());
            }
        }).start();
    }

    public static void setResolutionToBase() {
        String targetResolution = String.format("set 0 0 %s %s", screenWidth, screenHeight);
        sendResolutionCommandAsync(targetResolution);
    }

    public static void setResolutionToTall() {
        String targetResolution = String.format("set - - 384 %s", dpi);
        sendResolutionCommandAsync(targetResolution);
    }

    public static void setResolutionToThin() {
        String targetResolution = String.format("set - - 384 %s", screenHeight);
        sendResolutionCommandAsync(targetResolution);
    }

    public static void setResolutionToWide() {
        String targetResolution = String.format("set - - %s 300", screenWidth);
        sendResolutionCommandAsync(targetResolution);
    }

    // Read-only getters
    public static String getScreenWidth() { return screenWidth; }
    public static String getScreenHeight() { return screenHeight; }
    public static String getscreenScale() { return screenScale; }
    public static String getDPI() { return dpi; }
    public static String getPort() { return port; }
}