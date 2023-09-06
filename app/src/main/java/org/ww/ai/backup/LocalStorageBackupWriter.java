package org.ww.ai.backup;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.PagingCache;
import org.ww.ai.rds.entity.RenderResultImageLW;
import org.ww.ai.rds.ifenum.GalleryAdapterCallbackIF;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class LocalStorageBackupWriter extends AbstractBackupWriter implements GalleryAdapterCallbackIF {

    private final static String FILE_NAME_PREFIX = "AI-2-gen_";
    private final static String FILE_NAME_SUFFIX = ".zip";

    private final PagingCache pagingCache;
    private int mIdx;
    private int mTotalCount;
    private XmlMapper mXmlMapper;
    private File mZipFile;
    private ZipOutputStream mZipOutputStream;
    private BackupDoneCallbackIF mBackupDoneCallback;

    public LocalStorageBackupWriter(Context context) {
        super(context);
        pagingCache = PagingCache.getInstance(context);

    }

    @Override
    public void writeBackup(BackupDoneCallbackIF backupDoneCallbackIF) {
        mBackupDoneCallback = backupDoneCallbackIF;
        try {
            mZipOutputStream = prepareZipFile();
            mXmlMapper = new XmlMapper();
            writeImages();
        } catch (IOException e) {
            Log.e("ERROR", "" + e.getMessage());
        }
    }

    @Override
    public void onCachingDone(List<PagingCache.PagingEntry> pagingEntries) {
        if (pagingEntries != null) {
            List<String> uids = pagingEntries.stream()
                    .map(p -> String.valueOf(p.renderResultLightWeight.uid)).collect(Collectors.toList());
            ListenableFuture<List<RenderResultImageLW>> future = pagingCache.getAppDatabase()
                    .renderResultDao().getImagesForUids(uids);
            AsyncDbFuture<List<RenderResultImageLW>> asyncDbFuture = new AsyncDbFuture<>();
            asyncDbFuture.processFuture(future, result -> {
                for (PagingCache.PagingEntry pagingEntry : pagingEntries) {
                    String xml;
                    try {
                        xml = mXmlMapper.writeValueAsString(pagingEntry.renderResultLightWeight);
                        byte[] thumbNail = pagingEntry.renderResultLightWeight.thumbNail;
                        byte[] image = result.stream().filter(r -> r.uid == pagingEntry.renderResultLightWeight.uid)
                                .findFirst().map(m -> m.image).orElse(null);
                        if (image == null) {
                            continue; // may happen if elder fake/test data involved
                        }
                        try {
                            addXmlToZip(pagingEntry.renderResultLightWeight.uid, xml);
                            addImagesToZip(pagingEntry.renderResultLightWeight.uid, thumbNail, image);
                        } catch (IOException e) {
                            Log.e("CREATE_ZIP", "failed: " + e.getMessage());
                        }
                    } catch (IOException e) {
                        Log.e("JSON", "said, no: " + e.getMessage());
                    }
                }
                mIdx += pagingEntries.size();
                if (mIdx < mTotalCount) {
                    mBackupDoneCallback.notifyProgress(mIdx, mTotalCount);
                    pagingCache.fillCache(mContext, mIdx, mIdx, this, false, true);
                } else {
                    try {
                        mZipOutputStream.flush();
                        mZipOutputStream.close();
                        mBackupDoneCallback.backupDone(mZipFile);
                    } catch (IOException ignore) {
                    }
                }
            }, mContext);
        }
    }

    private void writeImages() {
        final AppDatabase appDatabase = AppDatabase.getInstance(mContext);
        mTotalCount = appDatabase.renderResultDao().getCount(false);
        mIdx = 0;
        pagingCache.fillCache(mContext, mIdx, mIdx, this, false, true);
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
    public void removeObsoleteBackups() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File[] files = downloadsDir.listFiles();
        if (files == null || files.length == 0) {
            return;
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
    }

    private int getBackupFilesCount(String name) {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS
                + File.separator + name);
        int count = 0;
        try (ZipFile zipFile = new ZipFile(file)) {
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                if (entries.nextElement().getName().endsWith(".xml")) {
                    count++;
                }
            }
            return count;
        } catch (IOException e) {
            Log.e("ZIP", "error: " + e.getMessage());
        }
        return -1;
    }

}
