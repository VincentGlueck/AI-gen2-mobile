package org.ww.ai.backup;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.NonNull;

import org.ww.ai.R;
import org.ww.ai.tools.FileUtil;

import java.io.File;
import java.util.Date;
import java.util.Objects;

public class BackupHolder implements Comparable<BackupHolder> {
    public File file;
    public Date date;
    public int count;
    public long size;

    public static BackupHolder create(@NonNull File file, int count) {
        BackupHolder holder = new BackupHolder();
        holder.file = file;
        holder.count = count;
        holder.size = file.length();
        holder.date = getDateFromFileName(file);
        return holder;
    }

    private static Date getDateFromFileName(@NonNull File file) {
        try {
            String str = file.getName().substring(file.getName()
                    .lastIndexOf('_') + 1, file.getName().lastIndexOf('.'));
            return new Date(Long.parseLong(str));
        } catch (StringIndexOutOfBoundsException e) {
            Log.e("ERROR", "getDateFromFileName failed on " + file.getName());
            return null;
        }
    }

    public String toReadableForm(Context context) {
        java.text.DateFormat dateFormat = DateFormat.getLongDateFormat(context.getApplicationContext());
        return context.getResources().getString(R.string.pref_backup_date,
                dateFormat.format(Objects.requireNonNull(getDateFromFileName(file))),
                count,
                FileUtil.FILE_UTIL.readableFileSize(file.length()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BackupHolder that = (BackupHolder) o;
        return Objects.equals(file.getName(), that.file.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }

    @Override
    public int compareTo(BackupHolder holder) {
        if(holder == null) {
            return 1;
        }
        if(file == null && holder.file == null) {
            return 0;
        }
        if(file == null) {
            return -1;
        }
        if(holder.file == null) {
            return 1;
        }
        return file.getName().compareTo(holder.file.getName());
    }

    @NonNull
    @Override
    public String toString() {
        return "BackupHolder{" +
                "file=" + file +
                ", date=" + date +
                ", count=" + count +
                ", size=" + size +
                '}';
    }
}
