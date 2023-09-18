package de.plocki.datacomparer;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String[] NYAN_CAT_FRAMES = {
            "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧ ",
            "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧",
            "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧",
            "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧",
            "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧",
            "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧ ",
            "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧"
    };

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the source folder path: ");
        String sourceFolderPath = scanner.nextLine();
        Path sourceFolder = Paths.get(sourceFolderPath);

        System.out.print("Enter the target folder path: ");
        String targetFolderPath = scanner.nextLine();
        Path targetFolder = Paths.get(targetFolderPath);

        try {
            List<Path> missingFilesSourceToTarget = compareFolders(sourceFolder, targetFolder);
            displayMissingFiles("Missing files in source to target:", missingFilesSourceToTarget);

            System.out.print("Do you want to copy missing files from source to target? (yes, else press enter): ");
            String confirmation = scanner.nextLine();

            if (confirmation.equalsIgnoreCase("yes")) {
                copyMissingFilesWithNyanCat(missingFilesSourceToTarget, sourceFolder, targetFolder);
            }

            List<Path> missingFilesTargetToSource = compareFolders(targetFolder, sourceFolder);
            displayMissingFiles("Missing files in target to source:", missingFilesTargetToSource);

            System.out.print("Do you want to copy missing files from target to source? (yes/no): ");
            confirmation = scanner.nextLine();

            if (confirmation.equalsIgnoreCase("yes")) {
                copyMissingFilesWithNyanCat(compareFolders(targetFolder, sourceFolder), targetFolder, sourceFolder);
            }

            System.out.print("Do you want to copy empty folders from source to target? (yes/no): ");
            confirmation = scanner.nextLine();

            if (confirmation.equalsIgnoreCase("yes")) {
                copyEmptyFolders(sourceFolder, targetFolder);
            }

            System.out.print("Do you want to copy empty folders from target to source? (yes/no): ");
            confirmation = scanner.nextLine();

            if (confirmation.equalsIgnoreCase("yes")) {
                copyEmptyFolders(targetFolder, sourceFolder);
            }

            System.out.println("\nCopying completed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Path> compareFolders(Path sourceFolder, Path targetFolder) throws IOException {
        List<Path> missingFiles = new ArrayList<>();
        List<Path> emptyFolders = new ArrayList<>();

        Files.walkFileTree(sourceFolder, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path sourceFile, BasicFileAttributes attrs) throws IOException {
                Path targetFile = targetFolder.resolve(sourceFolder.relativize(sourceFile));

                if (!Files.exists(targetFile)) {
                    missingFiles.add(sourceFile);
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                // Check for empty folders
                if (Files.isDirectory(dir) && isFolderEmpty(dir)) {
                    emptyFolders.add(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        Files.walkFileTree(targetFolder, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path targetFile, BasicFileAttributes attrs) throws IOException {
                Path sourceFile = sourceFolder.resolve(targetFolder.relativize(targetFile));

                if (!Files.exists(sourceFile)) {
                    missingFiles.add(targetFile);
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                // Check for empty folders
                if (Files.isDirectory(dir) && isFolderEmpty(dir)) {
                    emptyFolders.add(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        System.out.println("Empty folders in source and target:");
        for (Path emptyFolder : emptyFolders) {
            System.out.println(emptyFolder.toString());
        }

        return missingFiles;
    }

    private static void copyEmptyFolders(Path sourceFolder, Path targetFolder) {
        try {
            Files.walkFileTree(sourceFolder, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Path targetDir = targetFolder.resolve(sourceFolder.relativize(dir));
                    if (isFolderEmpty(dir)) {
                        Files.createDirectories(targetDir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isFolderEmpty(Path folder) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            return !stream.iterator().hasNext();
        }
    }

    private static void displayMissingFiles(String message, List<Path> missingFiles) {
        System.out.println(message);
        for (Path missingFile : missingFiles) {
            System.out.println(missingFile.toString());
        }
    }

    private static void copyMissingFilesWithNyanCat(List<Path> missingFiles, Path sourceFolder, Path targetFolder) {
        int totalFiles = missingFiles.size();
        int copiedFiles = 0;

        for (Path missingFile : missingFiles) {
            Path targetFile = targetFolder.resolve(sourceFolder.relativize(missingFile));

            try {
                // Ensure the parent directory of the target file exists, create it if necessary
                Path parentDir = targetFile.getParent();
                if (parentDir != null && !Files.exists(parentDir)) {
                    Files.createDirectories(parentDir);
                }

                System.out.print("\rCopying file: " + missingFile.getFileName() + "   " + NYAN_CAT_FRAMES[copiedFiles % NYAN_CAT_FRAMES.length]);
                Files.copy(missingFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                copiedFiles++;
                Thread.sleep(100);
            } catch (IOException | InterruptedException e) {
                System.err.println("Error copying file: " + missingFile);
                e.printStackTrace();
            }
        }

        System.out.println("\nCopied " + copiedFiles + " files out of " + totalFiles + " missing files.");
    }

}
