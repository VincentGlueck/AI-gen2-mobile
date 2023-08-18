package org.ww.ai.backup;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.entity.RenderResultLightWeight;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class LocalStorageBackupWriter extends AbstractBackupWriter {

    private final static String FILE_NAME_PREFIX = "AI-2-gen_";
    private final static String FILE_NAME_SUFFIX = ".zip";

    private File mZipFile;

    private ZipOutputStream mZipOutputStream;

    public LocalStorageBackupWriter(Context context) {
        super(context);
    }

    @Override
    public BackupHolder writeBackup(List<RenderResult> renderResults) {
        try {
            mZipOutputStream = prepareZipFile();
            writeImages();
        } catch (IOException e) {
            Log.e("ERROR", "" + e.getMessage());
        }
        return BackupHolder.create(mZipFile, 0);
    }

    private void writeImages() {
        final AppDatabase appDatabase = AppDatabase.getInstance(mContext);
        final ListenableFuture<List<RenderResult>> listenableFuture =
                appDatabase.renderResultDao().getAll();
        final AsyncDbFuture<List<RenderResult>> asyncDbFuture = new AsyncDbFuture<>();
        final XmlMapper xmlMapper = new XmlMapper();
        final AtomicInteger count = new AtomicInteger();
        asyncDbFuture.processFuture(listenableFuture,
                result -> {
                    count.set(result.size());
                    result.forEach(r -> {
                        String xml;
                        try {
                            xml = xmlMapper.writeValueAsString(RenderResultLightWeight.fromRenderResult(r));
                            addXmlToZip(r.uid, xml);
                        } catch (IOException e) {
                            Log.e("JSON", "said, no: " + e.getMessage());
                        }
                        byte[] thumbNail = r.thumbNail;
                        byte[] image = r.image;
                        try {
                            addImagesToZip(r.uid, thumbNail, image);
                        } catch (IOException e) {
                            Log.e("CREATE_ZIP", "failed: " + e.getMessage());
                        }
                    });
                    mZipOutputStream.close();
                }, mContext);
    }

    private void addXmlToZip(int uid, String xml) throws IOException {
        ZipEntry zipEntry = new ZipEntry(uid + ".result.xml");
        byte[] stringAsBytes = xml.getBytes(StandardCharsets.UTF_8);
        zipEntry.setSize(stringAsBytes.length);
        mZipOutputStream.putNextEntry(zipEntry);
        mZipOutputStream.write(stringAsBytes);
        mZipOutputStream.closeEntry();
    }

    private void addImagesToZip(Integer uid, byte[] thumbNail, byte[] image) throws IOException {
        ZipEntry zipEntry = new ZipEntry(uid + "|thumb.image.data");
        zipEntry.setSize(thumbNail.length);
        mZipOutputStream.putNextEntry(zipEntry);
        mZipOutputStream.write(thumbNail);
        mZipOutputStream.closeEntry();
        zipEntry = new ZipEntry(uid + "|image.image.data");
        mZipOutputStream.putNextEntry(zipEntry);
        mZipOutputStream.write(image);
        mZipOutputStream.closeEntry();
    }

    private ZipOutputStream prepareZipFile() throws IOException {
        String realName = FILE_NAME_PREFIX + System.currentTimeMillis() + FILE_NAME_SUFFIX;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        mZipFile = new File(storageDir, realName);
        ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(mZipFile.toPath()));
        outputStream.setComment("AI2-gen-mobile generated backup file");
        return outputStream;
    }

    @Override
    public List<BackupHolder> getBackupFiles() {
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File[] files = storageDir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        List<BackupHolder> result = new ArrayList<>();
        for (File file : files) {
            if (file.getName().startsWith(FILE_NAME_PREFIX) && file.getName().endsWith(FILE_NAME_SUFFIX)) {
                result.add(BackupHolder.create(file, getBackupFilesCount(file.getName())));
            }
        }
        result.sort(Collections.reverseOrder());
        return result;
    }

    @Override
    public int removeObsoleteBackups() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File[] files = downloadsDir.listFiles();
        if (files == null || files.length == 0) {
            return -1;
        }
        List<BackupHolder> backups = new ArrayList<>();
        for (File file : files) {
            if (file.getName().startsWith(FILE_NAME_PREFIX) && file.getName().endsWith(FILE_NAME_SUFFIX)) {
                backups.add(BackupHolder.create(file, getBackupFilesCount(file.getName())));
            }
        }
        backups.sort(Collections.reverseOrder());
        if (backups.size() > 1) {
            for (int n = 1; n < backups.size(); n++) {
                if (!backups.get(n).file.delete()) {
                    Log.e("DELETE", "this was not successful: " + backups.get(0).file.getAbsolutePath());
                }
            }
        }
        return backups.size() - 1;
    }

    private int getBackupFilesCount(String name) {
        File zipFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS
                + File.separator + name);
        ZipInputStream zipInputStream;
        int count = 0;
        try {
            zipInputStream = new ZipInputStream(Files.newInputStream(zipFile.toPath()));
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().endsWith(".xml")) {
                    count++;
                }
            }
        } catch (IOException e) {
            Log.e("ZIP", "error: " + e.getMessage());
            return -1;
        }
        return count;
    }

}
