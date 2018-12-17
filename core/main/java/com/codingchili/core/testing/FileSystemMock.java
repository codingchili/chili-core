package com.codingchili.core.testing;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.*;

import java.util.List;

/**
 * Mock filesystem: turns asynchronous reads into sync reads to avoid dealing
 * with asynchronous setup in test cases.
 */
public class FileSystemMock implements FileSystem {
    private FileSystem fs;

    public FileSystemMock(Vertx vertx) {
        this.fs = vertx.fileSystem();
    }

    @Override
    public FileSystem copy(String from, String to, Handler<AsyncResult<Void>> handler) {
        fs.copy(from, to, handler);
        return this;
    }

    @Override
    public FileSystem copy(String from, String to, CopyOptions options, Handler<AsyncResult<Void>> handler) {
        fs.copy(from, to, options, handler);
        return this;
    }

    @Override
    public FileSystem copyBlocking(String from, String to) {
        fs.copyBlocking(from, to);
        return this;
    }

    @Override
    public FileSystem copyRecursive(String from, String to, boolean recursive, Handler<AsyncResult<Void>> handler) {
        fs.copyRecursive(from, to, recursive, handler);
        return this;
    }

    @Override
    public FileSystem copyRecursiveBlocking(String from, String to, boolean recursive) {
        fs.copyRecursiveBlocking(from, to, recursive);
        return this;
    }

    @Override
    public FileSystem move(String from, String to, Handler<AsyncResult<Void>> handler) {
        fs.move(from, to, handler);
        return this;
    }

    @Override
    public FileSystem move(String from, String to, CopyOptions options, Handler<AsyncResult<Void>> handler) {
        fs.move(from, to, options, handler);
        return this;
    }

    @Override
    public FileSystem moveBlocking(String from, String to) {
        fs.moveBlocking(from, to);
        return this;
    }

    @Override
    public FileSystem truncate(String path, long len, Handler<AsyncResult<Void>> handler) {
        fs.truncate(path, len, handler);
        return this;
    }

    @Override
    public FileSystem truncateBlocking(String path, long len) {
        fs.truncateBlocking(path, len);
        return this;
    }

    @Override
    public FileSystem chmod(String path, String perms, Handler<AsyncResult<Void>> handler) {
        fs.chmod(path, perms, handler);
        return this;
    }

    @Override
    public FileSystem chmodBlocking(String path, String perms) {
        fs.chmodBlocking(path, perms);
        return this;
    }

    @Override
    public FileSystem chmodRecursive(String path, String perms, String dirPerms, Handler<AsyncResult<Void>> handler) {
        fs.chmodRecursive(path, perms, dirPerms, handler);
        return this;
    }

    @Override
    public FileSystem chmodRecursiveBlocking(String path, String perms, String dirPerms) {
        fs.chmodRecursiveBlocking(path, perms, dirPerms);
        return this;
    }

    @Override
    public FileSystem chown(String path, String user, String group, Handler<AsyncResult<Void>> handler) {
        return this;
    }

    @Override
    public FileSystem chownBlocking(String path, String user, String group) {
        return this;
    }

    @Override
    public FileSystem props(String path, Handler<AsyncResult<FileProps>> handler) {
        return this;
    }

    @Override
    public FileProps propsBlocking(String path) {
        return null;
    }

    @Override
    public FileSystem lprops(String path, Handler<AsyncResult<FileProps>> handler) {
        return this;
    }

    @Override
    public FileProps lpropsBlocking(String path) {
        return null;
    }

    @Override
    public FileSystem link(String link, String existing, Handler<AsyncResult<Void>> handler) {
        return this;
    }

    @Override
    public FileSystem linkBlocking(String link, String existing) {
        return this;
    }

    @Override
    public FileSystem symlink(String link, String existing, Handler<AsyncResult<Void>> handler) {
        return this;
    }

    @Override
    public FileSystem symlinkBlocking(String link, String existing) {
        return this;
    }

    @Override
    public FileSystem unlink(String link, Handler<AsyncResult<Void>> handler) {
        return this;
    }

    @Override
    public FileSystem unlinkBlocking(String link) {
        return this;
    }

    @Override
    public FileSystem readSymlink(String link, Handler<AsyncResult<String>> handler) {
        return this;
    }

    @Override
    public String readSymlinkBlocking(String link) {
        return null;
    }

    @Override
    public FileSystem delete(String path, Handler<AsyncResult<Void>> handler) {
        return this;
    }

    @Override
    public FileSystem deleteBlocking(String path) {
        return this;
    }

    @Override
    public FileSystem deleteRecursive(String path, boolean recursive, Handler<AsyncResult<Void>> handler) {
        return this;
    }

    @Override
    public FileSystem deleteRecursiveBlocking(String path, boolean recursive) {
        return this;
    }

    @Override
    public FileSystem mkdir(String path, Handler<AsyncResult<Void>> handler) {
        return this;
    }

