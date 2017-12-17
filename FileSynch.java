import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileSynch {

    public static void main(String[] args) throws IOException {

        checkInputParameters(args);
        final Path source = Paths.get(args[0]);
        final Path dest = Paths.get(args[1]);

        if (notExists(dest)) {
            Files.createDirectory(dest);
        }
        walkFileTree(source, new CopyFileVisitor(source, dest));
        walkFileTree(dest, new RemoveFileVisitor(source, dest));
    }

    private static void checkInputParameters(String[] parameters) {
        if (Objects.isNull(parameters) || parameters.length < 2) {
            throw new IllegalArgumentException();
        }
    }

   private static class CopyFileVisitor extends SimpleFileVisitor<Path> {

        private Path source;
        private Path dest;

        public CopyFileVisitor(Path source, Path dest) {
            this.source = source;
            this.dest = dest;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path currentPath, BasicFileAttributes attrs) throws IOException {
            if (currentPath.equals(source)) {
                return CONTINUE;
            }
            Path destPath =  dest.resolve(source.relativize(currentPath));
            if (notExists(destPath)) {
                Files.copy(currentPath, destPath);
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path currentPath, BasicFileAttributes attrs) throws IOException {
           Path destPath =  dest.resolve(source.relativize(currentPath));
            if (notExists(destPath) ||
                    (Files.exists(destPath) && (Files.size(currentPath) != Files.size(destPath)))) {
                Files.copy(currentPath, destPath, REPLACE_EXISTING);
            }
            return CONTINUE;
        }
    }

    private static class RemoveFileVisitor extends SimpleFileVisitor<Path> {

        private Path source;
        private Path dest;

        public RemoveFileVisitor(Path source, Path dest) {
            this.source = source;
            this.dest = dest;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path currentPath, BasicFileAttributes attrs) throws IOException {
            Path sourcePath = source.resolve(dest.relativize(currentPath));
            if (notExists(sourcePath) && !newDirectoryStream(currentPath).iterator().hasNext()) {
                delete(currentPath);
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path currentPath, BasicFileAttributes attrs) throws IOException {
          Path sourcePath = source.resolve(dest.relativize(currentPath));
          if (notExists(sourcePath)) {
              delete(currentPath);
          }
            return CONTINUE;
        }
    }
}
