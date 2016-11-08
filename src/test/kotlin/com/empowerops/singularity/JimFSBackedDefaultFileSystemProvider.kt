package com.empowerops.singularity

import com.google.common.jimfs.SystemJimfsFileSystemProvider
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileAttributeView
import java.nio.file.spi.FileSystemProvider

class JimFSBackedDefaultFileSystemProvider() : FileSystemProvider() {

    constructor(provider: FileSystemProvider): this()

    @Suppress("DEPRECATION") //JimFS's API isn't compatible with my particular flow for setting it as the system default.
    val backingFS = SystemJimfsFileSystemProvider()

    override fun checkAccess(path: Path?, vararg modes: AccessMode?) = backingFS.checkAccess(path)
    override fun copy(source: Path?, target: Path?, vararg options: CopyOption) = backingFS.copy(source, target, *options)

    override fun <V : FileAttributeView?> getFileAttributeView(path: Path?, type: Class<V>?, vararg options: LinkOption?): V
            = backingFS.getFileAttributeView(path, type, *options)

    override fun isSameFile(path: Path?, path2: Path?): Boolean = backingFS.isSameFile(path, path2)
    override fun newFileSystem(uri: URI?, env: MutableMap<String, *>?): FileSystem = backingFS.newFileSystem(uri, env)
    override fun getScheme() = backingFS.scheme!!
    override fun isHidden(path: Path?) = backingFS.isHidden(path)

    override fun newDirectoryStream(dir: Path?, filter: DirectoryStream.Filter<in Path>?): DirectoryStream<Path>
            = backingFS.newDirectoryStream(dir, filter)

    override fun newByteChannel(path: Path?, options: MutableSet<out OpenOption>?, vararg attrs: FileAttribute<*>?): SeekableByteChannel
            = backingFS.newByteChannel(path, options, *attrs)

    override fun <A : BasicFileAttributes?> readAttributes(path: Path?, type: Class<A>?, vararg options: LinkOption?): A
            = backingFS.readAttributes(path, type, *options)

    override fun readAttributes(path: Path?, attributes: String?, vararg options: LinkOption?): MutableMap<String, Any>
            = backingFS.readAttributes(path, attributes, *options)

    override fun delete(path: Path?) = backingFS.delete(path)
    override fun getFileSystem(uri: URI?): FileSystem = backingFS.getFileSystem(uri)
    override fun getPath(uri: URI?): Path = backingFS.getPath(uri)
    override fun getFileStore(path: Path?): FileStore = backingFS.getFileStore(path)

    override fun setAttribute(path: Path?, attribute: String?, value: Any?, vararg options: LinkOption?)
            = backingFS.setAttribute(path, attribute, value, *options)

    override fun move(source: Path?, target: Path?, vararg options: CopyOption?)
            = backingFS.move(source, target, *options)

    override fun createDirectory(dir: Path?, vararg attrs: FileAttribute<*>?)
            = backingFS.createDirectory(dir, *attrs)

}