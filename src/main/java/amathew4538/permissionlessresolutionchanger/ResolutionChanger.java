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
    private static String port;

    /**
     * Get the resolution settings of the mac
     */
    public static void InitializeScreenSettings() {
        String resolutionOsascript = "tell application \"Finder\" to get bounds of window of desktop";
        String screenScaleOsascript = "tell application \"System Events\" to return backing scale factor of window 1";

        try {
            String resolutionBashCommand = "osascript -e '" + resolutionOsascript + "'";
            ProcessBuilder resolutionPB = new ProcessBuilder("bash", "-c", resolutionBashCommand);
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
            String screenScaleBashCommand = "osascript -e '" + screenScaleOsascript + "'";
            ProcessBuilder screenScalePB = new ProcessBuilder("bash", "-c", screenScaleBashCommand);
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
                screenScale = "2.0";
                System.out.println("Screen Scale: " + screenScale);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            getBWPort();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getBWPort() {
        new Thread(() -> {
            Path instancesPath = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "PrismLauncher", "instances");
            try {
                List<Path> boundlessPortFiles = findPortFiles(instancesPath);

                if (boundlessPortFiles.isEmpty()) {
                    System.out.println("No boundless_port.txt files found");
                    port = "-1";
                } else {
                    System.out.println("\nFound the following port files via natives path:");
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
            }
            port = "-1";
        }).start();
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

    public static String getDpiFromScreenScale(String screenScale) {
        if ("2.0".equals(screenScale)) {
            return "8192";
        } else {
            return "16384";
        }
    }

    // Resolution Setters
    public static void setResolutionToBase() {
        if (port == "-1") {
            System.out.println("No port!");
            return;
        }
        new Thread(() -> {
            try {
                String targetResolution = String.format("set 0 0 %s %s", screenWidth, screenHeight);

                String bashCommand = String.format("echo %s | nc localhost %s >/dev/null", targetResolution, port);

                ProcessBuilder pb = new ProcessBuilder("bash", "-c", bashCommand);
                Process process = pb.start();

                if (process.waitFor() != 0) {
                    System.err.println("Error changing resolution");
                }
            } catch (Exception e) {
                System.err.println("Connection failed: " + e.getMessage());
            }
        }).start();
    }

    public static void setResolutionToTall() {
        if (port == "-1") {
            System.out.println("No port!");
            return;
        }
        new Thread(() -> {
            try {
                String targetResolution = String.format("set - - 384 %s", getDpiFromScreenScale(screenScale));

                String bashCommand = String.format("echo %s | nc localhost %s >/dev/null", targetResolution, port);

                ProcessBuilder pb = new ProcessBuilder("bash", "-c", bashCommand);
                Process process = pb.start();

                if (process.waitFor() != 0) {
                    System.err.println("Error changing resolution");
                }
            } catch (Exception e) {
                System.err.println("Connection failed: " + e.getMessage());
            }
        }).start();
    }

    public static void setResolutionToThin() {
        if (port == "-1") {
            System.out.println("No port!");
            return;
        }

        try {
            String targetResolution = String.format("set - - 384 %s", screenHeight);

            String bashCommand = String.format("echo %s | nc localhost %s >/dev/null", targetResolution, port);

            ProcessBuilder pb = new ProcessBuilder("bash", "-c", bashCommand);
            Process process = pb.start();

            if (process.waitFor() != 0) {
                System.err.println("Error changing resolution");
            }
        } catch (Exception e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

    public static void setResolutionToWide() {
        if (port == "-1") {
            System.out.println("No port!");
            return;
        }

        try {
            String targetResolution = String.format("set - - %s 300", screenWidth);

            String bashCommand = String.format("echo %s | nc localhost %s >/dev/null", targetResolution, port);

            ProcessBuilder pb = new ProcessBuilder("bash", "-c", bashCommand);
            Process process = pb.start();

            if (process.waitFor() != 0) {
                System.err.println("Error changing resolution");
            }
        } catch (Exception e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

    // Read-only getters
    public static String getScreenWidth() { return screenWidth; }
    public static String getScreenHeight() { return screenHeight; }
    public static String getscreenScale() { return screenScale; }
    public static String getDPI() { return getDpiFromScreenScale(screenScale); }
}