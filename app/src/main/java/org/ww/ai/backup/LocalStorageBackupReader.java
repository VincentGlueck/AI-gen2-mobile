package org.ww.ai.backup;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.entity.RenderResultLightWeight;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LocalStorageBackupReader extends AbstractBackupReader {

    private final AppDatabase mAppDatabase;
    private BackupReaderResultHolder mResultHolder;

    public LocalStorageBackupReader(Context context) {
        super(context);
        mAppDatabase = AppDatabase.getInstance(context);
    }

    @Override
    public BackupReaderResultHolder restoreBackup(@NonNull BackupHolder backupHolder) {
        mResultHolder = new BackupReaderResultHolder();
        if(!backupHolder.file.exists()) {
            Log.e("BACKUPHOLDER", "file does not exist: " + backupHolder.file.getAbsolutePath());
            return mResultHolder;
        }
        try {
            processBackupFile(new ZipFile(backupHolder.file));
        } catch (IOException e) {
            Log.e("RESTORE", "restoreBackup failed with exception: " + e.getMessage());
        }
        return mResultHolder;
    }

    private void processBackupFile(ZipFile zipFile) throws IOException {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        ZipEntry zipEntry;
        final XmlMapper xmlMapper = new XmlMapper();
        Map<Integer, RenderResult> tempRenderResults = new HashMap<>();
        ZipEntry thumbEntry = null;
        ZipEntry imageEntry = null;
        while (entries.hasMoreElements()) {
            zipEntry = entries.nextElement();
            if(zipEntry.getName().endsWith(".xml")) {
                InputStream inputStream = null;
                try {
                     inputStream = zipFile.getInputStream(zipEntry);
                } finally {
                    Objects.requireNonNull(inputStream).close();
                }
                RenderResultLightWeight lightWeight = xmlMapper.readValue(zipFile.getInputStream(zipEntry), RenderResultLightWeight.class);
                tempRenderResults.put(lightWeight.uid, RenderResult.fromRenderResultLightWeight(lightWeight));
            } else if (zipEntry.getName().endsWith(".data") && zipEntry.getName().indexOf('|') > 0) {
                int idx = zipEntry.getName().indexOf("|");
                if(idx >= 0) {
                    int uid;
                    try {
                        uid = Integer.parseInt(zipEntry.getName().substring(0, idx));
                    } catch (NumberFormatException e) {
                        mResultHolder.messages.add("Entry name must start with a numeric UID: " + zipEntry.getName());
                        mResultHolder.failures++;
                        return;
                    }
                    if(zipEntry.getName().contains("thumb")) {
                        thumbEntry = zipEntry;
                    } else {
                        imageEntry = zipEntry;
                    }
                    if(thumbEntry != null && imageEntry != null) {
                        addImagesToRenderResult(zipFile, imageEntry, thumbEntry, tempRenderResults, uid);
                        imageEntry = null;
                        thumbEntry = null;
                    }
                } else {
                    mResultHolder.messages.add("Unexpected entry name: " + zipEntry.getName());
                    mResultHolder.failures++;
                }
            }
        }
    }

    private void addImagesToRenderResult(ZipFile zipFile, ZipEntry imageEntry,
            ZipEntry thumbEntry, Map<Integer, RenderResult> tempRenderResults, int uid) {
        RenderResult result;
        if(!tempRenderResults.containsKey(uid)) {
            logAndStoreImportProblem("Attempt to import uid=" + uid + ", but there's no " + uid + ".xml inside zip");
            mResultHolder.failures++;
            return;
        } else {
            result = tempRenderResults.get(uid);
            assert result != null;
        }
        RenderResult existingRenderResult = mAppDatabase.renderResultDao().getByIdOnThread(uid);
        if(existingRenderResult != null) {
            logAndStoreImportProblem("Image with uid=" + uid + " already exists!");
            mResultHolder.skipped++;
            return;
        }
        try {
            result.image = getImageDataFromZipEntry(zipFile, imageEntry);
            result.thumbNail = getImageDataFromZipEntry(zipFile, thumbEntry);
            mAppDatabase.renderResultDao().insertRenderResultOnThread(result);
            mAppDatabase.close();
            tempRenderResults.remove(uid);
            mResultHolder.restored++;
        } catch (IOException e) {
            logAndStoreImportProblem("Unable to read image data for uid=" + uid);
            mResultHolder.failures++;
        }
    }

    @NonNull
    private byte[] getImageDataFromZipEntry(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        byte[] buffer = new byte[8192];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream inputStream = null;
        try {
            inputStream = zipFile.getInputStream(zipEntry);
            int size;
            while((size = inputStream.read(buffer, 0, buffer.length)) > 0) {
                bos.write(buffer, 0, size);
            }
            bos.close();
        } finally {
            assert inputStream != null;
            inputStream.close();
        }
        return bos.toByteArray();
    }

    private void logAndStoreImportProblem(String msg) {
        mResultHolder.messages.add(msg);
    }

}
