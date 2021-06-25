package com.jeecms.common.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * 用于制作zip压缩包
 *
 * @author: tom
 * @date: 2018年12月26日 下午3:42:24
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved.Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class Zipper {
    private static final Logger log = LoggerFactory.getLogger(Zipper.class);

    /**
     * 制作压缩包
     */
    public static void zip(OutputStream out, List<FileEntry> fileEntrys, String encoding) {
        new Zipper(out, fileEntrys, encoding);
    }

    /**
     * 制作压缩包
     */
    public static void zip(OutputStream out, List<FileEntry> fileEntrys) {
        new Zipper(out, fileEntrys, null);
    }

    /**
     * 压缩文件
     *
     * @param srcFile 源文件
     * @param pentry  父ZipEntry
     * @throws IOException IOException
     */
    private void zip(File srcFile, FilenameFilter filter, ZipEntry pentry, String prefix) throws IOException {
        ZipEntry entry;
        if (srcFile.isDirectory()) {
            if (pentry == null) {
                entry = new ZipEntry(srcFile.getName());
            } else {
                entry = new ZipEntry(pentry.getName() + "/" + srcFile.getName());
            }
            File[] files = srcFile.listFiles(filter);
            if (files != null) {
                for (File f : files) {
                    zip(f, filter, entry, prefix);
                }
            }
        } else {
            if (pentry == null) {
                entry = new ZipEntry(prefix + srcFile.getName());
            } else {
                entry = new ZipEntry(pentry.getName() + "/" + prefix + srcFile.getName());
            }
            FileInputStream in;
            try {
                log.debug("读取文件：{}", srcFile.getAbsolutePath());
                in = new FileInputStream(srcFile);
                try {
                    zipOut.putNextEntry(entry);
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        zipOut.write(buf, 0, len);
                    }
                    zipOut.closeEntry();
                } finally {
                    in.close();
                }
            } catch (FileNotFoundException e) {
                log.error(String.format("制作压缩包时，源文件不存在：%s", srcFile.getAbsolutePath()));
            }
        }
    }

    /**
     * 创建Zipper对象
     *
     * @param out         输出流
     * @param filter      文件过滤，不过滤可以为null。
     * @param srcFilename 源文件名。可以有多个源文件，如果源文件是目录，那么所有子目录都将被包含。
     */
    /**
     * 创建Zipper对象
     * @param out           输出流
     * @param fileEntrys    文件过滤，不过滤为null
     * @param encoding  源文件名。可以有多个源文件，如果源文件是目录，那么所有子目录都将被包含。
     */
    protected Zipper(OutputStream out, List<FileEntry> fileEntrys, String encoding) {
        long begin = System.currentTimeMillis();
        log.debug("开始制作压缩包");
        try {
            try {
                zipOut = new ZipOutputStream(out);
                if (!StringUtils.isBlank(encoding)) {
                    log.debug("using encoding: {}", encoding);
                    zipOut.setEncoding(encoding);
                } else {
                    log.debug("using default encoding");
                }
                for (FileEntry fe : fileEntrys) {
                    zip(fe.getFile(), fe.getFilter(), fe.getZipEntry(), fe.getPrefix());
                }
            } finally {
                if (zipOut != null) {
                    zipOut.close();
                }
            }
        } catch (IOException e) {
            log.error("制作压缩包时，出现IO异常！");
        }
        long end = System.currentTimeMillis();
        log.info("制作压缩包成功。耗时：{}ms。", end - begin);
    }

    private byte[] buf = new byte[1024];
    private ZipOutputStream zipOut;

    public static class FileEntry {
        private FilenameFilter filter;
        private String parent;
        private File file;
        private String prefix;

        /**
         * 构造器
         *
         * @param parent 父文件夹
         * @param prefix 前缀
         * @param file   文件
         * @param filter FilenameFilter
         */
        public FileEntry(String parent, String prefix, File file, FilenameFilter filter) {
            this.parent = parent;
            this.prefix = prefix;
            this.file = file;
            this.filter = filter;
        }

        public FileEntry(String parent, File file) {
            this.parent = parent;
            this.file = file;
        }

        public FileEntry(String parent, String prefix, File file) {
            this(parent, prefix, file, null);
        }

        /**
         * 构造器
         */
        public ZipEntry getZipEntry() {
            if (StringUtils.isBlank(parent)) {
                return null;
            } else {
                return new ZipEntry(parent);
            }
        }

        public FilenameFilter getFilter() {
            return filter;
        }

        public void setFilter(FilenameFilter filter) {
            this.filter = filter;
        }

        public String getParent() {
            return parent;
        }

        public void setParent(String parent) {
            this.parent = parent;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        /**
         * 获取前缀
         */
        public String getPrefix() {
            if (prefix == null) {
                return "";
            } else {
                return prefix;
            }
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
    }

}
