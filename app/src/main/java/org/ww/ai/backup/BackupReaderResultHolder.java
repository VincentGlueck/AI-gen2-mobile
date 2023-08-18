package org.ww.ai.backup;

import java.util.ArrayList;
import java.util.List;

public class BackupReaderResultHolder {
    public List<String> messages = new ArrayList<>();
    public int restored = 0;
    public int skipped = 0;
    public int failures = 0;
}