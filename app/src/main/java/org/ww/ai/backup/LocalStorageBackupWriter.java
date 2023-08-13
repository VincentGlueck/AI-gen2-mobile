package org.ww.ai.backup;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.entity.RenderResultLightWeight;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class LocalStorageBackupWriter extends AbstractBackupWriter {

    private final static String FILE_NAME_PREFIX = "AI-2-gen_";
    private final static String FILE_NAME_SUFFIX = ".zip";

    private Context mContext;
    private ZipOutputStream mZipOutputStream;
    private final Map<Integer, String> xmlMap = new HashMap<>();

    public LocalStorageBackupWriter(Context context) {
        mContext = context;
    }

    @Override
    public boolean writeBackup(List<RenderResultLightWeight> renderResults) throws JsonProcessingException {
        AtomicBoolean success = new AtomicBoolean(true);
        try {
            mZipOutputStream = prepareZipFile(FILE_NAME_PREFIX);
        } catch (IOException e) {
            Log.e("ERROR", Objects.requireNonNull(e.getMessage()));
            success.set(false);
        }
        if (success.get()) {
            prepareXmlMap(renderResults);
            if (!prepareImages()) {
                success.set(false);
            } else {
                try {
                    mZipOutputStream.close();
                } catch (IOException e) {
                    Log.e("FAILURE", "zipOutputStream failed.");
                    success.set(false);
                }
            }
        }
        return success.get();
    }

    private void showCouldNotCreateBackupMessage() {
        Log.w("WARN", "Backup has not been created.");
    }

    private void prepareXmlMap(List<RenderResultLightWeight> renderResults) throws JsonProcessingException {
        XmlMapper xmlMapper = new XmlMapper();
        for (RenderResultLightWeight renderResultLw : renderResults) {
            String xml = xmlMapper.writeValueAsString(renderResultLw);
            Log.d("XML", "" + xml);
            xmlMap.put(renderResultLw.uid, xml);
        }
    }

    private boolean prepareImages() {
        final AtomicBoolean success = new AtomicBoolean(true);
        AppDatabase appDatabase = AppDatabase.getInstance(mContext);
        for (Integer uid : xmlMap.keySet()) {
            ListenableFuture<RenderResult> listenableFuture = appDatabase.renderResultDao().getById(uid);
            AsyncDbFuture<RenderResult> asyncDbFuture = new AsyncDbFuture<>();
            asyncDbFuture.processFuture(listenableFuture,
                    result -> {
                        byte[] thumbNail = result.thumbNail;
                        byte[] image = result.image;
                        try {
                            addToZip(uid, thumbNail, image);
                        } catch (IOException e) {
                            Log.e("CREATE_ZIP", "failed: " + e.getMessage());
                            success.set(false);
                        }

                    }, mContext);
        }
        return success.get();
    }

    private void addToZip(Integer uid, byte[] thumbNail, byte[] image) throws IOException {
        ZipEntry zipEntry = new ZipEntry(uid + ".image.data");
        zipEntry.setSize(thumbNail.length);
        mZipOutputStream.putNextEntry(zipEntry);
        mZipOutputStream.write(thumbNail);
        mZipOutputStream.closeEntry();

        // TODO: add image
    }

    private ZipOutputStream prepareZipFile(String name) throws IOException {
        String realName = name + System.currentTimeMillis() + FILE_NAME_SUFFIX;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File zipFile = new File(storageDir, realName);
        return new ZipOutputStream(Files.newOutputStream(zipFile.toPath()));
    }


    @Override
    public boolean init() {
        // we do not need any init
        return true;
    }

    @Override
    public boolean finish() {
        if (mZipOutputStream != null) {
            try {
                mZipOutputStream.close();
            } catch (IOException e) {
                Log.w("ZIP", "Ignore - cannot close mZipOutputStream");
            }
        }
        return false;
    }
}