    @Override
    public FileSystem mkdirBlocking(String path) {
        return this;
    }

    @Override
    public FileSystem mkdir(String path, String perms, Handler<AsyncResult<Void>> handler) {
        return this;
    }

    @Override
    public FileSystem mkdirBlocking(String path, String perms) {
        return this;
    }

    @Override
    public FileSystem mkdirs(String path, Handler<AsyncResult<Void>> handler) {
        return this;
    }

    @Override
    public FileSystem mkdirsBlocking(String path) {
        return this;
    }

    @Override
    public FileSystem mkdirs(String path, String perms, Handler<AsyncResult<Void>> handler) {
        return this;
    }

    @Override
    public FileSystem mkdirsBlocking(String path, String perms) {
        return this;
    }

    @Override
    public FileSystem readDir(String path, Handler<AsyncResult<List<String>>> handler) {
        return this;
    }

    @Override
    public List<String> readDirBlocking(String path) {
        return null;
    }

    @Override
    public FileSystem readDir(String path, String filter, Handler<AsyncResult<List<String>>> handler) {
        return this;
    }

    @Override
    public List<String> readDirBlocking(String path, String filter) {
        return null;
    }

    @Override
    public FileSystem readFile(String path, Handler<AsyncResult<Buffer>> handler) {
        handler.handle(Future.succeededFuture(fs.readFileBlocking(path)));
        return fs;
    }

    @Override
    public Buffer readFileBlocking(String path) {
        return fs.readFileBlocking(path);
    }

    @Override
    public FileSystem writeFile(String path, Buffer data, Handler<AsyncResult<Void>> handler) {
        return fs.writeFile(path, data, handler);
    }

    @Override
    public FileSystem writeFileBlocking(String path, Buffer data) {
        return fs.writeFileBlocking(path, data);
    }

    @Override
    public FileSystem open(String path, OpenOptions options, Handler<AsyncResult<AsyncFile>> handler) {
        return fs.open(path, options, handler);
    }

    @Override
    public AsyncFile openBlocking(String path, OpenOptions options) {
        return fs.openBlocking(path, options);
    }

    @Override
    public FileSystem createFile(String path, Handler<AsyncResult<Void>> handler) {
        return fs.createFile(path, handler);
    }

    @Override
    public FileSystem createFileBlocking(String path) {
        return fs.createFileBlocking(path);
    }

    @Override
    public FileSystem createFile(String path, String perms, Handler<AsyncResult<Void>> handler) {
        return fs.createFile(path, perms, handler);
    }

    @Override
    public FileSystem createFileBlocking(String path, String perms) {
        return fs.createFileBlocking(path, perms);
    }

    @Override
    public FileSystem exists(String path, Handler<AsyncResult<Boolean>> handler) {
        return fs.exists(path, handler);
    }

    @Override
    public boolean existsBlocking(String path) {
        return fs.existsBlocking(path);
    }

    @Override
    public FileSystem fsProps(String path, Handler<AsyncResult<FileSystemProps>> handler) {
        return fs.fsProps(path, handler);
    }

    @Override
    public FileSystemProps fsPropsBlocking(String path) {
        return fs.fsPropsBlocking(path);
    }

    @Override
    public FileSystem createTempDirectory(String prefix, Handler<AsyncResult<String>> handler) {
        handler.handle(Future.succeededFuture());
        return this;
    }

    @Override
    public String createTempDirectoryBlocking(String prefix) {
        return "none";
    }

    @Override
    public FileSystem createTempDirectory(String prefix, String perms, Handler<AsyncResult<String>> handler) {
        handler.handle(Future.succeededFuture());
        return this;
    }

    @Override
    public String createTempDirectoryBlocking(String prefix, String perms) {
        return "none";
    }

    @Override
    public FileSystem createTempDirectory(String dir, String prefix, String perms, Handler<AsyncResult<String>> handler) {
        handler.handle(Future.succeededFuture());
        return this;
    }

    @Override
    public String createTempDirectoryBlocking(String dir, String prefix, String perms) {
        return "none";
    }

    @Override
    public FileSystem createTempFile(String prefix, String suffix, Handler<AsyncResult<String>> handler) {
        handler.handle(Future.succeededFuture());
        return this;
    }

    @Override
    public String createTempFileBlocking(String prefix, String suffix) {
        return "none";
    }

    @Override
    public FileSystem createTempFile(String prefix, String suffix, String perms, Handler<AsyncResult<String>> handler) {
        handler.handle(Future.succeededFuture());
        return this;
    }

    @Override
    public String createTempFileBlocking(String prefix, String suffix, String perms) {
        return "none";
    }

    @Override
    public FileSystem createTempFile(String dir, String prefix, String suffix, String perms, Handler<AsyncResult<String>> handler) {
        handler.handle(Future.succeededFuture());
        return this;
    }

    @Override
    public String createTempFileBlocking(String dir, String prefix, String suffix, String perms) {
        return "none";
    }
}
